package com.github.noelbundick_msft.security;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.client.RestTemplate;

public class CustomPermissionEvaluator implements PermissionEvaluator {

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    // Admins can do anything
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      return true;
    }

    // TODO call downstream API
    return false;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
      Object permission) {
    RestTemplate restTemplate = new RestTemplate();

    String url = String.format("http://localhost:3000/users/%s.json", authentication.getName());
    AuthUserDetails authZ = restTemplate.getForObject(url, AuthUserDetails.class);
    for (AuthRoleAssignment roleAssignment : authZ.getRoleAssignments()) {
      // Global Admin can do everything
      if (roleAssignment.role.getRoleName().equals("global_admin")) {
        return true;
      }

      // Check for permission match
      if (!roleAssignment.role.getRoleName().equals(permission)) {
        continue;
      }

      // Check for scope match
      // TODO: process wildcards
      if (roleAssignment.scope.equals("/*")) {
        return true;
      }
    }

    return false;
  }

}
