package com.github.ducoral.formula;

import java.awt.*;

import static com.github.ducoral.formula.FormulaDefaults.*;

public class Tests {

    public static void main(String[] args) {

        var formula = Formula.builder()
                .configure(DEFAULT_OPERATIONS)
                .function(new FunctionDefinition("soma",
                        params -> params.getAsBigInteger(0).add(params.getAsBigInteger(1))))
                .build();

//        String input = "- - - (1 - 3)";
//        System.out.println(formula.explain(input));

        formula.explorer();


//        var prop = System.getProperties();
//        for (var key : prop.keySet())
//            System.out.println(key + ":" + prop.get(key));


    }
}
