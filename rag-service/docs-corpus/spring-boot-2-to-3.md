# Spring Boot 2.x to 3.x upgrade path

Recommended path:
1. Move to Java 17 first while still on Boot 2.7 (2.7 supports Java 17).
2. Upgrade to the latest Spring Boot 2.7.x and fix every deprecation warning. Deprecated APIs in 2.7 are removed in 3.0.
3. Add `spring-boot-properties-migrator` temporarily to log renamed or removed configuration properties at startup. Remove it after fixing them.
4. Upgrade to Spring Boot 3.x and the matching Spring Cloud release train (for example Boot 3.3 pairs with Spring Cloud 2023.0.x).
5. Migrate javax.* imports to jakarta.* (see javax-to-jakarta notes).
6. Migrate Spring Security configuration to the component-based style (see spring-security-6 notes).
7. Run the full test suite and a smoke test of every endpoint.

Notable breaking changes in Boot 3:
- Jakarta EE 9/10 namespace (jakarta.*) replaces javax.*.
- Hibernate 6 is the default JPA provider. Some HQL, ID generation and type mappings behave differently.
- Trailing slash matching is disabled by default: `/users/` no longer matches `/users`.
- Auto-configuration registration moved from spring.factories to AutoConfiguration.imports.
- Spring Cloud Sleuth is replaced by Micrometer Tracing.
- Some properties were renamed, e.g. `spring.redis.*` became `spring.data.redis.*`.
