package com.github.noelbundick_msft.security;

import lombok.Data;

@Data
public class AuthRoleAssignment {
  String scope;
  AuthRole role;
}
