package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.FormulaDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static integration.TestUtils.assertOK;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationsLogicalTest {

    Formula formula;

    Map<String, Object> variables;

    @BeforeEach
    void beforeEach() {
        formula = Formula.builder()
                .configure(FormulaDefaults.OPERATIONS_LOGICAL)
                .build();

        variables = Map.of("true", true, "false", false);
    }

    @Test
    void testAndOperation() {
        var result = formula.evaluate("true && true", variables);
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = formula.evaluate("true && false", variables);
        assertOK(result);
        assertFalse(result.value().isTrue());

        result = formula.evaluate("false && true", variables);
        assertOK(result);
        assertFalse(result.value().isTrue());

        result = formula.evaluate("false && false", variables);
        assertOK(result);
        assertFalse(result.value().isTrue());
    }

    @Test
    void testOrOperation() {
        var result = formula.evaluate("true || true", variables);
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = formula.evaluate("true || false", variables);
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = formula.evaluate("false || true", variables);
        assertOK(result);
        assertTrue(result.value().isTrue());

        result = formula.evaluate("false || false", variables);
        assertOK(result);
        assertFalse(result.value().isTrue());
    }
}
