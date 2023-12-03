package br.com.walletzen.authuser.adapters.in.controller.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter@Setter
public class UserRequest {

    @NotBlank(message = "username field is empty")
    private String username;

    @NotBlank(message = "email field is empty.")
    private String email;

    @NotBlank(message = "password field is empty.")
    private String password;

    @NotBlank(message = "fullName field is empty.")
    private String fullName;

    @NotBlank(message = "cpf field is empty.")
    @CPF(message = "Invalid CPF.")
    private String cpf;
}
