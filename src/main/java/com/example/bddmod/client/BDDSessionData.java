package com.example.bddmod.client;

public final class BDDSessionData {
    private static final BDDSessionData INSTANCE = new BDDSessionData();
    private long recordingTicks;
    private long checks;
    private long coveredTicks;

    public static BDDSessionData get() { return INSTANCE; }
    public void tick(boolean recording, boolean covered) {
        if (recording) recordingTicks++;
        if (covered) coveredTicks++;
    }
    public void recordCheck() { checks++; }
    public long recordingTicks() { return recordingTicks; }
    public long checks() { return checks; }
    public long coveredTicks() { return coveredTicks; }
    public void reset() { recordingTicks = 0; checks = 0; coveredTicks = 0; }
    private BDDSessionData() {}
}
