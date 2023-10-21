package br.com.walletzen.authuser.adapters.in.controller;

import br.com.walletzen.authuser.adapters.in.controller.mapper.UserMapper;
import br.com.walletzen.authuser.adapters.in.controller.request.UserRequest;
import br.com.walletzen.authuser.application.ports.in.InsertUserInputPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private InsertUserInputPort insertUserInputPort;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody UserRequest userRequest){
        insertUserInputPort.insert(userMapper.toUser(userRequest));
        return ResponseEntity.ok().build();
    }
}
