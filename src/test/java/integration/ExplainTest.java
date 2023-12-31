package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.FunctionDefinition;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Precedence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static integration.TestUtils.assertOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class ExplainTest {

    Formula formula;

    @BeforeEach
    void beforeEach() {
        var pre1 = new Precedence(1);
        var pre2 = new Precedence(2);
        var pre3 = new Precedence(3);
        var op1 = new Operator("$", pre1);
        var op2 = new Operator("%", pre1);
        var op3 = new Operator("@", pre2);
        var op4 = new Operator("#", pre2);
        var op5 = new Operator("&", pre3);

        formula = Formula
                .builder()
                .unaryOperation(new Operation(Object.class, op4, (operands, chain) -> chain.chain(operands)))
                .binaryOperation(new Operation(Object.class, op1, (operands, chain) -> chain.chain(operands)))
                .binaryOperation(new Operation(Object.class, op2, (operands, chain) -> chain.chain(operands)))
                .binaryOperation(new Operation(Object.class, op3, (operands, chain) -> chain.chain(operands)))
                .binaryOperation(new Operation(Object.class, op4, (operands, chain) -> chain.chain(operands)))
                .binaryOperation(new Operation(Object.class, op5, (operands, chain) -> chain.chain(operands)))
                .function(new FunctionDefinition("foo", parameters -> ""))
                .build();
    }

    @Test
    void testEmptyExpression() {
        var result = formula.explain("");
        assertOK(result);
        assertTrue(result.value().isEmpty());
    }

    @Test
    void testWrongExpression() {
        var result = formula.explain("1 @ .");
        assertFalse(result.isOK());
    }

    @Test
    void testExpression1() {
        var result = formula.explain("1 @ 2");
        assertOK(result);
        assertEquals(readExpressionExplain(1), result.value());
    }

    @Test
    void testExpression2() {
        var result = formula.explain("1 # 2 # 3");
        assertOK(result);
        assertEquals(readExpressionExplain(2), result.value());
    }

    @Test
    void testExpression3() {
        var result = formula.explain("1 # 2 $ #3");
        assertOK(result);
        assertEquals(readExpressionExplain(3), result.value());
    }

    @Test
    void testExpression4() {
        var result = formula.explain("1 # 2 $ #3 @ foo(#1, 2)");
        if (!result.isOK())
            fail(result.formattedErrorMessage());
        assertOK(result);
        assertEquals(readExpressionExplain(4), result.value());
    }

    @Test
    void testExpression5() {
        var result = formula.explain("1 # 2 $ 3 @ foo(1, 2) % foo(abc, 'string', true) & variable $ 5");
        assertOK(result);
        assertEquals(readExpressionExplain(5), result.value());
    }

    @Test
    void testExpression6() {
        var result = formula.explain("`ab'c` @ (\"e'fg-hij`kl\" @ 3) @ variable $ 5");
        assertOK(result);
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