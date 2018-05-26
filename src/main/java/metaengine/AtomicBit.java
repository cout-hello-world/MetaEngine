package metaengine;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicBit {
    private final AtomicInteger data;
    public AtomicBit() {
        this(false);
    }

    public AtomicBit(boolean val) {
        if (val) {
            data = new AtomicInteger(1);
        } else {
            data = new AtomicInteger(0);
        }
    }

    public void toggle() {
        data.getAndIncrement();
    }

    public boolean get() {
        int val = data.get();
        if (val % 2 != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void set(boolean val) {
        if (val) {
            set();
        } else {
            clear();
        }
    }

    public void set() {
        data.set(1);
    }

    public void clear() {
        data.set(0);
    }
}
