package com.clinicalpsychology.app.security;


import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UsersRepository usersRepository;


    // To avoid this JwtAuthenticationFilter to be added as a filter in servlet container
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Prevents container from auto-registering it
        return registration;
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return username -> {

            Users user = usersRepository.findByEmailId(username);

            if(user==null){
                throw new UsernameNotFoundException("Invalid username or password.");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmailId())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole().name()) // e.g. ADMIN, USER, etc.
                    .build();
        };
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

