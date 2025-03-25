package cloud.xcan.angus.discovery.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/actuator/**",
                "/instances/**",
                "/json/**")
            .permitAll() // Endpoint that does not require authentication
            .anyRequest().authenticated()) // Other requests require authentication
        .httpBasic(withDefaults()) // Enable http basic authentication
        .csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (configure as needed)
    return http.build();
  }

}
