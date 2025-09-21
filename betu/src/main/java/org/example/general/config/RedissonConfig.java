package org.example.general.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(
            @org.springframework.beans.factory.annotation.Value("${redisson.address}") String address,
            @org.springframework.beans.factory.annotation.Value("${redisson.password}") String password
    ) {
        Config config = new Config();
        var single = config.useSingleServer().setAddress(address);
        if (password != null && !password.isBlank()) {
            single.setPassword(password);
        }
        single.setConnectionMinimumIdleSize(8);
        single.setConnectionPoolSize(32);
        return Redisson.create(config);
    }
}
