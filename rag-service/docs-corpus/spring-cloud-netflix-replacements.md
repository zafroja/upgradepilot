# Replacing removed Spring Cloud Netflix components

Spring Cloud 2020.0 removed most Netflix OSS integrations. Eureka is still supported.

- Hystrix (circuit breaker) -> Resilience4j via spring-cloud-starter-circuitbreaker-resilience4j. Replace @HystrixCommand(fallbackMethod=...) with @CircuitBreaker(name="x", fallbackMethod="...") from resilience4j-spring-boot3, or use the Spring Cloud CircuitBreaker factory API.
- Hystrix Dashboard and Turbine -> Micrometer metrics exported to Prometheus, visualised in Grafana.
- Zuul (edge gateway) -> Spring Cloud Gateway. Zuul filters become GatewayFilter / GlobalFilter beans; routes move to spring.cloud.gateway.routes. Note that Gateway is reactive (WebFlux).
- Ribbon (client load balancing) -> Spring Cloud LoadBalancer. Remove Ribbon dependencies and `ribbon.*` properties; @LoadBalanced RestTemplate/WebClient keeps working.
- Feign clients: move from Netflix Feign to spring-cloud-starter-openfeign, and configure fallbacks through Spring Cloud CircuitBreaker.
- Sleuth -> Micrometer Tracing (micrometer-tracing-bridge-brave or -otel).
