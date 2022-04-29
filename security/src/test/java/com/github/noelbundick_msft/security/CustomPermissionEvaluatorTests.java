package com.github.noelbundick_msft.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

class CustomPermissionEvaluatorTests {

  static String TEST_AUTHZ_URL = "http://localhost:3000/users/{userId}.json";
  static String TEST_GLOBAL_ADMIN = "global_admin";

  @Test
  void throwsForNullAuthorizedClientService() {
    assertThrows(IllegalArgumentException.class, () -> {
      new CustomPermissionEvaluator(null, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
    });
  }

  @Test
  void canCreateACustomPermissionEvaluator() {
    var registration = ClientRegistration.withRegistrationId("pingidentity")
        .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
        .build();
    var clientRegistrationRepository = new InMemoryClientRegistrationRepository(registration);
    var authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    var evaluator = new CustomPermissionEvaluator(authorizedClientService, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
    assertNotNull(evaluator);
  }

  @Test
  void hasPermissionByObject_failsForNullAuthentication() {
    var evaluator = createEvaluator();
    assertFalse(evaluator.hasPermission(null, null, "none"));
  }

  @Test
  void hasPermissionById_failsForNullAuthentication() {
    var evaluator = createEvaluator();
    assertFalse(evaluator.hasPermission(null, 0, "none", "none"));
  }

  CustomPermissionEvaluator createEvaluator() {
    var registration = ClientRegistration.withRegistrationId("pingidentity")
        .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
        .build();
    var clientRegistrationRepository = new InMemoryClientRegistrationRepository(registration);
    var authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    return new CustomPermissionEvaluator(authorizedClientService, TEST_AUTHZ_URL, TEST_GLOBAL_ADMIN);
  }
}
