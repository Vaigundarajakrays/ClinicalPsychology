package com.clinicalpsychology.app.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class  JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final HandlerExceptionResolver handlerExceptionResolver;

    // not used constructor because we used @RequiredArgsConstructor



    // jwt expired, invalid, exceptions are in global exception handler

    //GLOBAL EXCEPTION HANDLER ONLY CATCHES EXCEPTION FROM CONTROLLER LAYER
    //JWT BASED EXCEPTION ARE NOT CATCH BY GLOBAL EXCEPTION HANDLER, IT IS PART OF THE FILTER LAYER
    //THAT'S WHY WE USED HandlerExceptionResolver
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // This line is safe only if we are using MODE_THREAD LOCAL which we are using it by default
                    // If our app is Reactive and not servlet, it is better to create a new Context obj using SecurityContextHolder.createEmptyContext()
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e){
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }
}
