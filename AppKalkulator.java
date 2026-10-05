
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class AppKalkulator extends JFrame {

    private static final String ERROR = "Error";
    private static final int MAX_DIGITS = 15;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private final Color COLOR_BG = new Color(24, 24, 27);
    private final Color COLOR_BTN_NUM = new Color(44, 45, 51);
    private final Color COLOR_BTN_TOP = new Color(68, 70, 79);
    private final Color COLOR_BTN_FUNC = new Color(36, 52, 78);
    private final Color COLOR_BTN_OP = new Color(10, 132, 255);
    private final Color COLOR_BTN_EQ = new Color(255, 149, 0);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(150, 150, 160);
    private final Color COLOR_BADGE = new Color(50, 52, 58);

    private final List<String> riwayatOperasiList = new ArrayList<>();

    // state kalkulator
    private JTextField displayField;
    private JLabel historyLabel;
    private JLabel historyBadge;
    private double runningTotal = 0;
    private String operator = "";
    private boolean mulaiInputBaru = true;
    private final StringBuilder ekspresi = new StringBuilder();

    public AppKalkulator() {
        setTitle("Kalkulator & Converter");
        setSize(400, 780);
        setMinimumSize(new Dimension(360, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        RoundButton btnMenuCalc = new RoundButton("Calculator", 14, COLOR_BTN_OP);
        RoundButton btnMenuConv = new RoundButton("Converter", 14, COLOR_BTN_TOP);
        btnMenuCalc.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnMenuConv.setFont(new Font("SansSerif", Font.BOLD, 14));

        headerPanel.add(btnMenuCalc);
        headerPanel.add(btnMenuConv);
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BG);

        mainPanel.add(createCalculatorPanel(), "Kalkulator");
        mainPanel.add(createConverterPanel(), "Converter");

        add(mainPanel, BorderLayout.CENTER);

        btnMenuCalc.addActionListener(e -> {
            cardLayout.show(mainPanel, "Kalkulator");
            btnMenuCalc.setBase(COLOR_BTN_OP);
            btnMenuConv.setBase(COLOR_BTN_TOP);
        });

        btnMenuConv.addActionListener(e -> {
            cardLayout.show(mainPanel, "Converter");
            btnMenuConv.setBase(COLOR_BTN_OP);
            btnMenuCalc.setBase(COLOR_BTN_TOP);
        });
    }

    /** Tombol bulat dengan efek hover dan tekan. */
    private static class RoundButton extends JButton {
        private Color base;
        private final int radius;
        private boolean hover;

        RoundButton(String text, int radius, Color base) {
            super(text);
            this.radius = radius;
            this.base = base;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setBase(Color c) {
            base = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color c = base;
            if (getModel().isPressed()) {
                c = base.darker();
            } else if (hover) {
                c = new Color(Math.min(255, base.getRed() + 22),
                              Math.min(255, base.getGreen() + 22),
                              Math.min(255, base.getBlue() + 22));
            }
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createCalculatorPanel() {
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBackground(COLOR_BG);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(COLOR_BG);
        displayPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeRow.setBackground(COLOR_BG);

        historyBadge = new JLabel("🕒 Riwayat") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BADGE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        historyBadge.setFont(new Font("SansSerif", Font.PLAIN, 13));
        historyBadge.setForeground(COLOR_TEXT_MUTED);
        historyBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        historyBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBadge.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tampilkanDaftarRiwayat();
            }
        });
        badgeRow.add(historyBadge);

        displayField = new JTextField("0");
        displayField.setFont(new Font("SansSerif", Font.BOLD, 48));
        displayField.setForeground(COLOR_TEXT);
        displayField.setBackground(COLOR_BG);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(new EmptyBorder(8, 0, 4, 0));
        displayField.setEditable(false);
        displayField.setCaretColor(COLOR_BG);

        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        historyLabel.setForeground(COLOR_TEXT_MUTED);
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel historyRow = new JPanel(new BorderLayout());
        historyRow.setBackground(COLOR_BG);
        historyRow.add(historyLabel, BorderLayout.EAST);

        displayPanel.add(badgeRow);
        displayPanel.add(displayField);
        displayPanel.add(historyRow);
        calcPanel.add(displayPanel, BorderLayout.NORTH);

        JPanel keypadPanel = new JPanel(new GridLayout(6, 4, 8, 8));
        keypadPanel.setBackground(COLOR_BG);
        keypadPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        String[] buttons = {
            "√x", "x²", "x^y", "y√x",
            "AC", "+/-", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "⌫", "0", ".", "="
        };

        for (String text : buttons) {
            Color bg;
            if (text.equals("=")) {
                bg = COLOR_BTN_EQ;
            } else if (text.matches("[÷×\\-\\+]")) {
                bg = COLOR_BTN_OP;
            } else if (text.matches("√x|x²|x\\^y|y√x")) {
                bg = COLOR_BTN_FUNC;
            } else if (text.matches("AC|\\+/-|%|⌫")) {
                bg = COLOR_BTN_TOP;
            } else {
                bg = COLOR_BTN_NUM;
            }

            RoundButton btn = new RoundButton(text, 22, bg);
            btn.setFont(new Font("SansSerif", Font.PLAIN, text.length() > 2 ? 18 : 24));
            btn.addActionListener(e -> tekanTombol(e.getActionCommand()));
            keypadPanel.add(btn);
        }

        calcPanel.add(keypadPanel, BorderLayout.CENTER);
        return calcPanel;
    }

    private void setDisplay(String text) {
        displayField.setText(text);
        int len = text.length();
        int size = len > 18 ? 26 : len > 14 ? 32 : len > 11 ? 38 : len > 8 ? 44 : 48;
        displayField.setFont(new Font("SansSerif", Font.BOLD, size));
    }

    private void resetState() {
        operator = "";
        ekspresi.setLength(0);
        mulaiInputBaru = true;
    }

    private void tampilkanError(String pesan) {
        setDisplay(ERROR);
        historyLabel.setText(pesan);
        runningTotal = 0;
        resetState();
    }

    private boolean isBinary(String cmd) {
        return cmd.matches("[÷×\\-\\+]") || cmd.equals("x^y") || cmd.equals("y√x");
    }

    /** Teks operator untuk riwayat di layar. */
    private String simbol(String op) {
        switch (op) {
            case "x^y": return "^";
            case "y√x": return "√";
            default: return op;
        }
    }

    private void tekanTombol(String cmd) {
        String currentText = displayField.getText();
        boolean error = currentText.equals(ERROR);

        if (cmd.matches("[0-9]")) {
            if (mulaiInputBaru || currentText.equals("0") || error) {
                setDisplay(cmd);
                mulaiInputBaru = false;
            } else if (currentText.replace("-", "").replace(".", "").length() < MAX_DIGITS) {
                setDisplay(currentText + cmd);
            }
        } else if (cmd.equals(".")) {
            if (mulaiInputBaru || error) {
                setDisplay("0.");
                mulaiInputBaru = false;
            } else if (!currentText.contains(".")) {
                setDisplay(currentText + ".");
            }
        } else if (cmd.equals("⌫")) {
            if (!mulaiInputBaru && !error) {
                String newText = currentText.substring(0, currentText.length() - 1);
                if (newText.isEmpty() || newText.equals("-")) {
                    setDisplay("0");
                    mulaiInputBaru = true;
                } else {
                    setDisplay(newText);
                }
            }
        } else if (cmd.equals("+/-")) {
            if (!error && !currentText.equals("0")) {
                setDisplay(currentText.startsWith("-") ? currentText.substring(1) : "-" + currentText);
            }
        } else if (cmd.equals("AC")) {
            setDisplay("0");
            historyLabel.setText(" ");
            runningTotal = 0;
            resetState();
        } else if (cmd.equals("%")) {
            if (!error) {
                setDisplay(formatNumber(Double.parseDouble(currentText) / 100.0));
                mulaiInputBaru = true;
            }
        } else if (cmd.equals("√x") || cmd.equals("x²")) {
            if (!error) fungsiUnary(cmd, Double.parseDouble(currentText));
        } else if (isBinary(cmd)) {
            if (!error) operatorBiner(cmd, Double.parseDouble(currentText));
        } else if (cmd.equals("=")) {
            if (!error && !operator.isEmpty()) samaDengan(Double.parseDouble(currentText));
        }
    }

    private void fungsiUnary(String cmd, double val) {
        double hasil;
        String label;
        if (cmd.equals("√x")) {
            if (val < 0) {
                tampilkanError("Akar bilangan negatif tidak valid");
                return;
            }
            hasil = Math.sqrt(val);
            label = "√(" + formatNumber(val) + ")";
        } else {
            hasil = val * val;
            label = "sqr(" + formatNumber(val) + ")";
        }
        if (Double.isNaN(hasil) || Double.isInfinite(hasil)) {
            tampilkanError("Hasil terlalu besar");
            return;
        }
        String fmt = formatNumber(hasil);
        riwayatOperasiList.add(label + " = " + fmt);
        historyLabel.setText(label);
        setDisplay(fmt);
        historyBadge.setText("🕒 " + fmt);
        mulaiInputBaru = true;
    }

    private void operatorBiner(String cmd, double currentNum) {
        if (operator.isEmpty()) {
            runningTotal = currentNum;
            ekspresi.append(formatNumber(currentNum)).append(" ").append(simbol(cmd)).append(" ");
        } else if (mulaiInputBaru) {
            // ganti operator terakhir
            ekspresi.setLength(ekspresi.length() - simbol(operator).length() - 1);
            ekspresi.append(simbol(cmd)).append(" ");
        } else {
            double hasil = hitung(runningTotal, operator, currentNum);
            if (Double.isNaN(hasil) || Double.isInfinite(hasil)) {
                tampilkanError(pesanError(operator, currentNum));
                return;
            }
            runningTotal = hasil;
            ekspresi.append(formatNumber(currentNum)).append(" ").append(simbol(cmd)).append(" ");
            setDisplay(formatNumber(runningTotal));
        }
        operator = cmd;
        historyLabel.setText(ekspresi.toString());
        mulaiInputBaru = true;
    }

    private void samaDengan(double currentNum) {
        double hasil = hitung(runningTotal, operator, currentNum);
        if (Double.isNaN(hasil) || Double.isInfinite(hasil)) {
            tampilkanError(pesanError(operator, currentNum));
            return;
        }
        ekspresi.append(formatNumber(currentNum));
        String fmt = formatNumber(hasil);
        riwayatOperasiList.add(ekspresi + " = " + fmt);

        historyLabel.setText(ekspresi.toString());
        setDisplay(fmt);
        historyBadge.setText("🕒 " + fmt);
        runningTotal = hasil;
        resetState();
    }

    private String pesanError(String op, double b) {
        if (op.equals("÷") && b == 0) return "Tidak dapat dibagi 0";
        if (op.equals("y√x")) return "Akar tidak valid";
        if (op.equals("x^y")) return "Pangkat tidak valid";
        return "Hasil tidak valid";
    }

    private void tampilkanDaftarRiwayat() {
        JDialog dialog = new JDialog(this, "Riwayat Operasi", true);
        dialog.setSize(320, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Seluruh Operasi");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(COLOR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(COLOR_BG);
        textArea.setForeground(COLOR_TEXT);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));

        if (riwayatOperasiList.isEmpty()) {
            textArea.setText("Belum ada riwayat operasi.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String r : riwayatOperasiList) {
                sb.append(r).append("\n\n");
            }
            textArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /** Mengembalikan NaN/Infinity bila operasi tidak valid. */
    private double hitung(double a, String op, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : Double.NaN;
            case "x^y": return Math.pow(a, b);
            case "y√x": // akar ke-a dari b
                if (a == 0) return Double.NaN;
                if (b < 0) {
                    // akar ganjil dari bilangan negatif masih valid
                    boolean ganjil = a == Math.rint(a) && Math.abs(a % 2) == 1;
                    return ganjil ? -Math.pow(-b, 1.0 / a) : Double.NaN;
                }
                return Math.pow(b, 1.0 / a);
            default: return b;
        }
    }

    /** Format angka: dibulatkan 12 digit signifikan (menghilangkan 0.30000000000000004), eksponen untuk angka ekstrem. */
    private String formatNumber(double num) {
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return ERROR;
        }
        if (num == 0) return "0";

        BigDecimal bd = new BigDecimal(num).round(new MathContext(12)).stripTrailingZeros();
        double abs = Math.abs(num);
        if (abs >= 1e12 || abs < 1e-9) {
            String s = String.format("%.8e", bd.doubleValue());
            // buang nol di belakang mantissa: 1.50000000e+15 -> 1.5e+15
            int idx = s.indexOf('e');
            String mant = s.substring(0, idx).replaceAll("0+$", "").replaceAll("\\.$", "");
            return mant + s.substring(idx);
        }
        return bd.toPlainString();
    }

    /** Satu kategori konversi: nama unit + faktor ke unit dasar (suhu ditangani khusus). */
    private static class Kategori {
        final String nama;
        final String[] unit;
        final double[] faktor;

        Kategori(String nama, String[] unit, double[] faktor) {
            this.nama = nama;
            this.unit = unit;
            this.faktor = faktor;
        }

        double konversi(double val, int dari, int ke) {
            if (nama.equals("Suhu")) {
                double c = dari == 0 ? val : dari == 1 ? (val - 32) * 5 / 9 : val - 273.15;
                return ke == 0 ? c : ke == 1 ? c * 9 / 5 + 32 : c + 273.15;
            }
            return val * faktor[dari] / faktor[ke];
        }
    }

    private static final Kategori[] KATEGORI = {
        new Kategori("Panjang",
            new String[]{"mm", "cm", "m", "km", "inci", "kaki", "yard", "mil"},
            new double[]{0.001, 0.01, 1, 1000, 0.0254, 0.3048, 0.9144, 1609.344}),
        new Kategori("Massa",
            new String[]{"mg", "g", "kg", "ton", "ons (oz)", "pon (lb)"},
            new double[]{1e-6, 0.001, 1, 1000, 0.028349523125, 0.45359237}),
        new Kategori("Suhu",
            new String[]{"°C", "°F", "K"}, null),
        new Kategori("Luas",
            new String[]{"cm²", "m²", "km²", "hektar", "ft²", "acre"},
            new double[]{1e-4, 1, 1e6, 1e4, 0.09290304, 4046.8564224}),
        new Kategori("Volume",
            new String[]{"mL", "L", "m³", "galon (US)", "cangkir (US)"},
            new double[]{0.001, 1, 1000, 3.785411784, 0.2365882365}),
        new Kategori("Waktu",
            new String[]{"ms", "detik", "menit", "jam", "hari", "minggu"},
            new double[]{0.001, 1, 60, 3600, 86400, 604800}),
        new Kategori("Kecepatan",
            new String[]{"m/s", "km/jam", "mph", "knot"},
            new double[]{1, 1 / 3.6, 0.44704, 1852 / 3600.0}),
        new Kategori("Data",
            new String[]{"bit", "B", "KB", "MB", "GB", "TB"},
            new double[]{0.125, 1, 1024, 1048576, 1073741824, 1099511627776.0})
    };

    private <T> JComboBox<T> createCombo(T[] items, int fontSize) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        combo.setBackground(COLOR_BTN_TOP);
        combo.setForeground(COLOR_TEXT);
        combo.setFocusable(false);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean hasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                l.setBackground(isSelected ? COLOR_BTN_OP : COLOR_BTN_TOP);
                l.setForeground(COLOR_TEXT);
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
        return combo;
    }

    private JPanel createConverterPanel() {
        JPanel convPanel = new JPanel(new BorderLayout());
        convPanel.setBackground(COLOR_BG);

        String[] namaKategori = new String[KATEGORI.length];
        for (int i = 0; i < KATEGORI.length; i++) namaKategori[i] = KATEGORI[i].nama;

        JComboBox<String> kategoriBox = createCombo(namaKategori, 16);
        JComboBox<String> unitDari = createCombo(KATEGORI[0].unit, 16);
        JComboBox<String> unitKe = createCombo(KATEGORI[0].unit, 16);
        unitKe.setSelectedIndex(2);

        JTextField inputField = new JTextField("0");
        styleConverterTextField(inputField);
        inputField.setEditable(false);
        JTextField resultField = new JTextField("0");
        styleConverterTextField(resultField);
        resultField.setEditable(false);
        resultField.setForeground(COLOR_TEXT_MUTED);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(COLOR_BG);
        displayPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JPanel katRow = new JPanel(new BorderLayout());
        katRow.setBackground(COLOR_BG);
        katRow.setBorder(new EmptyBorder(0, 0, 10, 0));
        katRow.add(kategoriBox, BorderLayout.CENTER);

        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setBackground(COLOR_BG);
        row1.add(inputField, BorderLayout.CENTER);
        row1.add(unitDari, BorderLayout.EAST);

        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setBackground(COLOR_BG);
        row2.setBorder(new EmptyBorder(10, 0, 0, 0));
        row2.add(resultField, BorderLayout.CENTER);
        row2.add(unitKe, BorderLayout.EAST);

        displayPanel.add(katRow);
        displayPanel.add(row1);
        displayPanel.add(row2);
        convPanel.add(displayPanel, BorderLayout.NORTH);

        Runnable update = () -> {
            int dari = unitDari.getSelectedIndex();
            int ke = unitKe.getSelectedIndex();
            if (dari < 0 || ke < 0) return;
            try {
                double val = Double.parseDouble(inputField.getText());
                resultField.setText(formatNumber(KATEGORI[kategoriBox.getSelectedIndex()].konversi(val, dari, ke)));
            } catch (NumberFormatException ex) {
                resultField.setText("0");
            }
        };

        kategoriBox.addActionListener(e -> {
            Kategori k = KATEGORI[kategoriBox.getSelectedIndex()];
            unitDari.setModel(new DefaultComboBoxModel<>(k.unit));
            unitKe.setModel(new DefaultComboBoxModel<>(k.unit));
            unitDari.setSelectedIndex(0);
            unitKe.setSelectedIndex(Math.min(1, k.unit.length - 1));
            update.run();
        });
        unitDari.addActionListener(e -> update.run());
        unitKe.addActionListener(e -> update.run());

        JPanel keypadPanel = new JPanel(new GridLayout(5, 3, 8, 8));
        keypadPanel.setBackground(COLOR_BG);
        keypadPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        String[] convBtns = {
            "AC", "+/-", "⇅",
            "7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            ".", "0", "⌫"
        };

        for (String text : convBtns) {
            RoundButton btn = new RoundButton(text, 22, text.matches("AC|\\+/-|⇅|⌫") ? COLOR_BTN_TOP : COLOR_BTN_NUM);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 24));

            btn.addActionListener(e -> {
                String cmd = e.getActionCommand();
                String curr = inputField.getText();

                if (cmd.equals("⇅")) {
                    int a = unitDari.getSelectedIndex();
                    unitDari.setSelectedIndex(unitKe.getSelectedIndex());
                    unitKe.setSelectedIndex(a);
                    return;
                }

                if (cmd.matches("[0-9]")) {
                    if (curr.replace("-", "").replace(".", "").length() < MAX_DIGITS) {
                        inputField.setText(curr.equals("0") ? cmd : curr + cmd);
                    }
                } else if (cmd.equals("AC")) {
                    inputField.setText("0");
                } else if (cmd.equals("+/-")) {
                    if (!curr.equals("0")) {
                        inputField.setText(curr.startsWith("-") ? curr.substring(1) : "-" + curr);
                    }
                } else if (cmd.equals("⌫")) {
                    String n = curr.substring(0, curr.length() - 1);
                    inputField.setText(n.isEmpty() || n.equals("-") ? "0" : n);
                } else if (cmd.equals(".") && !curr.contains(".")) {
                    inputField.setText(curr + ".");
                }
                update.run();
            });

            keypadPanel.add(btn);
        }

        update.run();
        convPanel.add(keypadPanel, BorderLayout.CENTER);
        return convPanel;
    }

    private void styleConverterTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 36));
        field.setForeground(COLOR_TEXT);
        field.setBackground(COLOR_BG);
        field.setCaretColor(COLOR_BG);
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BTN_TOP));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppKalkulator calc = new AppKalkulator();
            calc.setLocationRelativeTo(null);
            calc.setVisible(true);
        });
    }
}
