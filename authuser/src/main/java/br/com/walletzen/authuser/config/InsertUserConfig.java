package br.com.walletzen.authuser.config;

import br.com.walletzen.authuser.adapters.out.InsertUserModelAdapter;
import br.com.walletzen.authuser.application.core.usecase.InsertUserModelUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InsertUserConfig {

    @Bean
    public InsertUserModelUseCase insertUserModelUseCase(InsertUserModelAdapter insertUserModelAdapter){
        return new InsertUserModelUseCase(insertUserModelAdapter);
    }
}
