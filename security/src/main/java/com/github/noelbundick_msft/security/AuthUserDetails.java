package com.github.noelbundick_msft.security;

import java.util.List;

import lombok.Data;

@Data
public class AuthUserDetails {
  String userPrincipalId;
  String userPrincipalOrgId;
  List<AuthRoleAssignment> roleAssignments;
}
