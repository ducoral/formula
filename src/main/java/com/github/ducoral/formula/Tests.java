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

        String input = "teste(a * 3) * 3 + oloco_fi('teste' + outro, 4 + 4 * 5 >= 5 + 7)";
        Expression expression = formula.parse(input);
        System.out.println(expression);

        System.out.println(formula.explain(input));

    }
}
