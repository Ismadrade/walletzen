package br.com.walletzen.config;

import br.com.walletzen.WzUserApplication;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import br.com.walletzen.core.port.output.UserPersistencePort;
import br.com.walletzen.core.service.UserServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = WzUserApplication.class)
public class BeanConfiguration {
    @Bean
    UserServicePort notificationServicePortImpl(UserPersistencePort persistence, UserDeletedEventPublisherPort eventPublisher) {
        return new UserServicePort(persistence, eventPublisher);
    }


}
