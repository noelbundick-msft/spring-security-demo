package com.github.noelbundick_msft.security;

import java.util.List;

import lombok.Data;

@Data
public class AuthUserDetails {
  private String userPrincipalId;
  private String userPrincipalOrgId;
  private List<AuthRoleAssignment> roleAssignments;
}
