package com.github.ducoral.formula;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

class ExpressionExplorer {

    private static final String IDENT_PREFIX = "identifier_";
    private static final File FORMULA_HOME = new File(System.getProperty("user.home") + "/.formula");
    private static final File EXPLORER_FILE = new File(FORMULA_HOME.getAbsolutePath() + "/explorer.properties");
    private static final Properties DATA = new Properties();

    static {
        try {
            var homeExists = FORMULA_HOME.exists() || FORMULA_HOME.mkdir();
            var fileExists = EXPLORER_FILE.exists() || EXPLORER_FILE.createNewFile();
            if (homeExists && fileExists)
                DATA.load(new FileInputStream(EXPLORER_FILE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void show(Formula formula) {
        var inputPane = new JEditorPane();
        var explainPane = new JEditorPane();
        var resultPane = new JEditorPane();

        var model = new SymbolTableModel().loadFromData();
        var table = new JTable(model);

        var loading = new AtomicBoolean(true);

        var evaluate = evaluateRunnable(loading, formula, model, inputPane, explainPane, resultPane);

        var area = buildHoriSplit(
                buildVertSplit(
                        buildVertSplit(buildInputPanel(inputPane), buildExplainPanel(explainPane)),
                        buildResultPanel(resultPane)),
                buildSymbolTablePanel(table, evaluate));

        var bottomPanel = buildBottomPanel(inputPane, explainPane, resultPane, table);

        configOnChange(inputPane, evaluate);

        if (DATA.containsKey("user.input"))
            inputPane.setText(DATA.getProperty("user.input"));

        loading.set(false);

        var frame = new JFrame();
        frame.setTitle(Strings.get("expression.explorer"));
        frame.setLayout(new BorderLayout());
        frame.add(area, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static Runnable evaluateRunnable(
            AtomicBoolean loading,
            Formula formula,
            SymbolTableModel symbolTableModel,
            JEditorPane inputPane,
            JEditorPane explainPane,
            JEditorPane resultPane) {
        return () -> {
            try {
                var input = inputPane.getText();
                var explainResult = formula.explain(input);

                if (explainResult.isOK()) {
                    explainPane.setText(explainResult.value());
                    explainPane.setForeground(Color.BLACK);
                } else
                    explainPane.setForeground(Color.LIGHT_GRAY);

                if (!loading.get()) {
                    DATA.setProperty("user.input", input);
                    saveData();
                }

                var evaluateResult = formula.evaluate(inputPane.getText(), symbolTableModel.getScope());
                if (evaluateResult.isOK()) {
                    resultPane.setText(evaluateResult.value().asString());
                    resultPane.setForeground(Color.BLACK);
                } else {
                    resultPane.setText(evaluateResult.formattedErrorMessage());
                    resultPane.setForeground(Color.RED);
                }
            } catch (Exception e) {
                resultPane.setText(e.getMessage());
                resultPane.setForeground(Color.RED);
            }
        };
    }

    private static void formatErrorMessage(JEditorPane inputPane, JEditorPane resultPane, FormulaException e) {
        var message = new StringBuilder(Strings.get("error"))
                .append(":\n\t")
                .append(e.getMessage())
                .append("\n\n")
                .append(Strings.get("position"))
                .append(":\n");

        var lines = inputPane.getText().split("\\n");
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
                .append(Utils.fill('-', e.position.column()))
                .append("^\n\t");

        resultPane.setText(message.toString());
    }

    private static void saveData() {
        try {
            DATA.store(new FileWriter(EXPLORER_FILE), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JComponent buildVertSplit(JComponent left, JComponent right) {
        return split(JSplitPane.VERTICAL_SPLIT, left, right);
    }

    private static JComponent buildHoriSplit(JComponent left, JComponent right) {
        return split(JSplitPane.HORIZONTAL_SPLIT, left, right);
    }

    private static JComponent split(int orientation, JComponent left, JComponent right) {
        var splitPane = new JSplitPane(orientation, left, right);
        splitPane.setDividerSize(5);
        var panel = new JPanel(new BorderLayout());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel buildInputPanel(JEditorPane inputPane) {
        return newPanel(inputPane, Strings.get("expression"), dim(750, 100));
    }

    private static JPanel buildExplainPanel(JEditorPane explainPane) {
        var panel = newPanel(explainPane, Strings.get("explanation"), dim(750, 450));
        explainPane.setEditable(false);
        explainPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        return panel;
    }

    private static JPanel buildResultPanel(JEditorPane resultPane) {
        var panel = newPanel(resultPane, Strings.get("result"), dim(750, 200));
        resultPane.setEditable(false);
        return panel;
    }

    private static JPanel buildSymbolTablePanel(JTable table, Runnable action) {
        var header = new JPanel(new GridLayout(1, 2));
        var nameField = new JTextField();
        var valueField = new JTextField();
        header.add(buildFieldPanel(Strings.get("identifier"), nameField));
        header.add(buildFieldPanel(Strings.get("value"), valueField));

        var model = (SymbolTableModel) table.getModel();

        nameField.addActionListener(e -> valueField.requestFocus());
        valueField.addActionListener(e -> {
            var name = nameField.getText();
            var value = valueField.getText();

            if (value.isEmpty())
                model.removeIdentifier(name);
            else
                model.setIdentifier(name, value);
            model.fireTableDataChanged();
            nameField.setText("");
            valueField.setText("");
            nameField.requestFocus();
            DATA.setProperty(IDENT_PREFIX + name, value);
            saveData();
            action.run();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow > -1) {
                model.getRow(selectedRow).load(nameField, valueField);
                valueField.selectAll();
                valueField.requestFocus();
            }
        });

        var area = new JPanel(new BorderLayout());
        area.add(header, BorderLayout.NORTH);
        area.add(table, BorderLayout.CENTER);

        return newPanel(area, Strings.get("symbol.table"), dim(250, 0));
    }

    private static JPanel buildFieldPanel(String label, JTextField field) {
        var panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(label, JLabel.LEFT);
        lbl.setLabelFor(field);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel newPanel(JComponent component, String title, Dimension preferredSize) {
        var panel = new JPanel(new BorderLayout());
        if (component != null)
            panel.add(component, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setMinimumSize(dim(0, 0));
        if (preferredSize != null)
            panel.setPreferredSize(preferredSize);
        return panel;
    }

    private static Dimension dim(int width, int height) {
        return new Dimension(width, height);
    }

    private static JPanel buildBottomPanel(JComponent... components) {
        var fonts = new JComboBox<String>();
        for (var font : getLocalGraphicsEnvironment().getAllFonts()) {
            var name = font.getName();
            var lower = name.toLowerCase();
            if (lower.contains("mono") && !(lower.contains("bold") || lower.contains("italic") || lower.contains("oblique")))
                fonts.addItem(name);
        }

        var sizes = new JComboBox<Integer>();
        for (int size = 11; size < 25; size++)
            sizes.addItem(size);

        var loading = new AtomicBoolean(true);

        ActionListener action = e -> {
            var fontName = (String) fonts.getSelectedItem();
            var size = (Integer) sizes.getSelectedItem();
            for (var component : components)
                component.setFont(new Font(fontName, Font.PLAIN, size == null ? 14 : size));

            if (!loading.get()) {
                DATA.setProperty("font.name", fontName);
                DATA.setProperty("font.size", String.valueOf(size));
                saveData();
            }
        };

        fonts.addActionListener(action);
        sizes.addActionListener(action);

        var selectedFont = DATA.containsKey("font.name")
                ? DATA.get("font.name")
                : fonts.getItemAt(0);
        fonts.setSelectedItem(selectedFont);

        var selectedSize = DATA.containsKey("font.size")
                ? Integer.valueOf(DATA.getProperty("font.size"))
                : sizes.getItemAt(3);
        sizes.setSelectedItem(selectedSize);

        loading.set(false);

        var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(fonts);
        panel.add(sizes);
        return panel;
    }

    private static void configOnChange(JTextComponent textComponent, Runnable action) {
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
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

    private static class SymbolTableModel extends AbstractTableModel {

        private final List<Row> rows = new ArrayList<>();

        SymbolTableModel loadFromData() {
            ExpressionExplorer.DATA
                    .keySet()
                    .stream()
                    .map(String::valueOf)
                    .filter(key -> key.startsWith(IDENT_PREFIX))
                    .forEach(property ->
                            setIdentifier(
                                    property.substring(IDENT_PREFIX.length()),
                                    ExpressionExplorer.DATA.getProperty(property)));
            return this;
        }

        void removeIdentifier(String name) {
            int rowIndex = findRowIndex(name);
            if (rowIndex > -1)
                rows.remove(rowIndex);
        }

        void setIdentifier(String name, String value) {
            if (name.isEmpty() || value.isEmpty())
                return;
            int rowIndex = findRowIndex(name);

            var objectValue = value.matches("\\d+(\\.\\d+)?")
                    ? (value.contains(".") ? new BigDecimal(value) : new BigInteger(value))
                    : value;
            if (rowIndex > -1)
                getRow(rowIndex).value = objectValue;
            else
                rows.add(new Row(name, objectValue));
        }

        Row getRow(int index) {
            return rows.get(index);
        }

        Map<String, Object> getScope() {
            var scope = new HashMap<String, Object>();
            for (var row : rows)
                scope.put(row.identifier, row.value);
            return scope;
        }

        private int findRowIndex(String name) {
            for (int index = 0; index < rows.size(); index++)
                if (rows.get(index).identifier.equals(name))
                    return index;
            return -1;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex).get(columnIndex);
        }
    }

    private static class Row {

        private final String identifier;

        private Object value;

        Row(String identifier, Object value) {
            this.identifier = identifier;
            this.value = value;
        }

        Object get(int column) {
            return column == 0 ? identifier : value;
        }

        void load(JTextField identifierField, JTextField valueField) {
            identifierField.setText(identifier);
            valueField.setText(String.valueOf(value));
        }
    }
}