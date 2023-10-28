package com.github.ducoral.formula;

import java.util.Map;

public class Tests {

    public static void main(String[] args) throws Exception {

        var formula = Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_LOGICAL)
                .configure(FormulaDefaults.OPERATIONS_DEFAULT)
                .function(new FunctionDefinition("soma",
                        params -> params.get(0).asBigInteger().add(params.get(1).asBigInteger())))
                .build();

        String input = "?";
        var result = formula.evaluate(input, Map.of("aa", 40));

        if (result.isOK())
            System.out.println(result.value().asString() + "\n" + formula.explain(input).value());
        else {
            System.out.println(result.formattedErrorMessage());
            result.exception().printStackTrace();
        }
    }
}
