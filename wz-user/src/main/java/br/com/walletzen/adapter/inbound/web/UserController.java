package br.com.walletzen.adapter.inbound.web;

import br.com.walletzen.adapter.inbound.web.dto.UserRequest;
import br.com.walletzen.adapter.inbound.web.dto.UserResponse;
import br.com.walletzen.adapter.inbound.web.mapper.UserWebMapper;
import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.CreateUserUseCase;
import br.com.walletzen.core.port.input.DeleteUserUseCase;
import br.com.walletzen.core.port.input.EditUserUseCase;
import br.com.walletzen.core.port.input.GetUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final EditUserUseCase editUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserWebMapper userWebMapper;

    public UserController(GetUserUseCase getUserUseCase,
                          CreateUserUseCase createUserUseCase,
                          EditUserUseCase editUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UserWebMapper userWebMapper) {
        this.getUserUseCase = getUserUseCase;
        this.createUserUseCase = createUserUseCase;
        this.editUserUseCase = editUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.userWebMapper = userWebMapper;
    }

    @GetMapping
    public ResponseEntity<PageInfo<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        PageQuery pageQuery = new PageQuery(page, size, sort, direction);
        PageInfo<User> userPageInfo = getUserUseCase.getAllUsers(pageQuery);
        List<UserResponse> userResponses = userPageInfo.getContent()
                .stream()
                .map(userWebMapper::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new PageInfo<>(
                        userResponses,
                        userPageInfo.getPageNumber(),
                        userPageInfo.getPageSize(),
                        userPageInfo.getTotalElements(),
                        userPageInfo.getTotalPages(),
                        userPageInfo.isLast()
                ));
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userWebMapper.toResponse(getUserUseCase.getUserById(userId)));
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserRequest userRequest) {
        createUserUseCase.createUser(userWebMapper.toDomain(userRequest));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{userId}")
    public ResponseEntity<Void> editUser(@PathVariable("userId") UUID userId, @RequestBody UserRequest userRequest) {
        editUserUseCase.editUser(userId, userWebMapper.toDomain(userRequest));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
