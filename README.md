# spring-security-demo

This repository contains an example of how to use Spring Security on a per-method basis

## Overview

Here's a high-level overview of the general approach that will let you secure your methods in any way you see fit (ex: calling a downstream API to check permissions).

First, your application needs to be configured to enable method security

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
}
```

> Note: As of Spring Security 5.6.3, the newer `@EnableMethodSecurity` annotation incorrectly resolves `@PreAuthorize` attributes on proxies. This is a known bug and is being tracked at [spring-security/11175](https://github.com/spring-projects/spring-security/issues/11175). So stick with the `@EnableGlobalMethodSecurity(prePostEnabled = true)` approach until a fix is released

You can now start using the `@PreAuthorize` attribute on methods that you want to secure. Spring provides `hasPermission()` expression methods out of the box with flavors for domain object, and for a combination of id/type. Ex:

```java
public interface ThingRepository extends JpaRepository<Thing, Long> {
  @Override
  @PreAuthorize("hasPermission(#id, 'org', 'read')")
  Optional<Thing> findById(Long id);

  @Override
  @PreAuthorize("hasPermission(#entity, 'write')")
  <S extends Thing> S save(S entity);
}
```

Spring uses a `PermissionEvaluator` with a default behavior of deny all by default, so you need to create your own. Your custom logic goes here

```java
public class CustomPermissionEvaluator implements PermissionEvaluator {
  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) { }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) { }
}
```

With that created, you can now tell your app to use your evaluator by configuring a `MethodSecurityExpressionHandler` bean

```java
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

    CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator();
    handler.setPermissionEvaluator(evaluator);

    return handler;
  }
```

## References

* [Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
* [Expression-Based Access Control](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html#_method_security_expressions)
* [Introduction to Spring Method Security](https://www.baeldung.com/spring-security-method-security)
