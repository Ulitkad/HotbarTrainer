package space.ulitka;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class HotbarPanel extends JPanel implements ActionListener {
    private final Config config;
    private final Random random;

    private String state = "GAME";
    private int targetIdx;
    private int streak = 0;
    private long startTime;
    private double lastReactionTime = 0.0;
    private Color feedbackColor = null;
    private int feedbackTimer = 0;
    private Integer bindingIdx = null;
    private Point mousePos = new Point(0, 0);

    private final Rectangle settingsBtn = new Rectangle(670, 20, 110, 35);
    private final Rectangle backBtn = new Rectangle(20, 20, 100, 35);
    private final Rectangle hintsBtn = new Rectangle(325, 80, 150, 40);
    private final Rectangle[] bindRects = new Rectangle[9];

    private final Font fontBig = new Font("Arial", Font.BOLD, 48);
    private final Font fontMid = new Font("Arial", Font.BOLD, 24);
    private final Font fontSmall = new Font("Arial", Font.BOLD, 16);

    public HotbarPanel() {
        setPreferredSize(new Dimension(800, 500));
        setBackground(new Color(10, 10, 12));
        setFocusable(true);
        requestFocusInWindow();

        config = new Config();
        config.load();
        random = new Random();
        targetIdx = random.nextInt(9);
        startTime = System.currentTimeMillis();

        int gridStartX = (800 - (3 * 200)) / 2;
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            bindRects[i] = new Rectangle(gridStartX + col * 200, 150 + row * 80, 180, 60);
        }

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handleMouse(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        Timer timer = new Timer(16, this);
        timer.start();
    }

    private void handleMouse(Point p) {
        if (state.equals("GAME")) {
            if (settingsBtn.contains(p)) state = "SETTINGS";
        } else {
            if (backBtn.contains(p)) {
                state = "GAME";
                bindingIdx = null;
            } else if (hintsBtn.contains(p)) {
                config.showKeys = !config.showKeys;
                config.save();
            } else {
                for (int i = 0; i < 9; i++) {
                    if (bindRects[i].contains(p)) {
                        bindingIdx = i;
                        break;
                    }
                }
            }
        }
    }

    private void handleKey(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            if (state.equals("SETTINGS")) {
                state = "GAME";
                bindingIdx = null;
            } else {
                System.exit(0);
            }
            return;
        }

        if (state.equals("SETTINGS") && bindingIdx != null) {
            config.binds[bindingIdx] = code;
            bindingIdx = null;
            config.save();
            return;
        }

        if (state.equals("GAME")) {
            int expected = config.binds[targetIdx];
            if (code == expected) {
                long curTime = System.currentTimeMillis();
                lastReactionTime = (curTime - startTime) / 1000.0;
                streak++;
                if (streak > config.bestStreak) {
                    config.bestStreak = streak;
                    config.save();
                }
                feedbackColor = new Color(0, 255, 0);
                feedbackTimer = 7;

                int prev = targetIdx;
                while (targetIdx == prev) {
                    targetIdx = random.nextInt(9);
                }
                startTime = System.currentTimeMillis();
            } else {
                streak = 0;
                lastReactionTime = 0.0;
                feedbackColor = new Color(255, 0, 0);
                feedbackTimer = 12;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (feedbackTimer > 0) {
            feedbackTimer--;
            if (feedbackTimer == 0) feedbackColor = null;
        }
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (state.equals("GAME")) {
            drawGame(g2);
        } else {
            drawSettings(g2);
        }
    }

    private void drawButton(Graphics2D g2, Rectangle r, String text) {
        if (r.contains(mousePos)) {
            g2.setColor(new Color(70, 70, 75));
        } else {
            g2.setColor(new Color(45, 45, 50));
        }
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
        g2.setColor(new Color(100, 100, 100));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 5, 5);

        g2.setColor(new Color(230, 230, 230));
        g2.setFont(fontSmall);
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);
    }

    private void drawGame(Graphics2D g2) {
        drawButton(g2, settingsBtn, I18n.get("btn.settings"));

        String strStreak = String.valueOf(streak);
        g2.setFont(fontBig);
        FontMetrics fmBig = g2.getFontMetrics();
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(strStreak, 400 - fmBig.stringWidth(strStreak) / 2, 80);

        String strTime = lastReactionTime > 0 ? String.format(java.util.Locale.US, "%.3fs", lastReactionTime) : "--.---s";
        g2.setFont(fontMid);
        FontMetrics fmMid = g2.getFontMetrics();
        g2.setColor(new Color(0, 191, 255));
        g2.drawString(strTime, 400 - fmMid.stringWidth(strTime) / 2, 120);

        String strBest = I18n.get("label.best") + config.bestStreak;
        g2.setFont(fontSmall);
        FontMetrics fmSmall = g2.getFontMetrics();
        g2.setColor(new Color(120, 120, 120));
        g2.drawString(strBest, 400 - fmSmall.stringWidth(strBest) / 2, 160);

        int slotSize = 60;
        int slotMargin = 5;
        int hotbarPad = 10;
        int hbw = (slotSize * 9) + (slotMargin * 8) + (hotbarPad * 2);
        int startX = (800 - hbw) / 2;
        int startY = 500 - 130;

        g2.setColor(new Color(40, 40, 42));
        g2.fillRoundRect(startX, startY, hbw, slotSize + 20, 4, 4);

        for (int i = 0; i < 9; i++) {
            int x = startX + hotbarPad + i * (slotSize + slotMargin);
            int y = startY + 10;

            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, slotSize, slotSize);
            g2.setColor(new Color(20, 20, 20));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x, y, slotSize, slotSize);

            if (config.showKeys) {
                String keyName = KeyEvent.getKeyText(config.binds[i]).toUpperCase();
                g2.setFont(fontSmall);
                g2.setColor(new Color(80, 80, 85));
                int kx = x + (slotSize - fmSmall.stringWidth(keyName)) / 2;
                int ky = y + ((slotSize - fmSmall.getHeight()) / 2) + fmSmall.getAscent();
                g2.drawString(keyName, kx, ky);
            }

            if (i == targetIdx) {
                Color c = feedbackColor != null ? feedbackColor : new Color(255, 255, 255);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(4));
                g2.drawRect(x - 4, y - 4, slotSize + 8, slotSize + 8);
            }
        }
    }

    private void drawSettings(Graphics2D g2) {
        drawButton(g2, backBtn, I18n.get("btn.back"));

        g2.setFont(fontMid);
        g2.setColor(Color.WHITE);
        FontMetrics fmMid = g2.getFontMetrics();
        String title = I18n.get("title.settings");
        g2.drawString(title, 400 - fmMid.stringWidth(title) / 2, 45);

        String status = config.showKeys ? I18n.get("stat.on") : I18n.get("stat.off");
        drawButton(g2, hintsBtn, I18n.get("btn.hints") + status);

        for (int i = 0; i < 9; i++) {
            Rectangle r = bindRects[i];
            Color borderCol = (bindingIdx != null && bindingIdx == i) ? new Color(100, 100, 255) : new Color(100, 100, 100);

            g2.setColor(new Color(40, 40, 45));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 5, 5);

            g2.setFont(fontSmall);
            g2.setColor(new Color(150, 150, 150));
            g2.drawString(I18n.get("label.slot") + (i + 1) + ":", r.x + 10, r.y + 25);

            String val = (bindingIdx != null && bindingIdx == i) ? I18n.get("label.press_key") : KeyEvent.getKeyText(config.binds[i]).toUpperCase();
            g2.setFont(fontMid);
            g2.setColor(Color.WHITE);
            g2.drawString(val, r.x + 10, r.y + 50);
        }
    }
}