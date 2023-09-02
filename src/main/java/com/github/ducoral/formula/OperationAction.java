package com.github.ducoral.formula;

import java.util.function.BiFunction;

public interface OperationAction extends BiFunction<Operands, OperationChain, Object> {
}
