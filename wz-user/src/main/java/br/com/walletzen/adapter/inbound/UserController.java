package br.com.walletzen.adapter.inbound;

import br.com.walletzen.adapter.dto.UserRequest;
import br.com.walletzen.adapter.dto.UserResponse;
import br.com.walletzen.adapter.mapper.UserMapper;
import br.com.walletzen.core.service.UserServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserServicePort userServicePort;
    private final UserMapper userMapper;

    public UserController(UserServicePort userServicePort, UserMapper userMapper) {
        this.userServicePort = userServicePort;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userServicePort.getAllUsers().stream().map(userMapper::toRecord).toList());
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody UserRequest userRequest) {
        userServicePort.createUser(userMapper.toDomain(userRequest));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
