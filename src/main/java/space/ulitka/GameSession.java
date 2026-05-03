package space.ulitka;

import java.util.LinkedList;
import java.util.Random;

public class GameSession {
    private final Random random = new Random();
    public int targetIdx;
    public int streak = 0;
    public int lastStreak = 0;
    public long startTime;
    public double lastReactionTime = 0.0;
    public final LinkedList<Double> reactionHistory = new LinkedList<>();

    public GameSession() {
        resetTarget();
    }

    public void resetTarget() {
        targetIdx = random.nextInt(9);
        startTime = System.currentTimeMillis();
    }

    public void nextTarget() {
        int prev = targetIdx;
        while (targetIdx == prev) {
            targetIdx = random.nextInt(9);
        }
        startTime = System.currentTimeMillis();
    }

    public void registerSuccess(double time, int historySize) {
        lastReactionTime = time;
        reactionHistory.addLast(time);
        if (reactionHistory.size() > historySize) {
            reactionHistory.removeFirst();
        }
        streak++;
        nextTarget();
    }

    public void registerFail() {
        if (streak > 0) lastStreak = streak;
        streak = 0;
        lastReactionTime = 0.0;
        startTime = System.currentTimeMillis();
    }

    public double getAverage() {
        if (reactionHistory.isEmpty()) return 0.0;
        double sum = 0;
        for (double t : reactionHistory) sum += t;
        return sum / reactionHistory.size();
    }

    public void trimHistory(int newSize) {
        while (reactionHistory.size() > newSize) {
            reactionHistory.removeFirst();
        }
    }
}