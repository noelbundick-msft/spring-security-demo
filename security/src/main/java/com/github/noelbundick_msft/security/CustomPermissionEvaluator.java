package com.github.noelbundick_msft.security;

import java.io.Serializable;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class CustomPermissionEvaluator implements PermissionEvaluator {
  private final OAuth2AuthorizedClientService authorizedClientService;

  public CustomPermissionEvaluator(OAuth2AuthorizedClientService authorizedClientService) {
    if (authorizedClientService == null) {
      throw new IllegalArgumentException("authorizedClientService cannot be null");
    }

    this.authorizedClientService = authorizedClientService;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    if (authentication == null) {
      return false;
    }

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
    if (authentication == null) {
      return false;
    }

    try {
      String userId = authentication.getName();
      OAuth2AccessToken accessToken = authorizedClientService
          .loadAuthorizedClient("pingidentity", userId).getAccessToken();

      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + accessToken.getTokenValue());
      HttpEntity<Void> request = new HttpEntity<>(headers);

      ResponseEntity<AuthUserDetails> response = restTemplate.exchange("http://localhost:3000/users/{userId}.json",
          HttpMethod.GET, request, AuthUserDetails.class, userId);
      AuthUserDetails authZ = response.getBody();

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
    } catch (HttpClientErrorException ex) {
      // 4xx errors are not authorized
      return false;
    }

    return false;
  }

}
