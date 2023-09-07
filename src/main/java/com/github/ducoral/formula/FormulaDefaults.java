package com.github.ducoral.formula;

import java.util.function.Consumer;

import static com.github.ducoral.formula.Formula.*;

public class FormulaDefaults {

    public static final Precedence UNARY = new Precedence(0);
    public static final Precedence ARITHMETIC_1 = new Precedence(10);
    public static final Precedence ARITHMETIC_2 = new Precedence(20);
    public static final Precedence RELATIONAL = new Precedence(30);
    public static final Precedence LOGICAL = new Precedence(40);

    public static final Operator ASTERISK = new Operator("*", ARITHMETIC_1);
    public static final Operator SLASH = new Operator("/", ARITHMETIC_1);
    public static final Operator PLUS = new Operator("+", ARITHMETIC_2);
    public static final Operator MINUS = new Operator("-", ARITHMETIC_2);

    public static final Operator EQUAL = new Operator("=", RELATIONAL);
    public static final Operator NOT_EQUAL = new Operator("!=", RELATIONAL);
    public static final Operator GREATER_THAN = new Operator(">", RELATIONAL);
    public static final Operator GREATER_THAN_OR_EQUAL = new Operator(">=", RELATIONAL);
    public static final Operator LESS_THAN = new Operator("<", RELATIONAL);
    public static final Operator LESS_THAN_OR_EQUAL = new Operator("<=", RELATIONAL);

    public static final Operator AND = new Operator("&&", LOGICAL);
    public static final Operator OR = new Operator("||", LOGICAL);

    public static final Consumer<Builder> NUMBER_OPERATIONS = new NumberOperations();
    public static final Consumer<Builder> STRING_OPERATIONS = new StringOperations();
    public static final Consumer<Builder> LOGICAL_OPERATIONS = new LogicalOperations();
}
