# AngusDiscovery - Distributed Service Registry Center

[English](README.md) | [中文](README_zh.md)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-4.2.0-brightgreen)](https://spring.io/projects/spring-cloud)
[![Eureka Server](https://img.shields.io/badge/Eureka%20Server-2.0.4-lightgrey)](https://spring.io/projects/spring-cloud-netflix)

## Project Overview

**AngusDiscovery** is a customized distributed service registry center based on Spring Cloud Netflix Eureka Server, ***specifically designed for the Angus microservices ecosystem***. It extends native Eureka capabilities with enhanced registry state management and integration support.

### Core Functions of Eureka Server

- **Service Registration & Discovery**: Automatic registration of microservice instances and dynamic discovery by consumers
- **Health Monitoring**: Real-time instance status tracking through heartbeat mechanism
- **Load Balancing**: Provides instance lists for client-side load balancers like Ribbon
- **High Availability**: Supports multi-node cluster deployment for registry reliability

> **Tip**: Enable TLS encryption for production environments using `security.require-ssl=true`.

## Extended Features

### ✨ Enhanced RESTful Management APIs

We extend native Eureka APIs with these endpoints:

| Endpoint          | Method | Description                | Example                                      |
|-------------------|--------|----------------------------|----------------------------------------------|
| `/json/status`    | GET    | Get registry status        | `GET http://localhost:1801/json/status`      |
| `/json/lastn`     | GET    | Get renewal information    | `GET http://localhost:1801/json/lastn`       |

### ✨ Deep Integration with Angus Ecosystem

- **AngusGM Integration**: Visualize registration data through Angus Governance Manager
- **Monitoring Extension**: Built-in Metrics instrumentation with Prometheus support

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.6+

### Deployment Steps

```bash
# Clone repository
git clone https://github.com/xcancloud/AngusDiscovery.git

# Build project (Cloud Service profile, Dev environment)
mvn clean package -DskipTests -P dist.cloudService,env.dev

# Start service
java -jar target/AngusDiscovery-1.0.0.jar
```

## Client Integration

### Add Dependency

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
  <version>4.2.0</version>
</dependency>
```

### Client Configuration

```yml
eureka:
  client:
    # Native heartbeat checks process status but not application health
    # Use actuator's /health endpoint for comprehensive health checks
    healthcheck:
      enabled: true
    # Max connection idle time to Eureka Server (seconds)
    eureka-connection-idle-timeout-seconds: 10
    # Initial registration delay after startup (default: 40s)
    initial-instance-info-replication-interval-seconds: 10
    # Registry refresh interval (default: 30s)
    registry-fetch-interval-seconds: 10
    serviceUrl:
      defaultZone: http://${DISCOVERY_SECURITY_USER_NAME:discovery}:${DISCOVERY_SECURITY_USER_PASSWORD:discovery}@${DISCOVERY_HOST:localhost}:${DISCOVERY_PORT:1801}/eureka/
  instance:
    # Registered application name (uses Maven artifactId)
    appname: '@artifactId@'
    # Instance ID format (IP:Port)
    instance-id: ${spring.cloud.client.ip-address}:${server.port}
    # Lease expiration time (default: 90s)
    lease-expiration-duration-in-seconds: 30
    # Lease renewal interval (default: 30s)
    lease-renewal-interval-in-seconds: 10
    # Display IP instead of hostname
    prefer-ip-address: true
    # Status page configuration
    status-page-url: http://${spring.cloud.client.ip-address}:${server.port}/swagger-ui/
    health-check-url-path: /actuator/health
```

### Verify Registration

- **Method 1**: Access registry console at http://localhost:1801/  
  Use default Basic Auth credentials: discovery/discovery  
  Confirm instance status shows `UP`

- **Method 2**: Check registration status in AngusGM:  
  Navigate to **System** → **Registry Center**

## License

📜 Licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
