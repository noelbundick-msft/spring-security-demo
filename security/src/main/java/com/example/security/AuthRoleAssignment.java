package com.example.security;

import lombok.Data;

@Data
public class AuthRoleAssignment {
  private String scope;
  private AuthRole role;
}
