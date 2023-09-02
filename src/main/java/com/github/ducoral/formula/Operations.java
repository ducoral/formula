package com.github.ducoral.formula;

import java.util.function.Consumer;

import static com.github.ducoral.formula.Formula.*;

public class Operations {

    public static final Operator PLUS = new Operator("+", 10);
    public static final Operator MINUS = new Operator("-", 10);
    public static final Operator ASTERISK = new Operator("*", 10);
    public static final Operator SLASH = new Operator("/", 10);

    public static final Operator EQUAL = new Operator("=", 20);
    public static final Operator NOT_EQUAL = new Operator("!=", 20);
    public static final Operator GREATER_THAN = new Operator(">", 20);
    public static final Operator GREATER_THAN_OR_EQUAL = new Operator(">=", 20);
    public static final Operator LESS_THAN = new Operator("<", 20);
    public static final Operator LESS_THAN_OR_EQUAL = new Operator("<=", 20);

    public static final Operator AND = new Operator("&&", 30);
    public static final Operator OR = new Operator("||", 30);

    public static final Consumer<Builder> NUMBER_OPERATIONS = new NumberOperations();
    public static final Consumer<Builder> STRING_OPERATIONS = new StringOperations();
    public static final Consumer<Builder> LOGICAL_OPERATIONS = new LogicalOperations();
}
