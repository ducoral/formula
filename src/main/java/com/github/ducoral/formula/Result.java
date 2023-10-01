package com.github.ducoral.formula;

public class Result<T> {

    private final T value;

    private final FormulaException exception;

    private final String formattedErrorMessage;

    Result(T value) {
        this.value = value;
        exception = null;
        formattedErrorMessage = null;
    }

    Result(String input, FormulaException exception) {
        value = null;
        this.exception = exception;
        formattedErrorMessage = formatErrorMessage(input, exception);
    }

    Result(Result<?> from) {
        value = null;
        exception = from.exception;
        formattedErrorMessage = from.formattedErrorMessage;
    }

    public boolean isOK() {
        return value != null;
    }

    public T value() {
        return value;
    }

    public FormulaException exception() {
        return exception;
    }

    public String formattedErrorMessage() {
        return formattedErrorMessage;
    }

    private static String formatErrorMessage(String input, FormulaException exception) {
        var message = new StringBuilder(Strings.get("error"))
                .append(":\n\t")
                .append(exception.getMessage())
                .append("\n\n")
                .append(Strings.get("position"))
                .append(":\n");

        var lines = input.split("\\n");
        var line = 0;
        while (line < lines.length) {
            message
                    .append('\t')
                    .append(Utils.rightAlign(String.valueOf(line + 1), 2))
                    .append(" | ")
                    .append(lines[line])
                    .append('\n');
            line++;
        }

        message
                .append('\t')
                .append(Utils.fillSpaces(5))
                .append(Utils.fill('-', exception.position.column()))
                .append("^\n\t");

        return message.toString();
    }
}