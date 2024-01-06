package integration;

import com.github.ducoral.formula.Formula;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;

import static com.github.ducoral.formula.FormulaDefaults.OPERATIONS_NUMBER;
import static com.github.ducoral.formula.FormulaExceptionType.OPERATION_NOT_SUPPORTED;
import static integration.TestUtils.assertOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationsNumberTest {

    Formula.Builder builder;

    @BeforeEach
    void beforeEach() {
        builder = Formula.builder()
                .configure(OPERATIONS_NUMBER);
    }

    @Test
    void testNegateOperation() {
        var result = builder.build().evaluate("-11");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(bigInteger("-11"), result.value().asBigInteger());

        result = builder.build().evaluate("-11.0");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("-11", 1), result.value().asBigDecimal());
    }

    @Test
    void testSumOperation() {
        var result = builder.build().evaluate("11 + 11");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(bigInteger("22"), result.value().asBigInteger());

        result = builder.build().evaluate("11.0 + 11");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("22", 1), result.value().asBigDecimal());

        result = builder.build().evaluate("11 + 11.0");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("22", 1), result.value().asBigDecimal());
    }

    @Test
    void testSubtractOperation() {
        var result = builder.build().evaluate("22 - 11");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(bigInteger("11"), result.value().asBigInteger());

        result = builder.build().evaluate("22.0 - 11");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("11", 1), result.value().asBigDecimal());

        result = builder.build().evaluate("22 - 11.0");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("11", 1), result.value().asBigDecimal());
    }

    @Test
    void testMultiplyOperation() {
        var result = builder.build().evaluate("11 * 2");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(bigInteger("22"), result.value().asBigInteger());

        result = builder.build().evaluate("11.0 * 2");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("22", 2), result.value().asBigDecimal());

        result = builder.build().evaluate("11 * 2.0");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("22", 2), result.value().asBigDecimal());
    }

    @Test
    void testDivideOperation() {
        var result = builder
                .build()
                .evaluate("11 / 2");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(bigInteger("5"), result.value().asBigInteger());

        result = builder
                .build()
                .evaluate("11.0 / 2");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("5.5", 1), result.value().asBigDecimal());

        result = builder
                .build()
                .evaluate("11 / 2.0");
        assertOK(result);
        assertTrue(result.value().isNumber());
        assertFalse(result.value().isInteger());
        assertEquals(bigDecimal("5.5", 1), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.UP)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("6", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.DOWN)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("5", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.CEILING)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("-11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("-5", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.FLOOR)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("-11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("-6", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.HALF_UP)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("6", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.HALF_DOWN)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("5", 0), result.value().asBigDecimal());

        result = builder
                .roudingMode(RoundingMode.HALF_EVEN)
                .build()
                .evaluate("x / 2", Map.of("x", bigDecimal("11", 0)));
        assertOK(result);
        assertEquals(bigDecimal("6", 0), result.value().asBigDecimal());
    }

    @Test
    void testEqualOperation() {
        var result = builder.build().evaluate("11 == 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11.0 == 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11 == 11.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testNotEqualOperation() {
        var result = builder.build().evaluate("11 != 12");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11.0 != 12");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11 != 12.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testGreaterThanOperation() {
        var result = builder.build().evaluate("12 > 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("12.0 > 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("12 > 11.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testGreaterThanOrEqualOperation() {
        var result = builder.build().evaluate("11 >= 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11.0 >= 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11 >= 11.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testLessThanOperation() {
        var result = builder.build().evaluate("11 < 12");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11.0 < 12");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11 < 12.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testLessThanOrEqualOperation() {
        var result = builder.build().evaluate("11 <= 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11.0 <= 11");
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = builder.build().evaluate("11 <= 11.0");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testOperationNotSupported() {
        var result = builder.build().evaluate("-`a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 + `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 - `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 * `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 / `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 == `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 != `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 > `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 >= `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 < `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);

        result = builder.build().evaluate("1 <= `a`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);
    }

    static BigInteger bigInteger(String val) {
        return new BigInteger(val);
    }

    static BigDecimal bigDecimal(String val, int scale) {
        return new BigDecimal(val)
                .setScale(scale, RoundingMode.UNNECESSARY);
    }
}