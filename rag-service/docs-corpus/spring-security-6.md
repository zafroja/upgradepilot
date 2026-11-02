# Spring Security 6 migration

WebSecurityConfigurerAdapter was deprecated in Spring Security 5.7 and removed in 6.0.
Configure security by declaring beans instead of extending a base class.

Before (Security 5):
    @Configuration
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().antMatchers("/public/**").permitAll()
                .anyRequest().authenticated().and().httpBasic();
        }
    }

After (Security 6):
    @Configuration
    public class SecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
            return http.build();
        }
    }

Other changes:
- authorizeRequests() -> authorizeHttpRequests(); antMatchers/mvcMatchers -> requestMatchers.
- The chained .and() style is deprecated; use the lambda DSL.
- configure(AuthenticationManagerBuilder) -> expose a UserDetailsService and PasswordEncoder bean.
- configure(WebSecurity) for ignoring paths -> a WebSecurityCustomizer bean (prefer permitAll instead).
- @EnableGlobalMethodSecurity -> @EnableMethodSecurity.
- Spring Security OAuth2 (the old spring-security-oauth2 project) is end of life. Use Spring Authorization Server and spring-boot-starter-oauth2-resource-server.
