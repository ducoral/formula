package com.github.ducoral.formula;

import java.util.function.Function;

public record FunctionDefinition(String name, Function<Parameters, Object> function) {
}
