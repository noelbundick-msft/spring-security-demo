package com.github.noelbundick_msft.security;

import java.util.List;

import lombok.Data;

@Data
public class AuthZResponse {
  String name;
  List<AuthZRoleAssignment> roleAssignments;
}
