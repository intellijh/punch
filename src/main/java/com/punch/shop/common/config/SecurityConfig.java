package com.punch.shop.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/",
            "/member/register",
            "/member/login",
            "/css/**",
            "/js/**",
            "/images/**"
    };

    private static final String LOGIN_PAGE_URL = "/member/login";
    private static final String LOGIN_PROCESSING_URL = "/member/login";
    private static final String LOGIN_FAILURE_URL = "/member/login?error=true";
    private static final String LOGOUT_URL = "/member/logout";
    private static final String DEFAULT_SUCCESS_URL = "/";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage(LOGIN_PAGE_URL)
                        .loginProcessingUrl(LOGIN_PROCESSING_URL)
                        .defaultSuccessUrl(DEFAULT_SUCCESS_URL, false)
                        .failureUrl(LOGIN_FAILURE_URL)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl(LOGOUT_URL)
                        .logoutSuccessUrl(DEFAULT_SUCCESS_URL)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
