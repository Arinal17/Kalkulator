import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class AppKalkulator extends JFrame {

    private static final String ERROR = "Error";
    private static final int MAX_DIGITS = 15;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // ===== Palet warna: tema lavender + indigo =====
    private static final Color COLOR_BG          = new Color(238, 240, 255); // latar jendela
    private static final Color COLOR_DISPLAY     = new Color(248, 249, 255); // kotak layar
    private static final Color COLOR_BTN_NUM     = new Color(253, 253, 255); // angka (putih)
    private static final Color COLOR_BTN_TOP     = new Color(243, 244, 246); // AC, %, hapus (abu muda)
    private static final Color COLOR_BTN_FUNC    = new Color(243, 232, 255); // √x, x², xʸ (ungu muda)
    private static final Color COLOR_BTN_OP      = new Color(224, 231, 255); // + − × ÷ (biru muda)
    private static final Color COLOR_BTN_EQ      = new Color(0xFF, 0xC6, 0xED); // = , AC, ⌫, tab aktif (pink #FFC6ED)
    private static final Color COLOR_TEXT        = new Color(31, 41, 55);    // teks utama
    private static final Color COLOR_TEXT_OP     = new Color(67, 56, 202);   // teks operator
    private static final Color COLOR_TEXT_FUNC   = new Color(126, 34, 206);  // teks fungsi
    private static final Color COLOR_TEXT_MUTED  = new Color(107, 114, 128);
    private static final Color COLOR_BADGE       = new Color(224, 231, 255);
    private static final Color COLOR_BORDER      = new Color(209, 213, 219);

    private final List<String> riwayatOperasiList = new ArrayList<>();

    // state kalkulator
    private JTextField displayField;
    private JLabel historyLabel;
    private JLabel historyBadge;
    private boolean mulaiInputBaru = true;   // ketikan angka berikutnya mengganti layar
    private boolean menungguOperan = false;  // baru menekan operator, angka kedua belum diisi
    private String operandLabel = null;      // tampilan operan hasil √x, x², % di riwayat, mis. "√(9)"
    private Double pendingHasil = null;      // hasil √x / x² yang baru muncul setelah "=" ditekan

    // ekspresi yang sedang disusun; baru dihitung saat tombol "=" ditekan
    private final List<Double> nilai = new ArrayList<>();
    private final List<String> labelNilai = new ArrayList<>();
    private final List<String> ops = new ArrayList<>();

    public AppKalkulator() {
        setTitle("Kalkulator & Converter");
        setSize(400, 780);
        setMinimumSize(new Dimension(360, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        RoundButton btnMenuCalc = new RoundButton("Calculator", 18, COLOR_BTN_EQ);
        RoundButton btnMenuConv = new RoundButton("Converter", 18, COLOR_BTN_OP);
        btnMenuCalc.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnMenuConv.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnMenuCalc.setForeground(COLOR_TEXT);
        btnMenuConv.setForeground(COLOR_TEXT_OP);

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
            btnMenuCalc.setBase(COLOR_BTN_EQ);
            btnMenuCalc.setForeground(COLOR_TEXT);
            btnMenuConv.setBase(COLOR_BTN_OP);
            btnMenuConv.setForeground(COLOR_TEXT_OP);
        });

        btnMenuConv.addActionListener(e -> {
            cardLayout.show(mainPanel, "Converter");
            btnMenuConv.setBase(COLOR_BTN_EQ);
            btnMenuConv.setForeground(COLOR_TEXT);
            btnMenuCalc.setBase(COLOR_BTN_OP);
            btnMenuCalc.setForeground(COLOR_TEXT_OP);
        });
    }

    private enum Glyph { NONE, AKAR, KUADRAT, PANGKAT, AKAR_Y, HAPUS }

    //Tombol rounded-rectangle dengan bayangan tipis, efek hover dan tekan.
    private static class RoundButton extends JButton {
        private Color base;
        private final int radius;
        private boolean hover;
        private Glyph glyph = Glyph.NONE;

        RoundButton(String text, int radius, Color base) {
            super(text);
            this.radius = radius;
            this.base = base;
            setForeground(COLOR_TEXT);
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

        void setGlyph(Glyph g) {
            glyph = g;
            setText("");
            repaint();
        }

        private static Color gelapkan(Color c, double f) {
            return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color c = base;
            if (getModel().isPressed()) {
                c = gelapkan(base, 0.88);
            } else if (hover) {
                c = gelapkan(base, 0.95);
            }
            int w = getWidth(), h = getHeight();
            // bayangan tipis
            g2.setColor(new Color(79, 70, 229, 22));
            g2.fillRoundRect(0, 3, w, h - 3, radius, radius);
            // badan tombol
            g2.setColor(c);
            g2.fillRoundRect(0, 0, w, h - 3, radius, radius);
            if (glyph != Glyph.NONE) {
                g2.setColor(getForeground());
                gambarGlyph(g2);
                g2.dispose();
                return;
            }
            g2.dispose();
            super.paintComponent(g);
        }

        private void gambarGlyph(Graphics2D g2) {
            double w = getWidth(), h = getHeight() - 3;
            float b = (float) (Math.min(w, h) * 0.34);   // ukuran huruf utama
            float k = b * 0.58f;                         // ukuran pangkat / indeks
            Font fb = getFont().deriveFont(Font.PLAIN, b);
            Font fk = getFont().deriveFont(Font.PLAIN, k);
            FontMetrics mb = g2.getFontMetrics(fb);
            FontMetrics mk = g2.getFontMetrics(fk);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setStroke(new BasicStroke(Math.max(1.6f, b * 0.075f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (glyph) {
                case KUADRAT:
                case PANGKAT: {
                    String pangkat = glyph == Glyph.KUADRAT ? "2" : "y";
                    float lx = mb.stringWidth("x");
                    float lp = mk.stringWidth(pangkat);
                    float x0 = (float) ((w - (lx + lp + 1)) / 2);
                    float y0 = (float) (h / 2 + 0.43 * b);
                    g2.setFont(fb);
                    g2.drawString("x", x0, y0);
                    g2.setFont(fk);
                    g2.drawString(pangkat, x0 + lx + 1, y0 - 0.45f * b);
                    break;
                }
                case AKAR:
                case AKAR_Y: {
                    boolean adaIndeks = glyph == Glyph.AKAR_Y;
                    float lx = mb.stringWidth("x");
                    float lead = adaIndeks ? mk.stringWidth("y") + 0.02f * b : 0;
                    float total = lead + 0.58f * b + 0.06f * b + lx + 0.12f * b;
                    float rx = (float) ((w - total) / 2) + lead;          // awal tanda akar
                    float y0 = (float) (h / 2 + (adaIndeks ? 0.44 : 0.35) * b);
                    float yAtas = y0 - 0.74f * b;                         // garis di atas angka
                    Path2D.Float akar = new Path2D.Float();
                    akar.moveTo(rx, y0 - 0.30f * b);
                    akar.lineTo(rx + 0.10f * b, y0 - 0.38f * b);
                    akar.lineTo(rx + 0.30f * b, y0 + 0.05f * b);
                    akar.lineTo(rx + 0.58f * b, yAtas);
                    akar.lineTo(rx + 0.58f * b + 0.06f * b + lx + 0.12f * b, yAtas);
                    g2.draw(akar);
                    g2.setFont(fb);
                    g2.drawString("x", rx + 0.64f * b, y0);
                    if (adaIndeks) {
                        g2.setFont(fk);
                        g2.drawString("y", rx - lead, y0 - 0.52f * b);
                    }
                    break;
                }
                case HAPUS: {
                    float ih = 0.80f * b, iw = 1.35f * b, miring = ih * 0.5f;
                    float x0 = (float) ((w - iw) / 2), y0 = (float) ((h - ih) / 2);
                    Path2D.Float ikon = new Path2D.Float();
                    ikon.moveTo(x0, y0 + ih / 2);
                    ikon.lineTo(x0 + miring, y0);
                    ikon.lineTo(x0 + iw, y0);
                    ikon.lineTo(x0 + iw, y0 + ih);
                    ikon.lineTo(x0 + miring, y0 + ih);
                    ikon.closePath();
                    g2.draw(ikon);
                    float cx = x0 + miring + (iw - miring) / 2, cy = y0 + ih / 2, d = ih * 0.17f;
                    g2.draw(new Line2D.Float(cx - d, cy - d, cx + d, cy + d));
                    g2.draw(new Line2D.Float(cx - d, cy + d, cx + d, cy - d));
                    break;
                }
                default:
                    break;
            }
        }
    }

    /** Combo box rounded dengan warna terang. */
    private static class RoundedComboBox<T> extends JComboBox<T> {

        private final int radius = 20;

        RoundedComboBox(T[] items) {
            super(items);
            setOpaque(false);
            setBorder(null);
            setFocusable(false);
            UIManager.put("ComboBox.squareButton", Boolean.FALSE);
            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton b = new JButton() {
                        @Override
                        public void paint(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(COLOR_TEXT_OP);
                            float cx = getWidth() / 2f, cy = getHeight() / 2f, s = 5f;
                            Path2D.Float segitiga = new Path2D.Float();
                            segitiga.moveTo(cx - s, cy - s / 2);
                            segitiga.lineTo(cx + s, cy - s / 2);
                            segitiga.lineTo(cx, cy + s / 2);
                            segitiga.closePath();
                            g2.fill(segitiga);
                            g2.dispose();
                        }

                        @Override
                        public Dimension getPreferredSize() {
                            return new Dimension(26, 30);
                        }
                    };
                    b.setBorder(null);
                    b.setContentAreaFilled(false);
                    b.setFocusable(false);
                    b.setOpaque(false);
                    return b;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // dikosongkan supaya tidak ada blok highlight
                }

                @Override
                protected ComboPopup createPopup() {
                    BasicComboPopup p = new BasicComboPopup(comboBox);
                    p.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
                    p.getList().setBackground(COLOR_BTN_NUM);
                    return p;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Membatasi seluruh tampilan JComboBox ke bentuk rounded
            Shape rounded = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.clip(rounded);

            g2.setColor(COLOR_BTN_NUM);
            g2.fillRect(0, 0, getWidth(), getHeight());

            super.paintComponent(g2);
            g2.dispose();

            // Garis tepi rounded
            Graphics2D border = (Graphics2D) g.create();
            border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            border.setColor(COLOR_BORDER);
            border.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            border.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {
        }
    }

    private static String labelTombol(String cmd) {
        switch (cmd) {
            case "-": return "−";
            case "+/-": return "±";
            default: return cmd;
        }
    }

    private static Glyph glyphTombol(String cmd) {
        switch (cmd) {
            case "√x": return Glyph.AKAR;
            case "x²": return Glyph.KUADRAT;
            case "x^y": return Glyph.PANGKAT;
            case "y√x": return Glyph.AKAR_Y;
            case "⌫": return Glyph.HAPUS;
            default: return Glyph.NONE;
        }
    }

    private RoundButton buatTombol(String cmd, Color bg, Color fg, int ukuranFont) {
        RoundButton btn = new RoundButton(labelTombol(cmd), 28, bg);
        btn.setActionCommand(cmd);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.PLAIN, ukuranFont));
        Glyph g = glyphTombol(cmd);
        if (g != Glyph.NONE) btn.setGlyph(g);
        return btn;
    }

    private JPanel createCalculatorPanel() {
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBackground(COLOR_BG);

        // kotak layar putih-lavender dengan sudut membulat
        JPanel displayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_DISPLAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setOpaque(false);
        displayPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeRow.setOpaque(false);

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
        historyBadge.setForeground(COLOR_TEXT_OP);
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
        displayField.setOpaque(false);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(new EmptyBorder(8, 0, 4, 0));
        displayField.setEditable(false);
        displayField.setCaretColor(COLOR_DISPLAY);

        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        historyLabel.setForeground(COLOR_TEXT_MUTED);
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel historyRow = new JPanel(new BorderLayout());
        historyRow.setOpaque(false);
        historyRow.add(historyLabel, BorderLayout.EAST);

        displayPanel.add(badgeRow);
        displayPanel.add(displayField);
        displayPanel.add(historyRow);

        JPanel displayWrap = new JPanel(new BorderLayout());
        displayWrap.setBackground(COLOR_BG);
        displayWrap.setBorder(new EmptyBorder(10, 20, 10, 20));
        displayWrap.add(displayPanel, BorderLayout.CENTER);
        calcPanel.add(displayWrap, BorderLayout.NORTH);

        JPanel keypadPanel = new JPanel(new GridLayout(6, 4, 10, 10));
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
            Color fg = COLOR_TEXT;
            if (text.equals("=")) {
                bg = COLOR_BTN_EQ;
            } else if (text.matches("[÷×\\-\\+]")) {
                bg = COLOR_BTN_OP;
                fg = COLOR_TEXT_OP;
            } else if (text.matches("√x|x²|x\\^y|y√x")) {
                bg = COLOR_BTN_FUNC;
                fg = COLOR_TEXT_FUNC;
            } else if (text.matches("AC|⌫")) {
                bg = COLOR_BTN_EQ;
            } else if (text.equals("%")) {
                bg = COLOR_BTN_TOP;
            } else {
                bg = COLOR_BTN_NUM;
            }

            boolean tombolOperator = text.matches("[÷×\\-\\+=]");
            RoundButton btn = buatTombol(text, bg, fg, tombolOperator ? 30 : 24);
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
        nilai.clear();
        labelNilai.clear();
        ops.clear();
        operandLabel = null;
        pendingHasil = null;
        menungguOperan = false;
        mulaiInputBaru = true;
    }

    //Teks riwayat dipotong dari kiri (diawali "…") kalau terlalu panjang untuk layar.
    private void setHistory(String teks) {
        FontMetrics fm = historyLabel.getFontMetrics(historyLabel.getFont());
        int maks = Math.max(200, displayField.getWidth() - 8);
        String t = teks;
        if (fm.stringWidth(t) > maks) {
            while (t.length() > 1 && fm.stringWidth("…" + t) > maks) {
                t = t.substring(1);
            }
            t = "…" + t;
        }
        historyLabel.setText(t);
    }

    private void tampilkanError(String pesan) {
        setDisplay(ERROR);
        setHistory(pesan);
        resetState();
    }

    private boolean isBinary(String cmd) {
        return cmd.matches("[÷×\\-\\+]") || cmd.equals("x^y") || cmd.equals("y√x");
    }

    //Teks operator untuk riwayat di layar.
    private String simbol(String op) {
        switch (op) {
            case "-": return "−";
            case "x^y": return "^";
            case "y√x": return "√";
            default: return op;
        }
    }

    private static String rapikan(String angka) {
        return angka.startsWith("-") ? "−" + angka.substring(1) : angka;
    }

    //Teks sebuah operan di riwayat; angka negatif di tengah ekspresi diberi kurung.
    private String labelOperan(double v) {
        if (operandLabel != null) return operandLabel;
        String s = rapikan(formatNumber(v));
        return (v < 0 && !ops.isEmpty()) ? "(" + s + ")" : s;
    }

    private String bangunEkspresi() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nilai.size(); i++) {
            sb.append(labelNilai.get(i));
            if (i < ops.size()) sb.append(" ").append(simbol(ops.get(i))).append(" ");
        }
        return sb.toString();
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
            operandLabel = null;
            pendingHasil = null;
            menungguOperan = false;
        } else if (cmd.equals(".")) {
            if (mulaiInputBaru || error) {
                setDisplay("0.");
                mulaiInputBaru = false;
            } else if (!currentText.contains(".")) {
                setDisplay(currentText + ".");
            }
            operandLabel = null;
            pendingHasil = null;
            menungguOperan = false;
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
            if (!error && !currentText.equals("0") && pendingHasil == null) {
                setDisplay(currentText.startsWith("-") ? currentText.substring(1) : "-" + currentText);
                if (operandLabel != null) operandLabel = "−(" + operandLabel + ")";
                menungguOperan = false;
            }
        } else if (cmd.equals("AC")) {
            setDisplay("0");
            historyLabel.setText(" ");
            resetState();
        } else if (cmd.equals("%")) {
            if (!error) {
                double val = pendingHasil != null ? pendingHasil : Double.parseDouble(currentText);
                pendingHasil = null;
                operandLabel = (operandLabel != null ? operandLabel : rapikan(formatNumber(val))) + "%";
                setDisplay(formatNumber(val / 100.0));
                mulaiInputBaru = true;
                menungguOperan = false;
            }
        } else if (cmd.equals("√x") || cmd.equals("x²")) {
            if (!error) fungsiUnary(cmd, Double.parseDouble(currentText));
        } else if (isBinary(cmd)) {
            if (!error) operatorBiner(cmd, Double.parseDouble(currentText));
        } else if (cmd.equals("=")) {
            if (!error && (!ops.isEmpty() || pendingHasil != null)) samaDengan(Double.parseDouble(currentText));
        }
    }

    //√x dan x² langsung diterapkan ke angka di layar (operan saat ini), bukan menunggu "=".
    private void fungsiUnary(String cmd, double val) {
        if (pendingHasil != null) val = pendingHasil;   // untuk kasus √ lalu x² berturut-turut
        String dasar = operandLabel != null ? operandLabel : rapikan(formatNumber(val));
        boolean sederhana = operandLabel == null && val >= 0;
        double hasil;
        String label;
        if (cmd.equals("√x")) {
            if (val < 0) {
                tampilkanError("Akar bilangan negatif tidak valid");
                return;
            }
            hasil = Math.sqrt(val);
            label = "√(" + dasar + ")";
        } else {
            hasil = val * val;
            label = sederhana ? dasar + "²" : "(" + dasar + ")²";
        }
        if (Double.isNaN(hasil) || Double.isInfinite(hasil)) {
            tampilkanError("Hasil terlalu besar");
            return;
        }
        operandLabel = label;
        pendingHasil = hasil;
        menungguOperan = false;
        mulaiInputBaru = true;
        setHistory(ops.isEmpty() ? label : bangunEkspresi() + label);
    }

    private void operatorBiner(String cmd, double currentNum) {
        if (pendingHasil != null) currentNum = pendingHasil;
        if (menungguOperan) {
            ops.set(ops.size() - 1, cmd);
        } else {
            nilai.add(currentNum);
            labelNilai.add(labelOperan(currentNum));
            ops.add(cmd);
            operandLabel = null;
        }
        pendingHasil = null;
        menungguOperan = true;
        mulaiInputBaru = true;
        setHistory(bangunEkspresi());
    }

    private void samaDengan(double currentNum) {
        if (pendingHasil != null) currentNum = pendingHasil;
        nilai.add(currentNum);
        labelNilai.add(labelOperan(currentNum));
        String teks = bangunEkspresi();
        double hasil;
        try {
            hasil = evaluasi();
        } catch (ArithmeticException ex) {
            tampilkanError(ex.getMessage());
            return;
        }
        String fmt = formatNumber(hasil);
        riwayatOperasiList.add(teks + " = " + fmt);
        setHistory(teks + " =");
        setDisplay(fmt);
        historyBadge.setText("🕒 " + fmt);
        resetState();
    }

    private static int prioritas(String op) {
        switch (op) {
            case "x^y":
            case "y√x": return 3;
            case "×":
            case "÷": return 2;
            default: return 1;
        }
    }

    //Hitung seluruh ekspresi: pangkat/akar dulu, lalu × ÷, lalu + −.
    private double evaluasi() {
        List<Double> a = new ArrayList<>(nilai);
        List<String> o = new ArrayList<>(ops);

        // pangkat dan akar dihitung dari kanan ke kiri
        for (int i = o.size() - 1; i >= 0; i--) {
            if (prioritas(o.get(i)) == 3) gabung(a, o, i);
        }
        // sisanya dari kiri ke kanan: × ÷ dulu, baru + −
        for (int level = 2; level >= 1; level--) {
            int i = 0;
            while (i < o.size()) {
                if (prioritas(o.get(i)) == level) gabung(a, o, i);
                else i++;
            }
        }
        return a.get(0);
    }

    //Hitung operasi ke-i, lalu ganti kedua operannya dengan hasilnya.
    private void gabung(List<Double> a, List<String> o, int i) {
        double b = a.get(i + 1);
        double hasil = hitung(a.get(i), o.get(i), b);
        if (Double.isNaN(hasil) || Double.isInfinite(hasil)) {
            throw new ArithmeticException(pesanError(o.get(i), b));
        }
        a.set(i, hasil);
        a.remove(i + 1);
        o.remove(i);
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
        title.setForeground(COLOR_TEXT_OP);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(COLOR_DISPLAY);
        textArea.setForeground(COLOR_TEXT);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
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
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_DISPLAY);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    //Mengembalikan NaN/Infinity bila operasi tidak valid
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

    //Satu kategori konversi: nama unit + faktor ke unit dasar (suhu ditangani khusus)
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
        JComboBox<T> combo = new RoundedComboBox<>(items);
        combo.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        combo.setBackground(COLOR_BTN_NUM);
        combo.setForeground(COLOR_TEXT);
        combo.setFocusable(false);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean hasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                l.setBackground(isSelected ? COLOR_BTN_OP : COLOR_BTN_NUM);
                l.setForeground(isSelected ? COLOR_TEXT_OP : COLOR_TEXT);
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
        unitDari.setPreferredSize(new Dimension(115, 40));
        unitKe.setPreferredSize(new Dimension(115, 40));

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

        JPanel keypadPanel = new JPanel(new GridLayout(5, 3, 10, 10));
        keypadPanel.setBackground(COLOR_BG);
        keypadPanel.setBorder(new EmptyBorder(20, 45, 25, 45));

        String[] convBtns = {
            "AC", "+/-", "⇅",
            "7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            ".", "0", "⌫"
        };

        for (String text : convBtns) {
            Color bg;
            Color fg = COLOR_TEXT;
            if (text.equals("⇅") || text.equals("AC")) {
                bg = COLOR_BTN_EQ;
            } else if (text.equals("⌫")) {
                bg = COLOR_BTN_TOP;
            } else {
                bg = COLOR_BTN_NUM;
            }
            RoundButton btn = buatTombol(text, bg, fg, 24);
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
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppKalkulator calc = new AppKalkulator();
            calc.setLocationRelativeTo(null);
            calc.setVisible(true);
        });
    }
}