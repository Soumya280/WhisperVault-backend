package com.whispervault.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.whispervault.Security.JsonAuthFilter;
import com.whispervault.Service.CustomUserDetailsService;

import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ---- UserDetailsService Bean ----
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    // ---- PasswordEncoder Bean -------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Safe password hashing
    }

    // ---- DaoAuthenticationProvider ---------
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // ---- AuthenticationManager -----
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ---- Security Filter Chain -----
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authManager,
            DaoAuthenticationProvider authProvider) throws Exception {

        // Custom JSON login filter
        JsonAuthFilter jsonFilter = new JsonAuthFilter();
        jsonFilter.setAuthenticationManager(authManager);
        jsonFilter.setFilterProcessesUrl("/login");

        jsonFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(30 * 60); // 30 min
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Login successful\"}");
        });

        jsonFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Login failed: " + exception.getMessage() + "\"}");
        });

        http
                .cors(cors -> {
                }) // Uses CorsConfigurationSource bean
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/signup", "/logout")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/login", "/signup"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Logout successful\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll());

        return http.build();
    }
}
