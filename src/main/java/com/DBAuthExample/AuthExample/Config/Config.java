package com.DBAuthExample.AuthExample.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public Matrix matrix() {
        return new Matrix();
    }
}
