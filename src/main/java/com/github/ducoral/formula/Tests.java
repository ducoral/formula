package com.github.ducoral.formula;

public class Tests {

    public static void main(String[] args) throws Exception {

        var formula = Formula.builder()
                .configure(FormulaDefaults.DEFAULT_OPERATIONS)
                .function(new FunctionDefinition("soma",
                        params -> params.getAsBigInteger(0).add(params.getAsBigInteger(1))))
                .build();

        formula.explorer();
    }
}
