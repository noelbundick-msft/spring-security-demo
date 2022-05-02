package com.example.security;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
  private final Logger logger = LoggerFactory.getLogger(CustomPermissionEvaluator.class);
  private final SystemRolePermissionEvaluator systemRoleEvaluator = new SystemRolePermissionEvaluator();

  private final OAuth2AuthorizedClientService authorizedClientService;
  private String authzurl;
  private String globalAdminRole;

  @Autowired
  public CustomPermissionEvaluator(OAuth2AuthorizedClientService authorizedClientService,
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

    // TODO: remove after we test a CompositePermissionEvaluator
    if (systemRoleEvaluator.hasPermission(authentication, targetDomainObject, permission)) {
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

    // TODO: remove after we test a CompositePermissionEvaluator
    if (systemRoleEvaluator.hasPermission(authentication, targetId, targetType, permission)) {
      return true;
    }

    if (!(authentication instanceof OAuth2AuthenticationToken)) {
      return false;
    }

    OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
    String userId = oauth2Authentication.getPrincipal().getAttribute("userId");
    if (userId == null) {
      return false;
    }

    String accessToken = authorizedClientService.loadAuthorizedClient("pingidentity", authentication.getName())
        .getAccessToken()
        .getTokenValue();

    Optional<AuthUserDetails> userDetails = getUserDetails(userId, accessToken);
    if (!userDetails.isPresent()) {
      return false;
    }

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
}
