package cloud.xcan.angus.discovery.config;

import cloud.xcan.angus.core.spring.boot.ApplicationBanner;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
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

