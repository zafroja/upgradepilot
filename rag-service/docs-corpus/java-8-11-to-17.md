# Upgrading a Spring application to Java 17

Spring Boot 3 and Spring Framework 6 require Java 17 as the minimum version.

Steps:
1. Install JDK 17 and set `<java.version>17</java.version>` (or `maven.compiler.release` to 17) in the root pom.xml.
2. Upgrade build plugins: maven-compiler-plugin 3.11+, maven-surefire-plugin 3.x, and any plugin that does bytecode work (JaCoCo 0.8.8+, Lombok 1.18.22+, MapStruct 1.5+).
3. Java EE modules removed in Java 11 (JAXB, JAX-WS, javax.annotation) are no longer in the JDK. Add explicit Jakarta dependencies such as `jakarta.xml.bind:jakarta.xml.bind-api` when needed.
4. Strong encapsulation (JEP 403): libraries that use reflection into JDK internals fail with InaccessibleObjectException. Upgrade those libraries first. Use `--add-opens` only as a temporary workaround.
5. Old versions of Mockito, ByteBuddy, Groovy, Hibernate 5.0-5.2 and ASM do not support Java 17 class files. Upgrade them.
6. Optional modernisation after it compiles: records for DTOs, text blocks, switch expressions, pattern matching for instanceof.

Common errors:
- "Unsupported class file major version 61": a tool or library is too old for Java 17 bytecode.
- "package javax.xml.bind does not exist": add the Jakarta XML Binding API and runtime.
