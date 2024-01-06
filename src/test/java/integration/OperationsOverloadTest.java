package integration;

import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.OperationAction;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Precedence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static integration.TestUtils.assertOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationsOverloadTest {

    Operator operator = new Operator("@@", new Precedence(1));

    OperationAction stringAction = (operands, chain) ->
            operands.right().isString()
                    ? operands.left().asString() + operands.right().asString()
                    : chain.chain(operands);

    OperationAction anotherStringAction =  (operands, chain) ->
            operands.right().isString()
                    ? operands.left().asString() + operands.right().asString()
                    : chain.chain(operands);

    OperationAction integerAction = (operands, chain) ->
            operands.right().isInteger()
                    ? operands.left().asBigInteger().add(operands.right().asBigInteger())
                    : chain.chain(operands);

    OperationAction decimalAction = (operands, chain) ->
            operands.right().isDecimal()
                    ? operands.left().asBigDecimal().add(operands.right().asBigDecimal())
                    : chain.chain(operands);

    Object object = new Object();

    OperationAction objectAction = (operands, chain) -> object;

    Formula formula;

    @BeforeEach
    void beforeEach() {
        formula = Formula.builder()
                .binaryOperation(new Operation(String.class, operator, stringAction))
                .binaryOperation(new Operation(String.class, operator, anotherStringAction))
                .binaryOperation(new Operation(BigInteger.class, operator, integerAction))
                .binaryOperation(new Operation(BigDecimal.class, operator, decimalAction))
                .binaryOperation(new Operation(Object.class, operator, objectAction))
                .build();
    }

    @Test
    void testStringOperation() {
        var result = formula.evaluate("'a' @@ 'b'");
        assertOK(result);
        assertTrue(result.value().isString());
        assertEquals("ab", result.value().asString());
    }

    @Test
    void testIntegerOperation() {
        var result = formula.evaluate("1 @@ 2");
        assertOK(result);
        assertTrue(result.value().isInteger());
        assertEquals(new BigInteger("3"), result.value().asBigInteger());
    }

    @Test
    void testDecimalOperation() {
        var result = formula.evaluate("1.0 @@ 2.0");
        assertOK(result);
        assertTrue(result.value().isDecimal());
        assertEquals(new BigDecimal("3.0"), result.value().asBigDecimal());
    }

    @Test
    void testObjectOperation() {
        var result = formula.evaluate("obj @@ obj", Map.of("obj", object));
        assertOK(result);
        assertFalse(result.value().isNumber());
        assertFalse(result.value().isString());
        assertEquals(object, result.value().asObject());
    }
}