package br.com.walletzen.adapter.inbound;

import br.com.walletzen.adapter.dto.UserRequest;
import br.com.walletzen.adapter.dto.UserResponse;
import br.com.walletzen.adapter.mapper.UserMapper;
import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.dto.PageRequestDTO;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.service.UserServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    private final UserServicePort userServicePort;
    private final UserMapper userMapper;

    public UserController(UserServicePort userServicePort, UserMapper userMapper) {
        this.userServicePort = userServicePort;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<PageInfo<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO(page, size, sort, direction);
        PageInfo<User> userPageInfo  = userServicePort.getAllUsers(pageRequestDTO);
        List<UserResponse> userResponses = userPageInfo.getContent()
                .stream()
                .map(userMapper::toRecord)
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
        return ResponseEntity.status(HttpStatus.OK).body(userMapper.toRecord(userServicePort.getUserById(userId)));
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody UserRequest userRequest) {
        userServicePort.createUser(userMapper.toDomain(userRequest));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{userId}")
    public ResponseEntity editUser(@PathVariable("userId") UUID userId, @RequestBody UserRequest userRequest) {
        userServicePort.editUser(userId, userMapper.toDomain(userRequest));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("{userId}")
    public ResponseEntity deleteUser(@PathVariable("userId") UUID userId) throws Exception {
        userServicePort.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
