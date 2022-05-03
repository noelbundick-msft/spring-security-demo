package com.example.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class CompositePermissionEvaluator implements PermissionEvaluator {

    private List<PermissionEvaluator> evaluators;

    public CompositePermissionEvaluator(PermissionEvaluator... evaluators) {
        this.evaluators = Arrays.asList(evaluators);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        for (PermissionEvaluator evaluator : evaluators) {
            if (evaluator.hasPermission(authentication, targetDomainObject, permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {
        for (PermissionEvaluator evaluator : evaluators) {
            if (evaluator.hasPermission(authentication, targetId, targetType, permission)) {
                return true;
            }
        }

        return false;
    }
}
