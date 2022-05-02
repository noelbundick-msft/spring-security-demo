package com.example.webapi;

import static org.junit.jupiter.api.Assertions.*;

import com.example.security.SystemSecurityContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@SpringBootTest
public class ThingRepositoryTests {

  @Autowired
  private ThingRepository repository;

  @Test
  void getAll_DeniesUnauthenticatedUsers() {
    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> repository.getById(Long.valueOf(1)));
  }

  @Test
  void getAll_AllowsSystemUsers() {
    try (var context = new SystemSecurityContext()) {
      assertDoesNotThrow(() -> repository.findById(Long.valueOf(1)));
    }
  }
}
