package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.FormulaDefaults;
import com.github.ducoral.formula.FunctionDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ExplainTest {

    Formula formula;

    @BeforeEach
    void beforeEach() {
        formula = Formula
                .builder()
                .configure(FormulaDefaults.OPERATIONS_DEFAULT)
                .function(new FunctionDefinition("foo", parameters -> ""))
                .build();
    }

    @Test
    void testExpression1() {
        var result = formula.explain("1 + 2");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(1), result.value());
    }

    @Test
    void testExpression2() {
        var result = formula.explain("1 - 2 - 3");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(2), result.value());
    }

    @Test
    void testExpression3() {
        var result = formula.explain("1 - 2 * 3");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(3), result.value());
    }

    @Test
    void testExpression4() {
        var result = formula.explain("1 - 2 * 3 + foo(1, 2)");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(4), result.value());
    }

    @Test
    void testExpression5() {
        var result = formula.explain("1 - 2 * 3 + foo(1, 2) / foo(abc, 'string', true) > variable * 5");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(5), result.value());
    }

    @Test
    void testExpression6() {
        var result = formula.explain("`ab'c` + (\"e'fg\" + 3) + variable * 5");
        assertTrue(result.isOK());
        assertEquals(readExpressionExplain(6), result.value());
    }

    String readExpressionExplain(int number) {
        var resourceName = "expression-explain-%s.txt";
        try (var resource = TestUtils.class.getClassLoader().getResourceAsStream(resourceName.formatted(number))) {
            return resource == null
                    ? ""
                    : new String(resource.readAllBytes()).replace("''", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}