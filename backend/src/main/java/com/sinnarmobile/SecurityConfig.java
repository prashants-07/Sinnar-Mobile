package com.sinnarmobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwords() { return new BCryptPasswordEncoder(12); }
    @Bean UserDetailsService users(AdminUsers users) {
        return username -> users.findByUsername(username)
            .map(u->User.withUsername(u.getUsername()).password(u.getPasswordHash()).roles(u.getRole()).build())
            .orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
    }
    @Bean CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
        var c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Content-Type","X-CSRF-TOKEN"));
        c.setAllowCredentials(true); c.setMaxAge(3600L);
        var source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**",c); return source;
    }
    @Bean SecurityFilterChain security(HttpSecurity http,ObjectMapper json,CorsConfigurationSource cors) throws Exception {
        http.cors(c->c.configurationSource(cors)).csrf(c->c.ignoringRequestMatchers("/api/public/enquiries","/api/public/repair-enquiries"))
            .authorizeHttpRequests(a->a
                .requestMatchers("/api/auth/csrf","/api/auth/login").permitAll()
                .requestMatchers("/api/admin/**","/api/auth/me").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,"/api/public/**","/uploads/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/public/enquiries","/api/public/repair-enquiries").permitAll()
                .requestMatchers("/api/**").denyAll()
                .anyRequest().permitAll())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation(f->f.changeSessionId()))
            .formLogin(f->f.loginProcessingUrl("/api/auth/login")
                .successHandler((req,res,auth)->{ res.setContentType("application/json"); json.writeValue(res.getWriter(),Api.ok(java.util.Map.of("username",auth.getName()))); })
                .failureHandler((req,res,e)->{ res.setStatus(401); res.setContentType("application/json"); json.writeValue(res.getWriter(),Api.error("Invalid username or password.",null)); }))
            .logout(l->l.logoutUrl("/api/auth/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req,res,auth)->{res.setContentType("application/json"); json.writeValue(res.getWriter(),Api.done("Logged out."));}))
            .exceptionHandling(e->e
                .authenticationEntryPoint((req,res,x)->{res.setStatus(401); res.setContentType("application/json"); json.writeValue(res.getWriter(),Api.error("Please log in.",null));})
                .accessDeniedHandler((req,res,x)->{res.setStatus(403); res.setContentType("application/json"); json.writeValue(res.getWriter(),Api.error("Access denied or CSRF token expired. Refresh and try again.",null));}))
            .headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' https: data:; font-src 'self'; connect-src 'self'; frame-src https://maps.google.com; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'")));
        return http.build();
    }
}
