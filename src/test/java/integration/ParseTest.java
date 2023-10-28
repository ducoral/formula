package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Position;
import com.github.ducoral.formula.Precedence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.github.ducoral.formula.Expression.BinaryOperation;
import static com.github.ducoral.formula.Expression.Empty;
import static com.github.ducoral.formula.Expression.FunctionCall;
import static com.github.ducoral.formula.Expression.Identifier;
import static com.github.ducoral.formula.Expression.NumberLiteral;
import static com.github.ducoral.formula.Expression.StringLiteral;
import static com.github.ducoral.formula.Expression.UnaryOperation;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_CHARACTER;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_DECIMAL_NUMBER;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_ESCAPE;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_TOKEN;
import static com.github.ducoral.formula.FormulaExceptionType.STRING_NOT_CLOSED_CORRECTLY;
import static com.github.ducoral.formula.FormulaExceptionType.UNEXPECTED_TOKEN;
import static integration.TestUtils.assertParse;
import static integration.TestUtils.assertValueType;
import static integration.TestUtils.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class ParseTest {

    final Operator op123 = new Operator("op123", new Precedence(0));
    final Operator atAt = new Operator("@@", op123.precedence().after(1));

    Formula formula;

    @BeforeEach
    void beforeEach() {
        formula = Formula.builder()
                .unaryOperation(new Operation(Number.class, op123, (operands, chain) -> null))
                .unaryOperation(new Operation(Number.class, atAt, (operands, chain) -> null))
                .binaryOperation(new Operation(Number.class, op123, (operands, chain) -> null))
                .binaryOperation(new Operation(Number.class, atAt, (operands, chain) -> null))
                .build();
    }

    void assertNumberLiteral(String lexeme, Function<String, Object> numberConstructor) {
        assertNumberLiteral(lexeme, lexeme, numberConstructor);
    }

    void assertNumberLiteral(String lexeme, String expectedString, Function<String, Object> numberConstructor) {
        var number = assertValueType(assertParse(formula, lexeme), NumberLiteral.class);
        assertEquals(Position.ZERO, number.position());
        assertEquals(numberConstructor.apply(lexeme), number.value());
        assertEquals(expectedString, number.toString());
    }

    void assertStringLiteral(String lexeme, String expected) {
        var string = assertValueType(assertParse(formula, lexeme), StringLiteral.class);
        assertEquals(Position.ZERO, string.position());
        assertEquals(expected, string.value());
        assertEquals("\"" + expected + "\"", string.toString());
    }

    void assertIdentifier(String name) {
        var identifier = assertValueType(assertParse(formula, name), Identifier.class);
        assertEquals(Position.ZERO, identifier.position());
        assertEquals(name, identifier.name());
        assertEquals(name, identifier.toString());
    }

    @Test
    void testEmpty() {
        var empty = assertValueType(assertParse(formula, ""), Empty.class);
        assertEquals(new Position(-1, -1, -1), empty.position());
        assertEquals("<EMPTY>", empty.toString());
    }

    @Test
    void testNumberLiteral() {
        assertNumberLiteral("0", BigInteger::new);
        assertNumberLiteral("1", BigInteger::new);
        assertNumberLiteral("1234567890", BigInteger::new);
        assertNumberLiteral("12345678901234567890", BigInteger::new);
        assertNumberLiteral("123456789012345678901234567890", BigInteger::new);
        assertNumberLiteral("1234567890123456789012345678901234567890", BigInteger::new);
        assertNumberLiteral("12345678901234567890123456789012345678901234567890", BigInteger::new);
        assertNumberLiteral("123456789012345678901234567890123456789012345678901234567890", BigInteger::new);

        assertNumberLiteral("0.0", BigDecimal::new);
        assertNumberLiteral("0.", "0", BigDecimal::new);
        assertNumberLiteral(".0", "0.0", BigDecimal::new);
        assertNumberLiteral("0.1e-1234", "1E-1235", BigDecimal::new);
        assertNumberLiteral("0.1e+1234", "1E+1233", BigDecimal::new);
        assertNumberLiteral("1.E+123", "1E+123", BigDecimal::new);
        assertNumberLiteral("1.0E+123", "1.0E+123", BigDecimal::new);
        assertNumberLiteral(".1E+123", "1E+122", BigDecimal::new);
        assertNumberLiteral("1E+123", "1E+123", BigDecimal::new);
    }

    @Test
    void testStringLiteral() {
        assertStringLiteral("\"a'b`c\\\\d\"", "a'b`c\\d");
        assertStringLiteral("`a\"b'c\\\\d`", "a\"b'c\\d");
        assertStringLiteral("'a\"b`c\\\\d'", "a\"b`c\\d");
    }

    @Test
    void testIdentifier() {
        assertIdentifier("abc");
        assertIdentifier("abc123");
        assertIdentifier("_AbC$123_");
    }

    @Test
    void testUnaryOperation() {
        var unaryOperation = assertValueType(assertParse(formula, "op123 1234"), UnaryOperation.class);
        assertEquals(Position.ZERO, unaryOperation.position());
        assertEquals(op123.lexeme(), unaryOperation.operator());
        assertEquals("op123 1234", unaryOperation.toString());
        if (unaryOperation.right() instanceof NumberLiteral number) {
            assertEquals(pos(6), number.position());
            assertEquals(new BigInteger("1234"), number.value());
        } else
            fail("NumberLiteral expected");

        unaryOperation = assertValueType(assertParse(formula, "@@ 1234.1234"), UnaryOperation.class);
        assertEquals(Position.ZERO, unaryOperation.position());
        assertEquals(atAt.lexeme(), unaryOperation.operator());
        assertEquals("@@ 1234.1234", unaryOperation.toString());
        if (unaryOperation.right() instanceof NumberLiteral number) {
            assertEquals(pos(3), number.position());
            assertEquals(new BigDecimal("1234.1234"), number.value());
        } else
            fail("NumberLiteral expected");
    }

    @Test
    void testBinaryOperation() {
        var binaryOperation = assertValueType(assertParse(formula, "1234 op123 1234"), BinaryOperation.class);
        assertEquals(Position.ZERO, binaryOperation.position());
        assertEquals(op123.lexeme(), binaryOperation.operator());
        assertEquals("1234 op123 1234", binaryOperation.toString());

        if (binaryOperation.left() instanceof NumberLiteral number) {
            assertEquals(pos(0), number.position());
            assertEquals(new BigInteger("1234"), number.value());
        } else
            fail("NumberLiteral expected");

        if (binaryOperation.right() instanceof NumberLiteral number) {
            assertEquals(pos(11), number.position());
            assertEquals(new BigInteger("1234"), number.value());
        } else
            fail("NumberLiteral expected");

        binaryOperation = assertValueType(assertParse(formula, "(1234.1234 @@ 1234.1234)"), BinaryOperation.class);
        assertEquals(pos(1), binaryOperation.position());
        assertEquals(atAt.lexeme(), binaryOperation.operator());
        assertEquals("1234.1234 @@ 1234.1234", binaryOperation.toString());

        if (binaryOperation.left() instanceof NumberLiteral number) {
            assertEquals(pos(1), number.position());
            assertEquals(new BigDecimal("1234.1234"), number.value());
        } else
            fail("NumberLiteral expected");

        if (binaryOperation.right() instanceof NumberLiteral number) {
            assertEquals(pos(14), number.position());
            assertEquals(new BigDecimal("1234.1234"), number.value());
        } else
            fail("NumberLiteral expected");
    }

    @Test
    void testFunctionCall() {
        var functionCall = assertValueType(assertParse(formula, "fn()"), FunctionCall.class);
        assertEquals(Position.ZERO, functionCall.position());
        assertEquals("fn", functionCall.name());
        assertEquals(List.of(), functionCall.parameters());
        assertEquals("fn()", functionCall.toString());

        //                                                                   1         2         3         4
        //                                                         0123456789012345678901234567890123456789012
        functionCall = assertValueType(assertParse(formula, "fn(1, 1.2, 'str1', `str2`, \"str3\", ident)"), FunctionCall.class);
        assertEquals(Position.ZERO, functionCall.position());
        assertEquals("fn", functionCall.name());
        var parameters = List.of(
                new NumberLiteral(pos(3), new BigInteger("1")),
                new NumberLiteral(pos(6), new BigDecimal("1.2")),
                new StringLiteral(pos(11), "str1"),
                new StringLiteral(pos(19), "str2"),
                new StringLiteral(pos(27), "str3"),
                new Identifier(pos(35), "ident"));
        assertEquals(parameters, functionCall.parameters());
        assertEquals("fn(1, 1.2, \"str1\", \"str2\", \"str3\", ident)", functionCall.toString());

        //                                                                   1         2         3         4
        //                                                         01234567890123456789012345678901234567890123
        functionCall = assertValueType(assertParse(formula, "fn(fn2(1), fn3(fn4(1.2), fn5(`str`), ident))"), FunctionCall.class);
        assertEquals(Position.ZERO, functionCall.position());
        assertEquals("fn", functionCall.name());
        var fn5 = new FunctionCall(pos(25), "fn5", List.of(new StringLiteral(pos(29), "str")));
        var fn4 = new FunctionCall(pos(15), "fn4", List.of(new NumberLiteral(pos(19), new BigDecimal("1.2"))));
        var fn3 = new FunctionCall(pos(11), "fn3", List.of(fn4, fn5, new Identifier(pos(37), "ident")));
        var fn2 = new FunctionCall(pos(3), "fn2", List.of(new NumberLiteral(pos(7), new BigInteger("1"))));
        assertEquals(List.of(fn2, fn3), functionCall.parameters());
        assertEquals("fn(fn2(1), fn3(fn4(1.2), fn5(\"str\"), ident))", functionCall.toString());
    }

    @Nested
    class ExceptionTest {

        @BeforeEach
        void beforeEach() {
            Locale.setDefault(Locale.ENGLISH);
        }

        @Test
        void testInvalidCharacter() {
            var result = formula.parse("?");
            assertFalse(result.isOK());
            assertEquals(INVALID_CHARACTER, result.exception().type);
            assertEquals("The character ? is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
        }

        @Test
        void testInvalidDecimalNumber() {
            var result = formula.parse(".");
            assertFalse(result.isOK());
            assertEquals(INVALID_DECIMAL_NUMBER, result.exception().type);
            assertEquals("The decimal number . is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse(".2E+");
            assertFalse(result.isOK());
            assertEquals(INVALID_DECIMAL_NUMBER, result.exception().type);
            assertEquals("The decimal number .2E+ is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("1.e+");
            assertFalse(result.isOK());
            assertEquals(INVALID_DECIMAL_NUMBER, result.exception().type);
            assertEquals("The decimal number 1.e+ is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("1 @ 2.E+");
            assertFalse(result.isOK());
            assertEquals(INVALID_DECIMAL_NUMBER, result.exception().type);
            assertEquals("The decimal number 2.E+ is invalid", result.exception().getMessage());
            assertEquals(pos(4), result.exception().position);
        }

        @Test
        void testInvalidEscape() {
            var result = formula.parse("\"\\'\"");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\' is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("\"\\`\"");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\` is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("'\\\"'");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\\" is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("'\\`'");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\` is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("`\\'`");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\' is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("`\\\"`");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\\" is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("\"\\1\"");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\1 is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("'\\2'");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\2 is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("`\\3`");
            assertFalse(result.isOK());
            assertEquals(INVALID_ESCAPE, result.exception().type);
            assertEquals("The escape \\3 is invalid", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
        }

        @Test
        void testStringNotClosedCorrectly() {
            var result = formula.parse("\"abc");
            assertFalse(result.isOK());
            assertEquals(STRING_NOT_CLOSED_CORRECTLY, result.exception().type);
            assertEquals("The string \"abc was not closed correctly", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("'abc");
            assertFalse(result.isOK());
            assertEquals(STRING_NOT_CLOSED_CORRECTLY, result.exception().type);
            assertEquals("The string 'abc was not closed correctly", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);

            result = formula.parse("`abc");
            assertFalse(result.isOK());
            assertEquals(STRING_NOT_CLOSED_CORRECTLY, result.exception().type);
            assertEquals("The string `abc was not closed correctly", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
        }

        @Test
        void testInvalidToken() {
            var result = formula.parse("1 @@ ,");
            assertFalse(result.isOK());
            assertEquals(INVALID_TOKEN, result.exception().type);
            assertEquals("The token , is invalid", result.exception().getMessage());
            assertEquals(pos(5), result.exception().position);
        }

        @Test
        void testUnexpectedToken() {
            var result = formula.parse("1 2");
            assertFalse(result.isOK());
            assertEquals(UNEXPECTED_TOKEN, result.exception().type);
            assertEquals(
                    "The token INTEGER was not expected. The token [EOF, OPERATOR] was expected in its place.",
                    result.exception().getMessage());
            assertEquals(pos(2), result.exception().position);

            result = formula.parse("(1 2");
            assertFalse(result.isOK());
            assertEquals(UNEXPECTED_TOKEN, result.exception().type);
            assertEquals(
                    "The token 2 was not expected. The token ) was expected in its place.",
                    result.exception().getMessage());
            assertEquals(pos(3), result.exception().position);
        }
    }
}
