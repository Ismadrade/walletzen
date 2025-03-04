package br.com.walletzen.adapter.inbound;

import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.GetAllUsersUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final GetAllUsersUseCase getAllUsersUseCase;

    public UserController(GetAllUsersUseCase getAllUsersUseCase) {
        this.getAllUsersUseCase = getAllUsersUseCase;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(getAllUsersUseCase.getAllUsers());
    }
}
