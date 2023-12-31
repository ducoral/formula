package com.github.ducoral.formula;

public class Result<T> {

    private final T value;

    private final FormulaException exception;

    private final String formattedErrorMessage;

    public static <T> Result<T> ofValue(T value) {
        return new Result<>(value, null, null);
    }

    public static <T> Result<T> ofException(String input, FormulaException exception) {
        return new Result<>(null, exception, formatErrorMessage(input, exception));
    }

    public static <T> Result<T> ofInvalid(Result<?> invalidResult) {
        return new Result<>(null, invalidResult.exception, invalidResult.formattedErrorMessage);
    }

    private Result(T value, FormulaException exception, String formattedErrorMessage) {
        this.value = value;
        this.exception = exception;
        this.formattedErrorMessage = formattedErrorMessage;
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
                .append("^\n");

        return message.toString();
    }
}