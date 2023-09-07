package com.github.ducoral.formula;

import static com.github.ducoral.formula.FormulaDefaults.*;

public class Tests {

    public static void main(String[] args) {

        var formula = Formula.builder()
                .configure(NUMBER_OPERATIONS)
                .configure(STRING_OPERATIONS)
                .configure(LOGICAL_OPERATIONS)
                .function(new FunctionDefinition("soma",
                        params -> params.getAsBigInteger(0).add(params.getAsBigInteger(1))))
                .build();

        String input = "teste * 5 + 1 * 4 + 5 * 2 + opa";
        System.out.println(formula.parse(input));

        var tree = ExpressionAsTextBinaryTree.asTextBinaryTree(formula.parse(input));

        System.out.println(tree);
    }
}
