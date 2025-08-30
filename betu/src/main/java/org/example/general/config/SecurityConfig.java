package org.example.general.config;

import lombok.RequiredArgsConstructor;
import org.example.general.jwt.JwtAuthenticationFilter;
import org.example.general.jwt.JwtProvider;
import org.example.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 테스트용)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/api/user/signup/step1", "/api/user/signup/step2", "/api/user/login", "/upload/glb"
                        ).permitAll() // 스웨거 허용
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .formLogin(form -> form.disable()) // 기본 로그인폼 끄기
                .addFilterBefore(jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, userRepository);
        return jwtAuthenticationFilter;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 포함 허용
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Authorization-refresh"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
