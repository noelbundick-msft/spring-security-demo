package com.example.webapi;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.example.security.AuthRoleAssignment;
import com.example.security.AuthUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ThingPermissionEvaluator implements PermissionEvaluator {
  private final Logger logger = LoggerFactory.getLogger(ThingPermissionEvaluator.class);
  public static GrantedAuthority SYSTEM_ROLE = new SimpleGrantedAuthority("ROLE_SYSTEM");

  private final OAuth2AuthorizedClientService authorizedClientService;
  private String authzurl;
  private String globalAdminRole;

  @Autowired
  public ThingPermissionEvaluator(OAuth2AuthorizedClientService authorizedClientService,
      @Value("${com.example.security.authz-url}") String authzurl,
      @Value("${com.example.security.global-admin-role:global_admin}") String globalAdminRole) {

    Assert.isTrue(authorizedClientService != null, "authorizedClientService cannot be null");
    Assert.isTrue(authzurl != null, "authzurl cannot be null");
    Assert.isTrue(globalAdminRole != null, "globalAdminRole cannot be null");

    this.authorizedClientService = authorizedClientService;
    this.authzurl = authzurl;
    this.globalAdminRole = globalAdminRole;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
    if (authentication == null) {
      return false;
    }

    if (authentication.getAuthorities().contains(SYSTEM_ROLE)) {
      return true;
    }

    String userId = authentication.getName();
    String accessToken = authorizedClientService.loadAuthorizedClient("pingidentity", userId)
        .getAccessToken()
        .getTokenValue();

    Optional<AuthUserDetails> userDetails = getUserDetails(userId, accessToken);
    if (!userDetails.isPresent()) {
      return false;
    }

    if (userDetails.get().getRoleAssignments().get(1).getRole().getRoleName().equals("customer_read")
      && userDetails.get().getRoleAssignments().get(1).getScope().equals("/customers/11")) {
      return true;
    }

    // TODO: replace hard coded with actual things
    if (validatePermission(userDetails, "customer_read", "/customers/11")) {
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

    if (authentication.getAuthorities().contains(SYSTEM_ROLE)) {
      return true;
    }

    String userId = authentication.getName();
    String accessToken = authorizedClientService.loadAuthorizedClient("pingidentity", userId)
        .getAccessToken()
        .getTokenValue();

    Optional<AuthUserDetails> userDetails = getUserDetails(userId, accessToken);

    return evaluateRoleAssignments("/*", permission.toString(), userDetails.get().getRoleAssignments());
  }

  protected Optional<AuthUserDetails> getUserDetails(String userId, String accessToken) {
    try {
      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + accessToken);
      HttpEntity<Void> request = new HttpEntity<>(headers);

      ResponseEntity<AuthUserDetails> response = restTemplate.exchange(this.authzurl, HttpMethod.GET, request,
          AuthUserDetails.class, userId);
      return Optional.of(response.getBody());
    } catch (HttpClientErrorException ex) {
      // 4xx errors have no details but it's not a program error
      logger.info("User not retrieved from authZ service: " + userId);
      return Optional.empty();
    }
  }

  protected boolean evaluateRoleAssignments(String scope, String permission, List<AuthRoleAssignment> roleAssignments) {
    for (AuthRoleAssignment roleAssignment : roleAssignments) {
      String roleName = roleAssignment.getRole().getRoleName();

      // Global Admin can do everything
      if (roleName.equals(this.globalAdminRole)) {
        return true;
      }

      // Check for permission match
      if (!roleName.equals(permission)) {
        continue;
      }

      // Check for scope match
      if (roleAssignmentMatchesScope(roleAssignment, scope)) {
        return true;
      }
    }

    return false;
  }

  protected boolean roleAssignmentMatchesScope(AuthRoleAssignment roleAssignment, String scope) {
    // TODO: process wildcards
    return roleAssignment.getScope().equals(scope);
  }

  protected boolean validatePermission(Optional<AuthUserDetails> userDetails, String roleName, String scope) {
    if (!userDetails.isPresent()) {
      return false;
    }

    for (AuthRoleAssignment roleAssignment : userDetails.get().getRoleAssignments()) {
      if (roleAssignment.getRole().getRoleName().equals(roleName) && roleAssignmentMatchesScope(roleAssignment, scope)) {
        return true;
      }
    }

    return false;
  }
}
