package me.strong.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.Random;

/**
 * Created by taesu on 2018-06-08.
 */
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(new Random().nextLong());
    }

}
