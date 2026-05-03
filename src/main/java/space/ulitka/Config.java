package space.ulitka;

import java.io.*;

public class Config {
    public int[] binds = {49, 50, 51, 52, 53, 82, 84, 71, 86};
    public int bestStreak = 0;
    public boolean showKeys = true;

    public void load() {
        File file = new File("config.cfg");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("bestStreak=")) {
                    bestStreak = Integer.parseInt(line.split("=")[1]);
                } else if (line.startsWith("showKeys=")) {
                    showKeys = Boolean.parseBoolean(line.split("=")[1]);
                } else if (line.startsWith("binds=")) {
                    String[] parts = line.split("=")[1].split(",");
                    for (int i = 0; i < 9 && i < parts.length; i++) {
                        binds[i] = Integer.parseInt(parts[i]);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("config.cfg"))) {
            pw.println("bestStreak=" + bestStreak);
            pw.println("showKeys=" + showKeys);
            StringBuilder sb = new StringBuilder("binds=");
            for (int i = 0; i < binds.length; i++) {
                sb.append(binds[i]).append(i == binds.length - 1 ? "" : ",");
            }
            pw.println(sb);
        } catch (Exception ignored) {}
    }
}