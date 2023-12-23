package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.FunctionDefinition;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.OperationAction;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Parameters;
import com.github.ducoral.formula.Precedence;
import com.github.ducoral.formula.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.github.ducoral.formula.FormulaExceptionType.FUNCTION_NOT_DEFINED;
import static com.github.ducoral.formula.FormulaExceptionType.OPERATION_NOT_SUPPORTED;
import static com.github.ducoral.formula.FormulaExceptionType.UNEXPECTED_TOKEN;
import static integration.TestUtils.formatMessage;
import static integration.TestUtils.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluatorTest {

    final Operator sharp = new Operator("#", new Precedence(0));
    final Operator percent = new Operator("%", sharp.precedence().before(1));
    final Operator at = new Operator("@", sharp.precedence().after(1));

    Formula formula;

    @BeforeEach
    void beforeEach() {
        Function<Operator, OperationAction> unary = (operator) ->
                (operands, chain) -> "[" + operator + operands.right().asString() + "]";

        Function<Operator, OperationAction> binary = (operator) ->
                (operands, chain) -> operands.right().isString()
                        ? "[" + operands.left().asString() + operator + operands.right().asString() + "]"
                        : chain.chain(operands);

        Function<Parameters, Object> joinFunction = parameters ->
                parameters
                        .asList()
                        .stream()
                        .map(Value::asString)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

        Function<Parameters, Object> invalidFunctionImpl = parameters -> parameters.get(parameters.count() + 1);

        formula = Formula.builder()
                .unaryOperation(new Operation(String.class, sharp, unary.apply(sharp)))
                .unaryOperation(new Operation(String.class, percent, unary.apply(percent)))
                .unaryOperation(new Operation(String.class, at, unary.apply(at)))
                .binaryOperation(new Operation(String.class, sharp, binary.apply(sharp)))
                .binaryOperation(new Operation(String.class, percent, binary.apply(percent)))
                .binaryOperation(new Operation(String.class, at, binary.apply(at)))
                .function(new FunctionDefinition("join", joinFunction))
                .function(new FunctionDefinition("invalid", invalidFunctionImpl))
                .build();
    }

    @Test
    void testEvaluateEmpty() {
        var result = formula.evaluate("");
        assertTrue(result.isOK());
        assertTrue(result.value().isNull());
    }

    @Test
    void testEvaluateNullFromIdentifier() {
        var scope = new HashMap<String, Object>();
        scope.put("ident", null);

        var result = formula.evaluate("ident", scope);
        assertTrue(result.isOK());
        assertTrue(result.value().isNull());
    }

    @Test
    void testEvaluateBooleanFromIdentifier() {
        var escope = Map.of(
                "true", (Object) Boolean.TRUE,
                "false", Boolean.FALSE);

        var result = formula.evaluate("true", escope);
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Boolean.class));
        assertFalse(result.value().isString());
        assertEquals(Boolean.TRUE, result.value().asType(Boolean.class));
        assertEquals(Boolean.class, result.value().getType());
        assertEquals("java.lang.Boolean", result.value().getTypeName());
        assertEquals("true", result.value().toString());

        result = formula.evaluate("false", escope);
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertFalse(result.value().isTruthful());
        assertTrue(result.value().isType(Boolean.class));
        assertFalse(result.value().isString());
        assertEquals(Boolean.FALSE, result.value().asType(Boolean.class));
        assertEquals(Boolean.class, result.value().getType());
        assertEquals("java.lang.Boolean", result.value().getTypeName());
        assertEquals("false", result.value().toString());
    }

    @Test
    void testEvaluateObjectFromIdentifier() {
        var object = new Object();
        var result = formula.evaluate("object", Map.of("object", object));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Object.class));
        assertFalse(result.value().isString());
        assertEquals(object, result.value().asType(Object.class));
        assertEquals(Object.class, result.value().getType());
        assertEquals("java.lang.Object", result.value().getTypeName());
        assertEquals("java.lang.Object@" + Integer.toHexString(object.hashCode()), result.value().toString());
    }

    @Test
    void testEvaluateNumber() {
        var result = formula.evaluate("0");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertFalse(result.value().isTruthful());
        assertTrue(result.value().isType(BigInteger.class));
        assertFalse(result.value().isString());
        assertEquals(new BigInteger("0"), result.value().asBigInteger());
        assertEquals(new BigInteger("0"), result.value().asType(BigInteger.class));
        assertEquals(BigInteger.class, result.value().getType());
        assertEquals("java.math.BigInteger", result.value().getTypeName());
        assertEquals("0", result.value().toString());

        result = formula.evaluate("'0'");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertFalse(result.value().isType(BigInteger.class));
        assertTrue(result.value().isString());
        assertEquals(new BigInteger("0"), result.value().asBigInteger());
        assertEquals("0", result.value().asType(String.class));
        assertEquals(String.class, result.value().getType());
        assertEquals("java.lang.String", result.value().getTypeName());
        assertEquals("0", result.value().toString());

        result = formula.evaluate("123");
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
        assertEquals("123", result.value().toString());

        result = formula.evaluate("'123'");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertFalse(result.value().isType(BigInteger.class));
        assertTrue(result.value().isString());
        assertEquals(new BigInteger("123"), result.value().asBigInteger());
        assertEquals("123", result.value().asType(String.class));
        assertEquals(String.class, result.value().getType());
        assertEquals("java.lang.String", result.value().getTypeName());
        assertEquals("123", result.value().toString());

        result = formula.evaluate("0.0");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertFalse(result.value().isTruthful());
        assertTrue(result.value().isType(BigDecimal.class));
        assertFalse(result.value().isString());
        assertEquals(new BigDecimal("0.0"), result.value().asBigDecimal());
        assertEquals(new BigDecimal("0.0"), result.value().asType(BigDecimal.class));
        assertEquals(BigDecimal.class, result.value().getType());
        assertEquals("java.math.BigDecimal", result.value().getTypeName());
        assertEquals("0.0", result.value().toString());

        result = formula.evaluate("'0.0'");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertFalse(result.value().isType(BigDecimal.class));
        assertTrue(result.value().isString());
        assertEquals(new BigDecimal("0.0"), result.value().asBigDecimal());
        assertEquals("0.0", result.value().asType(String.class));
        assertEquals(String.class, result.value().getType());
        assertEquals("java.lang.String", result.value().getTypeName());
        assertEquals("0.0", result.value().toString());

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
        assertEquals("123.123", result.value().toString());

        result = formula.evaluate("'123.123'");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertFalse(result.value().isType(BigDecimal.class));
        assertTrue(result.value().isString());
        assertEquals(new BigDecimal("123.123"), result.value().asBigDecimal());
        assertEquals("123.123", result.value().asType(String.class));
        assertEquals(String.class, result.value().getType());
        assertEquals("java.lang.String", result.value().getTypeName());
        assertEquals("123.123", result.value().toString());
    }

    @Test
    void testEvaluateNumberFromIdentifier() {
        var result = formula.evaluate("ident", Map.of("ident", Byte.valueOf("1")));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Byte.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(Byte.valueOf("1"), result.value().asType(Byte.class));
        assertEquals(Byte.class, result.value().getType());
        assertEquals("java.lang.Byte", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", Short.valueOf("1")));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Short.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(Short.valueOf("1"), result.value().asType(Short.class));
        assertEquals(Short.class, result.value().getType());
        assertEquals("java.lang.Short", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", Integer.valueOf("1")));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Integer.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(Integer.valueOf("1"), result.value().asType(Integer.class));
        assertEquals(Integer.class, result.value().getType());
        assertEquals("java.lang.Integer", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", Long.valueOf("1")));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Long.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(Long.valueOf("1"), result.value().asType(Long.class));
        assertEquals(Long.class, result.value().getType());
        assertEquals("java.lang.Long", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", new AtomicInteger(1)));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(AtomicInteger.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(new AtomicInteger(1).intValue(), result.value().asType(AtomicInteger.class).intValue());
        assertEquals(AtomicInteger.class, result.value().getType());
        assertEquals("java.util.concurrent.atomic.AtomicInteger", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", new AtomicLong(1)));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertTrue(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(AtomicLong.class));
        assertEquals(new BigInteger("1"), result.value().asBigInteger());
        assertEquals(new AtomicLong(1).intValue(), result.value().asType(AtomicLong.class).intValue());
        assertEquals(AtomicLong.class, result.value().getType());
        assertEquals("java.util.concurrent.atomic.AtomicLong", result.value().getTypeName());
        assertEquals("1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", 1.1f));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Float.class));
        assertEquals(new BigDecimal("1.1"), result.value().asBigDecimal());
        assertEquals(1.1f, result.value().asType(Float.class));
        assertEquals(Float.class, result.value().getType());
        assertEquals("java.lang.Float", result.value().getTypeName());
        assertEquals("1.1", result.value().toString());

        result = formula.evaluate("ident", Map.of("ident", 1.1d));
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertTrue(result.value().isTruthful());
        assertTrue(result.value().isType(Double.class));
        assertEquals(new BigDecimal("1.1"), result.value().asBigDecimal());
        assertEquals(1.1d, result.value().asType(Double.class));
        assertEquals(Double.class, result.value().getType());
        assertEquals("java.lang.Double", result.value().getTypeName());
        assertEquals("1.1", result.value().toString());
    }

    @Test
    void testEvaluateString() {
        var result = formula.evaluate("''");
        assertTrue(result.isOK());
        assertFalse(result.value().isNull());
        assertTrue(result.value().isString());
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertFalse(result.value().isTruthful());
        assertTrue(result.value().isType(String.class));
        assertEquals("", result.value().asString());
        assertEquals("", result.value().asType(String.class));
        assertEquals(String.class, result.value().getType());
        assertEquals("java.lang.String", result.value().getTypeName());
        assertEquals("", result.value().toString());

        result = formula.evaluate("'abc'");
        assertTrue(result.value().isTruthful());
    }

    @Test
    void testEvaluateOperations() {
        // precedence: % -> # -> @
        var result = formula.evaluate("@'a'");
        assertTrue(result.isOK());
        assertEquals("[@a]", result.value().asString());

        result = formula.evaluate("#'a' % @'b'");
        assertTrue(result.isOK());
        assertEquals("[[#a]%[@b]]", result.value().asString());

        result = formula.evaluate("'a' % 'b' # 'c' @ 'd'");
        assertTrue(result.isOK());
        assertEquals("[[[a%b]#c]@d]", result.value().asString());

        result = formula.evaluate("'a' % 'b' # ('c' @ 'd')");
        assertTrue(result.isOK());
        assertEquals("[[a%b]#[c@d]]", result.value().asString());

        result = formula.evaluate("'a' % ('b' # ('c' @ 'd'))");
        assertTrue(result.isOK());
        assertEquals("[a%[b#[c@d]]]", result.value().asString());
    }

    @Test
    void testEvalutateFunction() {
        var result = formula.evaluate("join(1, 2, 'abc')");
        assertTrue(result.isOK());
        assertEquals("1, 2, abc", result.value().asString());

        result = formula.evaluate("join(a, b, c, d)", Map.of("a", 1, "b", 2, "c", "abc", "d", true));
        assertTrue(result.isOK());
        assertEquals("1, 2, abc, true", result.value().asString());

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> formula.evaluate("invalid()"));
    }

    @Nested
    class ExceptionTest {

        @BeforeEach
        void beforeEach() {
            Locale.setDefault(Locale.ENGLISH);
        }

        @Test
        void testEvaluateWithParserException() {
            var result = formula.evaluate("foo(123");
            assertFalse(result.isOK());
            assertEquals(UNEXPECTED_TOKEN, result.exception().type);
            assertEquals(pos(7), result.exception().position);
        }

        @Test
        void testFunctionNotDefined() {
            var result = formula.evaluate("foo(123)");
            assertFalse(result.isOK());
            assertEquals(FUNCTION_NOT_DEFINED, result.exception().type);
            assertEquals("The function foo() has not been defined", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
            assertEquals(
                    formatMessage("The function foo() has not been defined", "foo(123)", 0),
                    result.formattedErrorMessage());
        }

        @Test
        void testOperationNotSupported() {
            var result = formula.evaluate("1 @ 2");
            assertFalse(result.isOK());
            assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);
            assertEquals("The operation BigInteger @ BigInteger is not supported", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
            assertEquals(
                    formatMessage("The operation BigInteger @ BigInteger is not supported", "1 @ 2", 0),
                    result.formattedErrorMessage());

            result = formula.evaluate("@ 2");
            assertFalse(result.isOK());
            assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);
            assertEquals("The operation @ BigInteger is not supported", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
            assertEquals(
                    formatMessage("The operation @ BigInteger is not supported", "@ 2", 0),
                    result.formattedErrorMessage());

            result = formula.evaluate("'a' @ 2");
            assertFalse(result.isOK());
            assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);
            assertEquals("The operation String @ BigInteger is not supported", result.exception().getMessage());
            assertEquals(pos(0), result.exception().position);
            assertEquals(
                    formatMessage("The operation String @ BigInteger is not supported", "'a' @ 2", 0),
                    result.formattedErrorMessage());
        }
    }
}