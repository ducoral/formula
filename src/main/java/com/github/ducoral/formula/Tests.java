package com.github.ducoral.formula;

import java.math.BigInteger;
import java.util.Map;

public class Tests {

    public static void main(String[] args) {

        var formula = Formula.builder()
                .configure(Operations.NUMBER_OPERATIONS)
                .configure(Operations.STRING_OPERATIONS)
                .configure(Operations.LOGICAL_OPERATIONS)
                .function(new FunctionDefinition("soma", parameters -> {
                    var left = (BigInteger) parameters.getValue(0);
                    var right = (BigInteger) parameters.getValue(1);
                    return left.add(right);
                }))
                .build();

        var result = formula.evaluate("x+3 + soma(10,2)", Map.of("x", 2));
        System.out.println(result);
    }
}
