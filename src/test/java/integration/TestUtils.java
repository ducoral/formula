package integration;

import com.github.ducoral.formula.Expression;
import com.github.ducoral.formula.Formula;
import com.github.ducoral.formula.Position;
import com.github.ducoral.formula.Result;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.fail;

class TestUtils {

    static final String FORMATTED_MESSAGE = "Error:\n\t%s\n\nPosition:\n\t 1 | %s\n\t     %s\n";

    static void assertOK(Result<?> result) {
        if (!result.isOK())
            fail(result.formattedErrorMessage());
    }

    static String formatMessage(String message, String expression, int position) {
        var offset = position > 0
                ? new String(new char[position]).replace('\0', '-')
                : "";
        return String.format(FORMATTED_MESSAGE, message, expression, offset + "^");
    }

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

    static Position pos(int index) {
        return new Position(index, 0, index);
    }
}
