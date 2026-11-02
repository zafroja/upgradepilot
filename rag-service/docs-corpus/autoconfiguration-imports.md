# Registering auto-configuration in Spring Boot 3

Spring Boot 2.7 introduced a new registration file and Spring Boot 3.0 stopped reading
auto-configuration classes from META-INF/spring.factories.

Before: META-INF/spring.factories
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
      com.example.MyAutoConfiguration

After: META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    com.example.MyAutoConfiguration

Also annotate the class with @AutoConfiguration instead of @Configuration.
Other spring.factories keys (for example ApplicationListener or EnvironmentPostProcessor) still work.
