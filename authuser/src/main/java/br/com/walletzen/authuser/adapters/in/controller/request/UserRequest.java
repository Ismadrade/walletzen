package br.com.walletzen.authuser.adapters.in.controller.request;


import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class UserRequest {

    private String username;

    private String email;

    private String password;

    private String fullName;

    private String cpf;
}
