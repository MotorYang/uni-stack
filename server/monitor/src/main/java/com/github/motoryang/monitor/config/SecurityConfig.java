package com.github.motoryang.monitor.config;

import com.github.motoryang.monitor.filter.GatewayInternalAuthFilter;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Boot Admin Server 安全配置
 *
 * @author motoryang
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminServerProperties adminServerProperties;
    private final GatewayInternalAuthFilter gatewayInternalAuthFilter;

    @Value("${auth.internal.secret}")
    private String internalSecret;

    public SecurityConfig(AdminServerProperties adminServerProperties,
                          GatewayInternalAuthFilter gatewayInternalAuthFilter) {
        this.adminServerProperties = adminServerProperties;
        this.gatewayInternalAuthFilter = gatewayInternalAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServerProperties.path("/"));

        http
                // 添加网关内部鉴权过滤器（在用户名密码认证之前执行）
                .addFilterBefore(gatewayInternalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 允许静态资源访问
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 允许登录页面访问
                        .requestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/login"))).permitAll()
                        // 允许 actuator 端点访问（供客户端注册）
                        .requestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/instances"))).permitAll()
                        .requestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/instances/**"))).permitAll()
                        .requestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/actuator/**"))).permitAll()
                        // 允许资源文件
                        .requestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/assets/**"))).permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage(adminServerProperties.path("/login"))
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl(adminServerProperties.path("/logout"))
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher(adminServerProperties.path("/instances")),
                                new AntPathRequestMatcher(adminServerProperties.path("/instances/**")),
                                new AntPathRequestMatcher(adminServerProperties.path("/actuator/**"))
                        )
                );

        return http.build();
    }

}
