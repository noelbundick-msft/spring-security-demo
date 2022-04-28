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
    // Admins can do anything
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      return true;
    }

    RestTemplate restTemplate = new RestTemplate();

    String url = String.format("http://localhost:3000/users/%s.json", authentication.getName());
    AuthZResponse authZ = restTemplate.getForObject(url, AuthZResponse.class);
    for (AuthZRoleAssignment roleAssignment : authZ.getRoleAssignments()) {
      String desiredScope = String.format("/things/%s", targetId);
      if (roleAssignment.scope.equals(desiredScope) && roleAssignment.role.equals(permission)) {
        return true;
      }
    }

    return false;
  }

}
