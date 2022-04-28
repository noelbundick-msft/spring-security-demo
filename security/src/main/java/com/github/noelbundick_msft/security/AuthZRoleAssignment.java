package com.github.noelbundick_msft.security;

import lombok.Data;

@Data
public class AuthZRoleAssignment {
  String role;
  String scope;
}
