package com.github.ducoral.formula;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Strings {

    private final static ResourceBundle resourceBundle = ResourceBundle.getBundle("strings");

    static String get(String key, Object... args) {
        return MessageFormat.format(resourceBundle.getString(key), args);
    }
}