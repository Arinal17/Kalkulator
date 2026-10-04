import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AppKalkulator extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private final Color COLOR_BG = new Color(28, 28, 30);
    private final Color COLOR_BTN_NUM = new Color(44, 45, 49);
    private final Color COLOR_BTN_TOP = new Color(64, 66, 73);
    private final Color COLOR_BTN_OP = new Color(10, 132, 255);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(150, 150, 150);
    private final Color COLOR_BADGE = new Color(50, 52, 58);

    private List<String> riwayatOperasiList = new ArrayList<>();

    public AppKalkulator() {
        setTitle("Kalkulator & Converter");
        setSize(380, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel(new GridLayout(1, 2));
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JButton btnMenuCalc = createHeaderButton("Calculator", true);
        JButton btnMenuConv = createHeaderButton("Converter", false);

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
            btnMenuCalc.setBackground(COLOR_BTN_OP);
            btnMenuConv.setBackground(COLOR_BTN_TOP);
        });

        btnMenuConv.addActionListener(e -> {
            cardLayout.show(mainPanel, "Converter");
            btnMenuConv.setBackground(COLOR_BTN_OP);
            btnMenuCalc.setBackground(COLOR_BTN_TOP);
        });
    }

    private JButton createHeaderButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(COLOR_TEXT);
        btn.setBackground(active ? COLOR_BTN_OP : COLOR_BTN_TOP);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
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

        JLabel historyBadge = new JLabel("🕒 Riwayat") {
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

        JTextField displayField = new JTextField("0");
        displayField.setFont(new Font("SansSerif", Font.BOLD, 48));
        displayField.setForeground(COLOR_TEXT);
        displayField.setBackground(COLOR_BG);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(null);
        displayField.setEditable(false);

        JLabel historyLabel = new JLabel(" ");
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

        JPanel keypadPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        keypadPanel.setBackground(COLOR_BG);
        keypadPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        String[] buttons = {
            "AC", "+/-", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "⌫", "0", ".", "="
        };

        final double[] runningTotal = {0};
        final String[] operator = {""};
        final boolean[] mulaiInputBaru = {true};
        final StringBuilder ekspresi = new StringBuilder();

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 24));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);

            if (text.matches("[÷×\\-\\+=]")) {
                btn.setBackground(COLOR_BTN_OP);
                btn.setForeground(COLOR_TEXT);
            } else if (text.matches("AC|\\+/-|%|⌫")) {
                btn.setBackground(COLOR_BTN_TOP);
                btn.setForeground(COLOR_TEXT);
            } else {
                btn.setBackground(COLOR_BTN_NUM);
                btn.setForeground(COLOR_TEXT);
            }

            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = e.getActionCommand();
                    String currentText = displayField.getText();

                    if (cmd.matches("[0-9]")) {
                        if (mulaiInputBaru[0] || currentText.equals("0") || currentText.equals("Error")) {
                            displayField.setText(cmd);
                            mulaiInputBaru[0] = false;
                        } else {
                            if (currentText.replace("-", "").replace(".", "").length() < 15) {
                                displayField.setText(currentText + cmd);
                            }
                        }
                    } else if (cmd.equals(".")) {
                        if (mulaiInputBaru[0] || currentText.equals("Error")) {
                            displayField.setText("0.");
                            mulaiInputBaru[0] = false;
                        } else if (!currentText.contains(".")) {
                            displayField.setText(currentText + ".");
                        }
                    } else if (cmd.equals("⌫")) {
                        if (!mulaiInputBaru[0] && currentText.length() > 0 && !currentText.equals("Error")) {
                            String newText = currentText.substring(0, currentText.length() - 1);
                            if (newText.isEmpty() || newText.equals("-")) {
                                displayField.setText("0");
                                mulaiInputBaru[0] = true;
                            } else {
                                displayField.setText(newText);
                            }
                        }
                    } else if (cmd.equals("+/-")) {
                        if (!currentText.equals("0") && !currentText.equals("Error")) {
                            if (currentText.startsWith("-")) {
                                displayField.setText(currentText.substring(1));
                            } else {
                                displayField.setText("-" + currentText);
                            }
                        }
                    } else if (cmd.equals("%")) {
                        if (!currentText.equals("Error")) {
                            double val = Double.parseDouble(currentText) / 100.0;
                            displayField.setText(formatNumber(val));
                        }
                    } else if (cmd.equals("AC")) {
                        displayField.setText("0");
                        historyLabel.setText(" ");
                        runningTotal[0] = 0;
                        operator[0] = "";
                        ekspresi.setLength(0);
                        mulaiInputBaru[0] = true;
                    } else if (cmd.matches("[÷×\\-\\+]")) {
                        if (currentText.equals("Error")) return;

                        double currentNum = Double.parseDouble(currentText);
                        if (operator[0].isEmpty()) {
                            runningTotal[0] = currentNum;
                            ekspresi.append(formatNumber(currentNum)).append(" ").append(cmd).append(" ");
                        } else if (mulaiInputBaru[0]) {
                            if (ekspresi.length() >= 3) {
                                ekspresi.setLength(ekspresi.length() - 2);
                                ekspresi.append(cmd).append(" ");
                            }
                        } else {
                            if (operator[0].equals("÷") && currentNum == 0) {
                                displayField.setText("Error");
                                operator[0] = "";
                                ekspresi.setLength(0);
                                mulaiInputBaru[0] = true;
                                return;
                            }
                            runningTotal[0] = hitung(runningTotal[0], operator[0], currentNum);
                            ekspresi.append(formatNumber(currentNum)).append(" ").append(cmd).append(" ");
                            displayField.setText(formatNumber(runningTotal[0]));
                        }
                        operator[0] = cmd;
                        historyLabel.setText(ekspresi.toString());
                        mulaiInputBaru[0] = true;
                    } else if (cmd.equals("=")) {
                        if (!operator[0].isEmpty() && !currentText.equals("Error")) {
                            double currentNum = Double.parseDouble(currentText);
                            if (operator[0].equals("÷") && currentNum == 0) {
                                displayField.setText("Error");
                                historyLabel.setText("Tidak dapat dibagi 0");
                                operator[0] = "";
                                ekspresi.setLength(0);
                                mulaiInputBaru[0] = true;
                                return;
                            }
                            double hasil = hitung(runningTotal[0], operator[0], currentNum);
                            ekspresi.append(formatNumber(currentNum));
                            String itemRiwayat = ekspresi.toString() + " = " + formatNumber(hasil);
                            riwayatOperasiList.add(itemRiwayat);

                            historyLabel.setText(ekspresi.toString());
                            displayField.setText(formatNumber(hasil));
                            historyBadge.setText("🕒 " + formatNumber(hasil));
                            operator[0] = "";
                            ekspresi.setLength(0);
                            mulaiInputBaru[0] = true;
                        }
                    }
                }
            });

            keypadPanel.add(btn);
        }

        calcPanel.add(keypadPanel, BorderLayout.CENTER);
        return calcPanel;
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

    private double hitung(double a, String op, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : 0;
            default: return b;
        }
    }

    private String formatNumber(double num) {
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return "Error";
        }
        
        String plainStr;
        if (num == (long) num) {
            plainStr = String.format("%d", (long) num);
        } else {
            plainStr = String.format("%s", num);
        }

        String digitsOnly = plainStr.replace("-", "").replace(".", "");
        if (digitsOnly.length() > 10 || Math.abs(num) >= 1e10 || (Math.abs(num) > 0 && Math.abs(num) < 1e-6)) {
            return String.format("%.4e", num);
        }

        return plainStr;
    }

    private JPanel createConverterPanel() {
        JPanel convPanel = new JPanel(new BorderLayout());
        convPanel.setBackground(COLOR_BG);

        JPanel displayPanel = new JPanel(new GridLayout(3, 1));
        displayPanel.setBackground(COLOR_BG);
        displayPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Length ▼");
        titleLabel.setForeground(COLOR_TEXT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(COLOR_BG);
        JTextField inputField = new JTextField("0");
        styleConverterTextField(inputField);
        JLabel unit1 = new JLabel("ft ▼ ");
        unit1.setForeground(COLOR_TEXT_MUTED);
        unit1.setFont(new Font("SansSerif", Font.PLAIN, 18));
        row1.add(inputField, BorderLayout.CENTER);
        row1.add(unit1, BorderLayout.EAST);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.setBackground(COLOR_BG);
        JTextField resultField = new JTextField("0");
        styleConverterTextField(resultField);
        resultField.setEditable(false);
        resultField.setForeground(COLOR_TEXT_MUTED);
        JLabel unit2 = new JLabel("m ▼ ");
        unit2.setForeground(COLOR_TEXT_MUTED);
        unit2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        row2.add(resultField, BorderLayout.CENTER);
        row2.add(unit2, BorderLayout.EAST);

        displayPanel.add(titleLabel);
        displayPanel.add(row1);
        displayPanel.add(row2);
        convPanel.add(displayPanel, BorderLayout.NORTH);

        JPanel keypadPanel = new JPanel(new GridLayout(5, 3, 5, 5));
        keypadPanel.setBackground(COLOR_BG);
        keypadPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        String[] convBtns = {
            "AC", "", "⇅",
            "7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            ".", "0", "⌫"
        };

        for (String text : convBtns) {
            if (text.isEmpty()) {
                JPanel emptyCell = new JPanel();
                emptyCell.setBackground(COLOR_BG);
                keypadPanel.add(emptyCell);
                continue;
            }

            JButton btn = new JButton(text);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 24));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setBackground(text.matches("AC|⇅") ? COLOR_BTN_TOP : COLOR_BTN_NUM);
            btn.setForeground(COLOR_TEXT);

            btn.addActionListener(e -> {
                String cmd = e.getActionCommand();
                String curr = inputField.getText();

                if (cmd.matches("[0-9]")) {
                    inputField.setText(curr.equals("0") ? cmd : curr + cmd);
                } else if (cmd.equals("AC")) {
                    inputField.setText("0");
                } else if (cmd.equals("⌫")) {
                    if (curr.length() > 1) {
                        inputField.setText(curr.substring(0, curr.length() - 1));
                    } else {
                        inputField.setText("0");
                    }
                } else if (cmd.equals(".") && !curr.contains(".")) {
                    inputField.setText(curr + ".");
                }

                try {
                    double val = Double.parseDouble(inputField.getText());
                    resultField.setText(formatNumber(val * 0.3048));
                } catch (Exception ex) {
                    resultField.setText("0");
                }
            });

            keypadPanel.add(btn);
        }

        convPanel.add(keypadPanel, BorderLayout.CENTER);
        return convPanel;
    }

    private void styleConverterTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 36));
        field.setForeground(COLOR_TEXT);
        field.setBackground(COLOR_BG);
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