package br.com.walletzen.config;

import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import br.com.walletzen.core.port.output.UserPersistencePort;
import br.com.walletzen.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    UserService userService(UserPersistencePort persistence, UserDeletedEventPublisherPort eventPublisher) {
        return new UserService(persistence, eventPublisher);
    }
}
