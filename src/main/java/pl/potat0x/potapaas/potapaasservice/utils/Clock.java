package pl.potat0x.potapaas.potapaasservice.utils;

public class Clock {
    private long start;

    public Clock() {
        restart();
    }

    public void restart() {
        start = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - start;
    }
}
