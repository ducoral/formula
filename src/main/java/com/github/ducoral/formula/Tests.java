package com.github.ducoral.formula;

public class Tests {

    public static void main(String[] args) throws Exception {

        var formula = Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_LOGICAL)
                .configure(FormulaDefaults.OPERATIONS_DEFAULT)
                .function(new FunctionDefinition("soma",
                        params -> params.get(0).asBigInteger().add(params.get(1).asBigInteger())))
                .build();

        formula.explorer();
    }
}
