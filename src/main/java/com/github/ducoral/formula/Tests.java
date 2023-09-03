package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static com.github.ducoral.formula.FormulaUtils.*;

public class Tests {

    public static void main(String[] args) {

        var dobra = new Operator("dobra", 0);
        var mais = new Operator("mais", Operations.PLUS.precedence());

        var formula = Formula.builder()
                .configure(Operations.NUMBER_OPERATIONS)
                .configure(Operations.STRING_OPERATIONS)
                .configure(Operations.LOGICAL_OPERATIONS)
                .function(new FunctionDefinition("soma", parameters -> {
                    var left = (BigInteger) parameters.getValue(0);
                    var right = (BigInteger) parameters.getValue(1);
                    return left.add(right);
                }))
                .unaryOperation(new Operation(Number.class, dobra,
                        (operands, chain) -> asBigDecimal(operands.right()).multiply(new BigDecimal(2))))
                .binaryOperation(new Operation(Number.class, mais, ((operands, chain) -> {
                    if (!isNumber(operands.right()))
                        chain.chain(operands);
                    return asBigDecimal(operands.left()).add(asBigDecimal(operands.right()));
                })))
                .build();

        var result = formula.evaluate("3 + dobra3 mais 4", Map.of("x", 2, "dobra3", 5));
        System.out.println(result);
    }
}
