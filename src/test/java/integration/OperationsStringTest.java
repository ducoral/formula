package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.FormulaDefaults;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.ducoral.formula.FormulaExceptionType.INVALID_CHARACTER;
import static com.github.ducoral.formula.FormulaExceptionType.OPERATION_NOT_SUPPORTED;
import static integration.TestUtils.assertOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationsStringTest {

    Formula formula;

    @BeforeEach
    void beforeEach() {
        formula = Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_STRING)
                .build();
    }

    @Test
    void testConcatenationOperation() {
        var result = formula.evaluate("'abc' + 'def'");
        assertOK(result);
        assertEquals("abcdef", result.value().asString());
    }

    @Test
    void testSubtractionOperation() {
        var result = formula.evaluate("`abcdef` - `def`");
        assertOK(result);
        assertEquals("abc", result.value().asString());
    }

    @Test
    void testEqualOperation() {
        var result = formula.evaluate("`abc` == `abc`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testNotEqualOperation() {
        var result = formula.evaluate("`abc` != `def`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testGreaterThanOperation() {
        var result = formula.evaluate("`def` > `abc`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testGreaterThanOrEqualOperation() {
        var result = formula.evaluate("`abc` >= `abc`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testLessThanOperation() {
        var result = formula.evaluate("`abc` < `def`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testLessThanOrEqualOperation() {
        var result = formula.evaluate("`def` <= `def`");
        assertOK(result);
        assertTrue(result.value().isTrue());
    }

    @Test
    void testInvalidCharacterSlash() {
        var result = formula.evaluate("`def` / `def`");
        assertFalse(result.isOK());
        assertEquals(INVALID_CHARACTER, result.exception().type);
    }

    @Test
    void testOperationNotSupported() {
        var slashOperator = new Operator("/", FormulaDefaults.ARITHMETIC_2);
        var formula =  Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_STRING)
                .binaryOperation(new Operation(String.class, slashOperator, (operands, chain) -> chain.chain(operands)))
                .build();
        var result = formula.evaluate("`def` / `def`");
        assertFalse(result.isOK());
        assertEquals(OPERATION_NOT_SUPPORTED, result.exception().type);
    }
}