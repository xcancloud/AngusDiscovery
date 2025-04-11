# AngusDiscovery - 分布式服务注册中心

[English](README.md) | [中文](README_zh.md)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-4.2.0-brightgreen)](https://spring.io/projects/spring-cloud)
[![Eureka Server](https://img.shields.io/badge/Eureka%20Server-2.0.4-lightgrey)](https://spring.io/projects/spring-cloud-netflix)

## 项目概述

基于 Spring Cloud Eureka Server 深度定制的分布式服务注册中心，为 Angus 微服务生态提供以下核心能力：

### 核心功能

- **服务发现**：自动注册与动态发现服务实例
- **健康监控**：心跳机制实时追踪实例状态
- **负载均衡**：提供实时可用的服务实例列表
- **高可用**：多节点集群部署确保服务可靠性

> 💡 生产环境建议启用配置项 `security.require-ssl=true` 开启 TLS 加密。

## 核心特性

### 🚀 增强管理接口

| 端点             | 方法  | 功能描述       | 调用示例                                  |
|----------------|-----|--------------|----------------------------------------|
| `/json/status` | GET | 注册中心状态    | `GET http://localhost:1801/json/status` |
| `/json/lastn`  | GET | 服务续约信息    | `GET http://localhost:1801/json/lastn`  |

### 🔗 生态集成

- **可视化管控**：与 AngusGM 无缝对接，实时展示注册信息
- **深度监控**：集成 Metrics，输出 Prometheus 格式指标

## 客户端接入

### 添加依赖

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  <version>4.2.0</version>
</dependency>
```

### 配置示例

```yml
eureka:
  client:
    healthcheck:  # 使用/health端点检测应用健康
      enabled: true
    eureka-connection-idle-timeout-seconds: 10  # 连接空闲超时
    initial-instance-info-replication-interval-seconds: 10  # 实例注册延迟
    registry-fetch-interval-seconds: 10  # 注册表拉取间隔
    serviceUrl:
      defaultZone: http://${DISCOVERY_USER:discovery}:${DISCOVERY_PWD:discovery}@${DISCOVERY_HOST:localhost}:1801/eureka/
  
  instance:
    appname: '@artifactId@'  # Maven应用名
    instance-id: ${spring.cloud.client.ip-address}:${server.port}  # 实例ID格式
    lease-expiration-duration-in-seconds: 30  # 租约过期时间
    lease-renewal-interval-in-seconds: 10  # 续约间隔
    prefer-ip-address: true  # 优先显示IP
    status-page-url: http://${spring.cloud.client.ip-address}:${server.port}/swagger-ui/
    health-check-url-path: /actuator/health
```

## 快速部署

### 环境要求
- JDK 17+
- Maven 3.6+

### 以源码方式运行

```bash
# 克隆项目
git clone https://github.com/xcancloud/AngusDiscovery.git

# 构建社区版、生产环境版
mvn clean package -DskipTests -P dist.community,env.prod

# 启动服务
java -jar target/AngusDiscovery-Community-1.0.0.jar
```

### 以 Docker 方式运行

```bash
# 拉去镜像
docker pull angusdiscovery:1.0.0

# 启动（通过参数 -d 在后台允许）
docker run --name angusdiscovery -d -p 1801:1801 angusdiscovery:1.0.0 
```

### 验证注册

1. **控制台验证**  
   访问 [http://localhost:1801](http://localhost:1801)  
   使用默认凭证 discovery/discovery 登录，查看实例状态为 `UP`

2. **管理平台验证**  
   在 AngusGM 平台 **系统 > 注册中心** 查看注册状态

## 开源协议

📜 本项目采用 [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html) 开源协议。
