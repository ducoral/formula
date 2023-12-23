package com.github.ducoral.formula;

import java.util.List;
import java.util.Map;

public class Tests {

    public static void main(String[] args) throws Exception {

        Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_DEFAULT)
                .build()
                .explorer();
    }
}
