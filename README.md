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

## Demo usage

* Open this repository in GitHub Codespaces
* Launch the simulated AuthZ API: `npx serve ./authz`
* `cp .env.sample .env` and fill in the details for your Ping Identity application in your `.env` file
* Press F5 in VS Code, then navigate to `http://localhost:8080`
* Login, then visit `GET` on `things` - note that you receive HTTP 403 Forbidden
* Create a fake API response for your user
  * Visit `http://localhost:8080/me` to see your JWT. Copy your `sub` claim
  * `cp ./authz/sample_admin.json ./authz/{sub}.json`
  * Update `./authz/{sub}.json` with your details
* Visit `GET` on `things` - you should see 10 things, showing that you are successfully authorized as a `global_admin`


## CustomPermissionEvaluator

This is a sample `PermissionEvaluator` with the following features

## Logic

* Unauthenticated calls are always denied
* Always allowed: `ROLE_SYSTEM` - since we are using `@PreAuthorize` at the method level (versus on a `RestController` or similar), **all** calls must be authenticated - including ones internal to the software. Ex: to preload data on application startup.
* Authorization API: all other calls look up information from an external AuthZ REST API
  * User lookups are based on the `Authentication` name - normally the `sub` claim in an OAuth2 JWT
  * The user's bearer token is passed downstream to the AuthZ API
  * If the user holds a role assignment of `global_admin`, the call is allowed
  * If any of the user's role assignments match the requested scope/permission, the call is allowed
  * If there is no match, the call is denied

### Role assignment evaluation

The key parts of a 



## Configuration

`CustomPermissionEvaluator` can be configured via application.properties:

```
# REQUIRED: the downstream API that should be used to lookup user role assignments
com.example.security.authz-url: 'http://localhost:3000/users/{userId}.json'

# OPTIONAL: the name of the global admin role. Defaults to `global_admin`
com.example.security.authz-urlglobal-admin-role: global_admin
```

## References

* [Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
* [Expression-Based Access Control](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html#_method_security_expressions)
* [Introduction to Spring Method Security](https://www.baeldung.com/spring-security-method-security)
