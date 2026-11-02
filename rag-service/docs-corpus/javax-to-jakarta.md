# Migrating javax to jakarta for Spring Boot 3

Spring Boot 3 is built on Jakarta EE 9+, which renamed the package prefix from javax to jakarta.

Packages that must change:
- javax.persistence.* -> jakarta.persistence.*
- javax.servlet.* -> jakarta.servlet.*
- javax.validation.* -> jakarta.validation.*
- javax.transaction.* -> jakarta.transaction.*
- javax.annotation.PostConstruct / PreDestroy / Resource -> jakarta.annotation.*
- javax.xml.bind.* -> jakarta.xml.bind.*

Packages that stay the same because they are part of the JDK: javax.sql, javax.crypto, javax.net, javax.naming, javax.management, javax.annotation.processing.

How to migrate:
1. Automated: run the OpenRewrite recipe `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` or the full `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` recipe.
2. IntelliJ IDEA: Refactor > Migrate Packages and Classes > Java EE to Jakarta EE.
3. Check third-party libraries: each one must have a Jakarta-compatible version, otherwise you get ClassNotFoundException at runtime for javax classes.

Example:
Before: import javax.persistence.Entity;
After:  import jakarta.persistence.Entity;
