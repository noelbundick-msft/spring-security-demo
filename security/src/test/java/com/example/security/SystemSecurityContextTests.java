package com.example.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

public class SystemSecurityContextTests {
  @Test
  void defaultHasNoAuthentication() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);
  }

  @Test
  void contextSetsSystemRoleAuthentication() {
    try (SystemSecurityContext context = new SystemSecurityContext()) {
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      assertNotNull(authentication);
      assertEquals("system", authentication.getName());
      assertEquals(1, authentication.getAuthorities().size());
      assertEquals("ROLE_SYSTEM", authentication.getAuthorities().iterator().next().getAuthority());
    }
  }

  @Test
  void getAuthenticationReturnsSecurityContextHolderAuthentication() {
    try (SystemSecurityContext context = new SystemSecurityContext()) {
      assertEquals(context.getAuthentication(), SecurityContextHolder.getContext().getAuthentication());
    }
  }

  @Test
  void resetsAuthenticationAfterClose() {
    try (SystemSecurityContext context = new SystemSecurityContext()) {
      assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
