package com.clinicalpsychology.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // if not add this, cors error occur
                .csrf(AbstractHttpConfigurer::disable)
//                .securityContext(securityContext -> securityContext // If not add this when api is needs to be authorised, ai-therapist async api, will throw error
//                        .securityContextRepository(new RequestAttributeSecurityContextRepository())
//                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/stripe/webhook").permitAll()
                        .requestMatchers("/api/auth/signUp",
                                "/api/auth/adminSignUp",
                                "/api/therapist/register",
                                "/api/client/register",
                                "/api/ai-therapist/stream",
                                "/api/contact-messages",
                                "/api/categories",
                                "/api/therapist/getAllTherapists",
                                "/api/s3/upload/**",
                                "/api/auth/changePassword",
                                "/api/auth/login",
                                "/api/auth/sendOtp", "/api/auth/verifyOtp" ,"/oauth2/**", "/calendar/**",
                                "/api/getAllCategories", "/api/getTopRatedTherapists",
                                "/api/getVerifiedTherapists", "/api/getTopTherapists",
                                "/api/searchTherapists").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
