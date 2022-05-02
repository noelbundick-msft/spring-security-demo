package com.example.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SystemRolePermissionEvaluatorTests {
  @Test
  void deniesByDefault() {
    SystemRolePermissionEvaluator evaluator = new SystemRolePermissionEvaluator();

    assertFalse(evaluator.hasPermission(null, new Object(), "read"));
    assertFalse(evaluator.hasPermission(null, 1, "Object", "read"));
  }

  @Test
  void allowsSystemRole() throws Exception {
    try (SystemSecurityContext context = new SystemSecurityContext()) {
      SystemRolePermissionEvaluator evaluator = new SystemRolePermissionEvaluator();
      assertTrue(evaluator.hasPermission(context.getAuthentication(), new Object(), "read"));
      assertTrue(evaluator.hasPermission(context.getAuthentication(), 1, "Object", "read"));
    }
  }
}
