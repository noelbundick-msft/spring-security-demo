package com.example.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

class AuthZServicePermissionEvaluatorTests {

  static String TEST_AUTHZ_URL = "http://localhost:3000/users/{userId}.json";
  static String TEST_GLOBAL_ADMIN = "global_admin";

  AuthZServicePermissionEvaluator evaluator;

  @BeforeEach
  void init() {
    evaluator = createEvaluator();
  }

  @AfterEach
  void teardown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void throwsForNullAuthorizedClientService() {
    assertThrows(IllegalArgumentException.class, () -> {
      new AuthZServicePermissionEvaluator(null, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
    });
  }

  @Test
  void canCreateACustomPermissionEvaluator() {
    var registration = ClientRegistration.withRegistrationId("pingidentity")
        .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
        .build();
    var clientRegistrationRepository = new InMemoryClientRegistrationRepository(registration);
    var authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    var evaluator = new AuthZServicePermissionEvaluator(authorizedClientService, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
    assertNotNull(evaluator);
  }

  @Test
  void failsForNullAuthentication() {
    assertFalse(evaluator.hasPermission(null, null, "none"));
    assertFalse(evaluator.hasPermission(null, 0, "none", "none"));
  }

  AuthZServicePermissionEvaluator createEvaluator() {
    var registration = ClientRegistration.withRegistrationId("pingidentity")
        .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
        .build();
    var clientRegistrationRepository = new InMemoryClientRegistrationRepository(registration);
    var authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    return new AuthZServicePermissionEvaluator(authorizedClientService, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
  }

  Authentication createAuthentication(String role) {
    return new TestingAuthenticationToken("system", null, String.format("ROLE_%s", role));
  }
}
