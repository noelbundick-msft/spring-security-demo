package com.example.security;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class CompositePermissionEvaluator implements PermissionEvaluator  {

    private List<PermissionEvaluator> evaluators;

    public CompositePermissionEvaluator(List<PermissionEvaluator> evaluators)
    {
      super();
      this.evaluators = evaluators;
    }
  
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission)
    {
      return evaluators.stream()
          .map(ev -> ev.hasPermission(authentication, targetDomainObject, permission))
          .reduce(Boolean::logicalOr)
          .orElse(false);
    }
  
    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable targetId,
        String targetType,
        Object permission)
    {
        return evaluators.stream()
            .map(ev -> ev.hasPermission(authentication, targetId, targetType, permission))
            .reduce(Boolean::logicalOr)
            .orElse(false);
    }
}
