package com.github.motoryang.auth.config;

import com.github.motoryang.common.web.filter.InternalSecretFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${auth.internal.secret}")
    private String internalSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        InternalSecretFilter internalFilter = new InternalSecretFilter(internalSecret);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class) // 认证前校验是否是网关发来的请求
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/refresh",
                                "/logout",
                                "/captcha",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/doc.html",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
