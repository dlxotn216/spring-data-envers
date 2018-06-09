package me.strong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.Random;

/**
 * Created by taesu on 2018-06-08.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfiguration {

    @Bean
    public AuditorAware<Long> auditorAware(){
        //TODO ThreadLocal.get() -> 현재 request의 사용자 추출
        return new AuditorAwareImpl();
    }
}
