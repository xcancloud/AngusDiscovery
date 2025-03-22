package cloud.xcan.sdf.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class XCanAngusDiscoveryApplication {

  public static void main(String[] args) {
    SpringApplication.run(XCanAngusDiscoveryApplication.class, args);
  }
}
