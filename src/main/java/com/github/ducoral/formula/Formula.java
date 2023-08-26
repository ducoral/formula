package com.github.ducoral.formula;

import com.github.ducoral.formula.parser.Parser;
import com.github.ducoral.formula.scanner.CharReader;
import com.github.ducoral.formula.scanner.Operators;

public class Formula {

    public static void main(String[] args) {
        var operatorInfo = Operators
                .builder()
                .operators("&&", "||")
                .operators("=", "!=", ">", ">=", "<", "<=")
                .operators("^")
                .operators("+", "-", "*", "/")
                .build();
        var charReader = new CharReader("(teste(5 + 3, 5) > 2 * (6 + 4))");
        var expression = Parser.parse(charReader, operatorInfo);
        System.out.println(expression);
    }
}
