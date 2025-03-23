package cloud.xcan.sdf.discovery.config;

import cloud.xcan.sdf.core.spring.boot.ApplicationBanner;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ApplicationInfo.class})
public class BannerConfiguration {

  @Bean
  public ApplicationBanner applicationBanner() {
    return new ApplicationBanner();
  }

}

