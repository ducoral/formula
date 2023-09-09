package com.github.ducoral.formula;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

class ExpressionExplorer {

    static void show(Formula formula) {
        var form = new JDialog();
        form.setLayout(new BorderLayout());

        var input = new JTextField();
        var pane = new JEditorPane();
        pane.setEditable(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        var fonts = new JComboBox<String>();
        for (var font : getLocalGraphicsEnvironment().getAllFonts()) {
            var name = font.getName();
            var lower = name.toLowerCase();
            if (lower.contains("mono") && !(lower.contains("bold") || lower.contains("italic") || lower.contains("oblique")))
                fonts.addItem(name);
        }
        fonts.addActionListener(e -> {
            String fontName = (String) fonts.getSelectedItem();
            input.setFont(new Font(fontName, Font.PLAIN, 16));
            pane.setFont(new Font(fontName, Font.PLAIN, 14));
        });

        configOnChange(input, () -> {
            try {
                var explain = formula.explain(input.getText());
                pane.setText(explain);
                input.setBackground(Color.WHITE);
            } catch (Exception e) {
                input.setBackground(Color.MAGENTA);
            }
        });

        form.add(input, BorderLayout.NORTH);
        form.add(pane, BorderLayout.CENTER);
        form.add(fonts, BorderLayout.SOUTH);
        form.setBounds(200, 200, 800, 550);
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        form.setVisible(true);
        fonts.setSelectedIndex(0);
    }

    private static void configOnChange(JTextField textField, Runnable action) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }
}
