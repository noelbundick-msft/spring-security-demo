package com.github.noelbundick_msft.security;

import lombok.Data;

@Data
public class AuthRoleAssignment {
  private String scope;
  private AuthRole role;
}
