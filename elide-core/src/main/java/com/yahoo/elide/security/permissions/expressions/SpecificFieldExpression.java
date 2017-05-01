/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.security.permissions.expressions;

import com.yahoo.elide.security.permissions.ExpressionResult;
import com.yahoo.elide.security.permissions.PermissionCondition;
import lombok.Getter;

import java.util.Optional;

import static com.yahoo.elide.security.permissions.ExpressionResult.PASS;

/**
 * Expression for joining specific fields.
 *
 * That is, this evaluates security while giving precedence to the annotation on a particular field over
 * the annotation at the entity- or package-level.
 */
public class SpecificFieldExpression implements Expression {
    private final Expression entityExpression;
    private final Optional<Expression> fieldExpression;
    @Getter private final PermissionCondition condition;

    public SpecificFieldExpression(final PermissionCondition condition,
                                   final Expression entityExpression,
                                   final Expression fieldExpression) {
        this.condition = condition;
        this.entityExpression = entityExpression;
        this.fieldExpression = Optional.ofNullable(fieldExpression);
    }

    @Override
    public ExpressionResult evaluate(EvaluationMode mode) {
        if (!fieldExpression.isPresent()) {
            ExpressionResult entityResult = (entityExpression == null) ? PASS : entityExpression.evaluate(mode);
            return entityResult;
        } else {
            ExpressionResult fieldResult = fieldExpression.get().evaluate(mode);
            return fieldResult;
        }
    }


    @Override
    public String toString() {
        if (entityExpression == null && !fieldExpression.isPresent()) {
            return String.format("%s FOR EXPRESSION []", condition);
        }

        if (!fieldExpression.isPresent()) {
             return String.format(
                    "%s FOR EXPRESSION [ENTITY(%s)]",
                    condition,
                    entityExpression);
        }

        return String.format(
                    "%s FOR EXPRESSION [FIELD(%s)]",
                    condition,
                    fieldExpression.get());
    }
}
