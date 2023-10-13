package integration;

import com.github.ducoral.formula.Expression;
import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Position;
import com.github.ducoral.formula.Precedence;
import com.github.ducoral.formula.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

import static com.github.ducoral.formula.Expression.BinaryOperation;
import static com.github.ducoral.formula.Expression.Empty;
import static com.github.ducoral.formula.Expression.FunctionCall;
import static com.github.ducoral.formula.Expression.Identifier;
import static com.github.ducoral.formula.Expression.NumberLiteral;
import static com.github.ducoral.formula.Expression.StringLiteral;
import static com.github.ducoral.formula.Expression.UnaryOperation;
import static com.github.ducoral.formula.Formula.Builder;
import static com.github.ducoral.formula.Formula.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FormulaTest {

    static final String UNARY_STR = "unarily operated[%s %s]";
    static final String BINARY_STR = "binary operated[%s %s %s]";

    final Operator sharp = new Operator("#", new Precedence(0));
    final Operator at = new Operator("@", new Precedence(1));

    interface TypeOne {
        String value();

        static TypeOne of(String value) {
            return () -> value;
        }
    }

    interface TypeTwo extends TypeOne {
        static TypeTwo of(String value) {
            return () -> value;
        }
    }

    final Operation unaryForTypeOne = new Operation(Number.class, sharp, (operands, chain) ->
            TypeOne.of(String.format(UNARY_STR, sharp, operands.right())));

    final Operation binaryFoTypeOne = new Operation(Number.class, sharp, (operands, chain) -> {
        if (!operands.right().isType(Number.class))
            return chain.chain(operands);
        return TypeOne.of(String.format(BINARY_STR, operands.left(), sharp, operands.right()));
    });

    final Operation unaryForTypeTwo = new Operation(BigInteger.class, at, (operands, chain) ->
            TypeTwo.of(String.format(UNARY_STR, at, operands.right())));

    final Operation binaryFoTypeTwo = new Operation(BigInteger.class, at, (operands, chain) -> {
        if (!operands.right().isType(BigInteger.class))
            return chain.chain(operands);
        return TypeTwo.of(String.format(BINARY_STR, operands.left(), at, operands.right()));
    });

    final Builder builder = builder()
            .unaryOperation(unaryForTypeOne)
            .unaryOperation(unaryForTypeTwo)
            .binaryOperation(binaryFoTypeOne)
            .binaryOperation(binaryFoTypeTwo);

    static Result<Expression> assertParse(Formula formula, String input) {
        var result = formula.parse(input);
        if (!result.isOK())
            fail(result.exception().getMessage());
        return result;
    }

    static <T extends Expression> T assertValueType(Result<Expression> result, Class<T> type) {
        if (type.isInstance(result.value()))
            return type.cast(result.value());
        throw new AssertionFailedError();
    }

    Position pos(int index) {
        return new Position(index, 0, index);
    }

    @Nested
    class ParseTest {

        Formula formula;

        @BeforeEach
        void beforeEach() {
            formula = builder.build();
        }

        Result<Expression> assertParse(String input) {
            return FormulaTest.assertParse(formula, input);
        }

        void assertNumberLiteral(String lexeme, Function<String, Object> numberConstructor) {
            assertNumberLiteral(lexeme, lexeme, numberConstructor);
        }

        void assertNumberLiteral(String lexeme, String expectedString, Function<String, Object> numberConstructor) {
            var number = assertValueType(assertParse(lexeme), NumberLiteral.class);
            assertEquals(Position.ZERO, number.position());
            assertEquals(numberConstructor.apply(lexeme), number.value());
            assertEquals(expectedString, number.toString());
        }

        void assertStringLiteral(String lexeme, String expected) {
            var string = assertValueType(assertParse(lexeme), StringLiteral.class);
            assertEquals(Position.ZERO, string.position());
            assertEquals(expected, string.value());
            assertEquals("\"" + expected + "\"", string.toString());
        }

        void assertIdentifier(String name) {
            var identifier = assertValueType(assertParse(name), Identifier.class);
            assertEquals(Position.ZERO, identifier.position());
            assertEquals(name, identifier.name());
            assertEquals(name, identifier.toString());
        }

        @Test
        void testEmpty() {
            var empty = assertValueType(assertParse(""), Empty.class);
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
            var unaryOperation = assertValueType(assertParse("# 1234"), UnaryOperation.class);
            assertEquals(Position.ZERO, unaryOperation.position());
            assertEquals(sharp.lexeme(), unaryOperation.operator());
            assertEquals("# 1234", unaryOperation.toString());
            if (unaryOperation.right() instanceof NumberLiteral number) {
                assertEquals(pos(2), number.position());
                assertEquals(new BigInteger("1234"), number.value());
            } else
                fail("NumberLiteral expected");

            unaryOperation = assertValueType(assertParse("@ 1234.1234"), UnaryOperation.class);
            assertEquals(Position.ZERO, unaryOperation.position());
            assertEquals(at.lexeme(), unaryOperation.operator());
            assertEquals("@ 1234.1234", unaryOperation.toString());
            if (unaryOperation.right() instanceof NumberLiteral number) {
                assertEquals(pos(2), number.position());
                assertEquals(new BigDecimal("1234.1234"), number.value());
            } else
                fail("NumberLiteral expected");
        }

        @Test
        void testBinaryOperation() {
            var binaryOperation = assertValueType(assertParse("1234 # 1234"), BinaryOperation.class);
            assertEquals(Position.ZERO, binaryOperation.position());
            assertEquals(sharp.lexeme(), binaryOperation.operator());
            assertEquals("1234 # 1234", binaryOperation.toString());

            if (binaryOperation.left() instanceof NumberLiteral number) {
                assertEquals(pos(0), number.position());
                assertEquals(new BigInteger("1234"), number.value());
            } else
                fail("NumberLiteral expected");

            if (binaryOperation.right() instanceof NumberLiteral number) {
                assertEquals(pos(7), number.position());
                assertEquals(new BigInteger("1234"), number.value());
            } else
                fail("NumberLiteral expected");

            binaryOperation = assertValueType(assertParse("1234.1234 @ 1234.1234"), BinaryOperation.class);
            assertEquals(Position.ZERO, binaryOperation.position());
            assertEquals(at.lexeme(), binaryOperation.operator());
            assertEquals("1234.1234 @ 1234.1234", binaryOperation.toString());

            if (binaryOperation.left() instanceof NumberLiteral number) {
                assertEquals(pos(0), number.position());
                assertEquals(new BigDecimal("1234.1234"), number.value());
            } else
                fail("NumberLiteral expected");

            if (binaryOperation.right() instanceof NumberLiteral number) {
                assertEquals(pos(12), number.position());
                assertEquals(new BigDecimal("1234.1234"), number.value());
            } else
                fail("NumberLiteral expected");
        }

        @Test
        void testFunctionCall() {
            var functionCall = assertValueType(assertParse("fn()"), FunctionCall.class);
            assertEquals(Position.ZERO, functionCall.position());
            assertEquals("fn", functionCall.name());
            assertEquals(List.of(), functionCall.parameters());
            assertEquals("fn()", functionCall.toString());

            //                                                          1         2         3         4
            //                                                0123456789012345678901234567890123456789012
            functionCall = assertValueType(assertParse("fn(1, 1.2, 'str1', `str2`, \"str3\", ident)"), FunctionCall.class);
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

            //                                                          1         2         3         4
            //                                                01234567890123456789012345678901234567890123
            functionCall = assertValueType(assertParse("fn(fn2(1), fn3(fn4(1.2), fn5(`str`), ident))"), FunctionCall.class);
            assertEquals(Position.ZERO, functionCall.position());
            assertEquals("fn", functionCall.name());
            var fn5 = new FunctionCall(pos(25), "fn5", List.of(new StringLiteral(pos(29), "str")));
            var fn4 = new FunctionCall(pos(15), "fn4", List.of(new NumberLiteral(pos(19), new BigDecimal("1.2"))));
            var fn3 = new FunctionCall(pos(11), "fn3", List.of(fn4, fn5, new Identifier(pos(37), "ident")));
            var fn2 = new FunctionCall(pos(3), "fn2", List.of(new NumberLiteral(pos(7), new BigInteger("1"))));
            assertEquals(List.of(fn2, fn3), functionCall.parameters());
            assertEquals("fn(fn2(1), fn3(fn4(1.2), fn5(\"str\"), ident))", functionCall.toString());
        }
    }

    @Nested
    class VisitorTest {

        final Stack<Expression> visitedStack = new Stack<>();

        class Visitor implements Expression.Visitor {

            @Override
            public void visit(NumberLiteral numberLiteral) {
                visitedStack.push(numberLiteral);
            }

            @Override
            public void visit(StringLiteral stringLiteral) {
                visitedStack.push(stringLiteral);
            }

            @Override
            public void visit(Identifier identifier) {
                visitedStack.push(identifier);
            }

            @Override
            public void visit(UnaryOperation unaryOperation) {
                visitedStack.push(unaryOperation);
            }

            @Override
            public void visit(BinaryOperation binaryOperation) {
                visitedStack.push(binaryOperation);
            }

            @Override
            public void visit(FunctionCall functionCall) {
                visitedStack.push(functionCall);
            }

        }
        Formula formula;

        @BeforeEach
        void beforeEach() {
            formula = builder.build();
        }

        void assertVisitor(String input) {
            assertValueType(assertParse(formula, input), Expression.class)
                    .accept(new Visitor());
        }

        @Test
        void testEmptyVisited() {
            assertVisitor("");
            assertTrue(visitedStack.empty());
        }

        @Test
        void testNumberLiteralVisited() {
            assertVisitor("123");
            assertEquals(new NumberLiteral(pos(0), new BigInteger("123")), visitedStack.pop());

            assertVisitor("123.123");
            assertEquals(new NumberLiteral(pos(0), new BigDecimal("123.123")), visitedStack.pop());
        }

        @Test
        void testStringLiteralVisited() {
            assertVisitor("'abc'");
            assertEquals(new StringLiteral(pos(0), "abc"), visitedStack.pop());
        }

        @Test
        void testIdentifierVisited() {
            assertVisitor("abc");
            assertEquals(new Identifier(pos(0), "abc"), visitedStack.pop());
        }

        @Test
        void testUnaryOperationVisited() {
            assertVisitor("@ 1");
            var right = new NumberLiteral(pos(2), new BigInteger("1"));
            assertEquals(new UnaryOperation(pos(0),"@", right), visitedStack.pop());
        }

        @Test
        void testBinaryOperationVisited() {
            assertVisitor("1 @ 1");
            var left = new NumberLiteral(pos(0), new BigInteger("1"));
            var right = new NumberLiteral(pos(4), new BigInteger("1"));
            assertEquals(new BinaryOperation(pos(0),left, "@", right), visitedStack.pop());
        }

        @Test
        void testFunctionCallVisited() {
            assertVisitor("call()");
            assertEquals(new FunctionCall(pos(0), "call", List.of()), visitedStack.pop());
        }
    }

    @Nested
    class EvaluatorTest {

        Formula formula;

        @BeforeEach
        void beforeEach() {
            formula = builder.build();
        }

        @Test
        void testEvaluateEmpty() {
            var result = formula.evaluate("");
            assertTrue(result.isOK());
            assertTrue(result.value().isNull());
        }

        @Test
        void testEvaluateNumber() {
            var result = formula.evaluate("123");
            assertTrue(result.isOK());
            assertFalse(result.value().isNull());
            assertTrue(result.value().isNumber());
            assertTrue(result.value().isInteger());
            assertTrue(result.value().isTruthful());
            assertTrue(result.value().isType(BigInteger.class));
            assertFalse(result.value().isString());
            assertEquals(new BigInteger("123"), result.value().asBigInteger());
            assertEquals(new BigInteger("123"), result.value().asType(BigInteger.class));
            assertEquals(BigInteger.class, result.value().getType());
            assertEquals("java.math.BigInteger", result.value().getTypeName());

            result = formula.evaluate("123.123");
            assertTrue(result.isOK());
            assertFalse(result.value().isNull());
            assertTrue(result.value().isNumber());
            assertFalse(result.value().isInteger());
            assertTrue(result.value().isTruthful());
            assertTrue(result.value().isType(BigDecimal.class));
            assertFalse(result.value().isString());
            assertEquals(new BigDecimal("123.123"), result.value().asBigDecimal());
            assertEquals(new BigDecimal("123.123"), result.value().asType(BigDecimal.class));
            assertEquals(BigDecimal.class, result.value().getType());
            assertEquals("java.math.BigDecimal", result.value().getTypeName());
        }
    }
}
