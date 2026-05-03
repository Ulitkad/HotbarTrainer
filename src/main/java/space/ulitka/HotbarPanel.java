package space.ulitka;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HotbarPanel extends JPanel implements ActionListener {
    private final Config config;
    private final GameSession session;

    private String state = "GAME";
    private Color feedbackColor = null;
    private int feedbackTimer = 0;
    private Integer bindingIdx = null;
    private Point mousePos = new Point(0, 0);

    private final Rectangle settingsBtn = new Rectangle(670, 20, 110, 35);
    private final Rectangle backBtn = new Rectangle(20, 20, 100, 35);
    private final Rectangle hintsBtn = new Rectangle(180, 80, 200, 40);
    private final Rectangle historyBtn = new Rectangle(420, 80, 200, 40);
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
        session = new GameSession();

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

        new Timer(16, this).start();
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
            } else if (historyBtn.contains(p)) {
                int[] sizes = {5, 10, 25, 50, 100};
                int next = sizes[0];
                for (int i = 0; i < sizes.length; i++) {
                    if (config.historySize == sizes[i]) {
                        next = sizes[(i + 1) % sizes.length];
                        break;
                    }
                }
                config.historySize = next;
                config.save();
                session.trimHistory(config.historySize);
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
            if (code == config.binds[session.targetIdx]) {
                double reaction = (System.currentTimeMillis() - session.startTime) / 1000.0;
                session.registerSuccess(reaction, config.historySize);

                if (session.streak > config.bestStreak) {
                    config.bestStreak = session.streak;
                    config.save();
                }

                feedbackColor = new Color(0, 255, 0);
                feedbackTimer = 7;
            } else {
                session.registerFail();
                feedbackColor = new Color(255, 0, 0);
                feedbackTimer = 12;
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
        g2.setColor(r.contains(mousePos) ? new Color(70, 70, 75) : new Color(45, 45, 50));
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

        String strStreak = String.valueOf(session.streak);
        g2.setFont(fontBig);
        FontMetrics fmBig = g2.getFontMetrics();
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(strStreak, 400 - fmBig.stringWidth(strStreak) / 2, 70);

        String strTime = session.lastReactionTime > 0 ? String.format(java.util.Locale.US, "%.3fs", session.lastReactionTime) : "--.---s";
        g2.setFont(fontMid);
        FontMetrics fmMid = g2.getFontMetrics();
        g2.setColor(new Color(0, 191, 255));
        g2.drawString(strTime, 400 - fmMid.stringWidth(strTime) / 2, 110);

        g2.setFont(fontSmall);
        FontMetrics fmSmall = g2.getFontMetrics();
        g2.setColor(new Color(150, 150, 150));

        g2.drawString(I18n.get("label.best") + ": " + config.bestStreak, 30, 45);
        g2.drawString(I18n.get("label.last_streak") + ": " + session.lastStreak, 30, 70);

        String strAvgVal = session.reactionHistory.isEmpty() ? "--.---s" : String.format(java.util.Locale.US, "%.3fs", session.getAverage());
        String strAvg = I18n.get("label.avg") + ": (" + session.reactionHistory.size() + "/" + config.historySize + "): " + strAvgVal;
        g2.drawString(strAvg, 30, 95);

        int startX = (800 - 620) / 2;
        int startY = 370;

        g2.setColor(new Color(40, 40, 42));
        g2.fillRoundRect(startX, startY, 620, 80, 4, 4);

        for (int i = 0; i < 9; i++) {
            int x = startX + 10 + i * 65;
            int y = startY + 10;

            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, 60, 60);
            g2.setColor(new Color(20, 20, 20));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x, y, 60, 60);

            if (config.showKeys) {
                String keyName = KeyEvent.getKeyText(config.binds[i]).toUpperCase();
                g2.setFont(fontSmall);
                g2.setColor(new Color(80, 80, 85));
                g2.drawString(keyName, x + (60 - fmSmall.stringWidth(keyName)) / 2, y + 35);
            }

            if (i == session.targetIdx) {
                g2.setColor(feedbackColor != null ? feedbackColor : Color.WHITE);
                g2.setStroke(new BasicStroke(4));
                g2.drawRect(x - 4, y - 4, 68, 68);
            }
        }
    }

    private void drawSettings(Graphics2D g2) {
        drawButton(g2, backBtn, I18n.get("btn.back"));

        g2.setFont(fontMid);
        g2.setColor(Color.WHITE);
        String title = I18n.get("title.settings");
        g2.drawString(title, 400 - g2.getFontMetrics().stringWidth(title) / 2, 45);

        drawButton(g2, hintsBtn, I18n.get("btn.hints") + ": " + (config.showKeys ? I18n.get("stat.on") : I18n.get("stat.off")));
        drawButton(g2, historyBtn, I18n.get("btn.history") + ": " + config.historySize);

        for (int i = 0; i < 9; i++) {
            Rectangle r = bindRects[i];
            g2.setColor(new Color(40, 40, 45));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
            g2.setColor((bindingIdx != null && bindingIdx == i) ? new Color(100, 100, 255) : new Color(100, 100, 100));
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