package cloud.xcan.sdf.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableEurekaServer
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class XCanDiscoveryApplication {

  public static void main(String[] args) {
    SpringApplication.run(XCanDiscoveryApplication.class, args);
  }

  @EnableWebSecurity
  public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.headers().frameOptions().disable();

      http.csrf().disable().authorizeRequests()
          .antMatchers("/actuator/**", "/instances", "/instances/*", "/pubapi/**").permitAll()
          .anyRequest().authenticated().and().httpBasic();
    }
  }

}
