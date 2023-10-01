package com.github.ducoral.formula;

import java.util.Locale;
import java.util.Map;

public class Tests {

    public static void main(String[] args) throws Exception {

//        Locale.setDefault(Locale.ENGLISH);
//        Locale.setDefault(new Locale("es", "ES"));

        var formula = Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_LOGICAL)
                .configure(FormulaDefaults.OPERATIONS_DEFAULT)
                .function(new FunctionDefinition("soma",
                        params -> params.get(0).asBigInteger().add(params.get(1).asBigInteger())))
                .build();

        String input = "15 + 12 * 30 * 15  soma(15, 25) + aa";
        var result = formula.evaluate(input, Map.of("aa", 40));

        if (result.isOK())
            System.out.println(result.value().asString() + "\n" + formula.explain(input).value());
        else {
            System.out.println(result.formattedErrorMessage());
            result.exception().printStackTrace();
        }
    }
}
