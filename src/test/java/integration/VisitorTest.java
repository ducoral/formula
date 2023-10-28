package integration;

import com.github.ducoral.formula.Expression;
import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.Operation;
import com.github.ducoral.formula.Operator;
import com.github.ducoral.formula.Precedence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.ducoral.formula.Expression.BinaryOperation;
import static com.github.ducoral.formula.Expression.FunctionCall;
import static com.github.ducoral.formula.Expression.Identifier;
import static com.github.ducoral.formula.Expression.NumberLiteral;
import static com.github.ducoral.formula.Expression.StringLiteral;
import static com.github.ducoral.formula.Expression.UnaryOperation;
import static integration.TestUtils.assertParse;
import static integration.TestUtils.assertValueType;
import static integration.TestUtils.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisitorTest {

    final Operator sharp = new Operator("#", new Precedence(0));
    final Operator at = new Operator("@", new Precedence(1));

    final AtomicReference<Expression> visited = new AtomicReference<>();

    class Visitor implements Expression.Visitor {

        @Override
        public void visit(NumberLiteral numberLiteral) {
            visited.set(numberLiteral);
        }

        @Override
        public void visit(StringLiteral stringLiteral) {
            visited.set(stringLiteral);
        }

        @Override
        public void visit(Identifier identifier) {
            visited.set(identifier);
        }

        @Override
        public void visit(UnaryOperation unaryOperation) {
            visited.set(unaryOperation);
        }

        @Override
        public void visit(BinaryOperation binaryOperation) {
            visited.set(binaryOperation);
        }

        @Override
        public void visit(FunctionCall functionCall) {
            visited.set(functionCall);
        }

    }

    Formula formula;

    @BeforeEach
    void beforeEach() {
        formula = Formula.builder()
                .unaryOperation(new Operation(Number.class, sharp, (operands, chain) -> null))
                .unaryOperation(new Operation(Number.class, at, (operands, chain) -> null))
                .binaryOperation(new Operation(Number.class, sharp, (operands, chain) -> null))
                .binaryOperation(new Operation(Number.class, at, (operands, chain) -> null))
                .build();
    }

    void assertVisitor(String input) {
        assertValueType(assertParse(formula, input), Expression.class)
                .accept(new Visitor());
    }

    @Test
    void testEmptyVisited() {
        assertVisitor("");
        assertNull(visited.get());
    }

    @Test
    void testNumberLiteralVisited() {
        assertVisitor("123");
        assertEquals(new NumberLiteral(pos(0), new BigInteger("123")), visited.get());

        assertVisitor("123.123");
        assertEquals(new NumberLiteral(pos(0), new BigDecimal("123.123")), visited.get());
    }

    @Test
    void testStringLiteralVisited() {
        assertVisitor("'abc'");
        assertEquals(new StringLiteral(pos(0), "abc"), visited.get());
    }

    @Test
    void testIdentifierVisited() {
        assertVisitor("abc");
        assertEquals(new Identifier(pos(0), "abc"), visited.get());
    }

    @Test
    void testUnaryOperationVisited() {
        assertVisitor("@ 1");
        var right = new NumberLiteral(pos(2), new BigInteger("1"));
        assertEquals(new UnaryOperation(pos(0), "@", right), visited.get());
    }

    @Test
    void testBinaryOperationVisited() {
        assertVisitor("1 @ 1");
        var left = new NumberLiteral(pos(0), new BigInteger("1"));
        var right = new NumberLiteral(pos(4), new BigInteger("1"));
        assertEquals(new BinaryOperation(pos(0), left, "@", right), visited.get());
    }

    @Test
    void testFunctionCallVisited() {
        assertVisitor("call()");
        assertEquals(new FunctionCall(pos(0), "call", List.of()), visited.get());
    }
}
