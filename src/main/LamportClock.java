package main;

public class LamportClock {
    private int time;

    public LamportClock() {
        this.time = 0; // Initialize clock to 0
    }

    // Increment clock on every event (local event or sending message)
    public synchronized void increment() {
        time++;
    }

    // Update clock based on received timestamp
    public synchronized void update(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
    }

    // Get current clock time
    public synchronized int getTime() {
        return time;
    }
}
