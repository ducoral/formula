package com.github.ducoral.formula;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Strings {

    static String get(String key, Object... args) {
        return MessageFormat.format(resourceBundle().getString(key), args);
    }

    static ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("strings");
    }
}