package metaengine;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ProcessIO {
    public static final AtomicBit debug = new AtomicBit();
    private static final boolean DEBUG_WRITES = false;
    private static final boolean DEBUG_READS = false;
    private final Object readMutex = new Object();
    private final BufferedReader fromProc;
    private final Object writeMutex = new Object();
    private final PrintWriter toProc;
    private final String name;

    public ProcessIO(Process proc, String name) {
        fromProc = new BufferedReader(new InputStreamReader(
                       proc.getInputStream()));
        toProc =
            new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(proc.getOutputStream())), true);
        this.name = name;
    }

    public String readLine() {
        String result;
        try {
            synchronized (readMutex) {
                result = fromProc.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpted error reading from process");
        }
        if (DEBUG_READS) {
            String toPrint = "<" + name + ": " + result;
            System.err.println(toPrint);
        }
        if (debug.get()) {
            String toPrint = "<" + name + ": " + result;
            System.out.println("info string " + toPrint);
        }
        return result;
    }

    public void sendLine(String line) {
        synchronized (writeMutex) {
            toProc.println(line);
        }
        String toPrint = ">" + name + ": " + line;
        if (DEBUG_WRITES) {
            System.err.println(toPrint);
        }
        if (debug.get()) {
            System.out.println("info string " + toPrint);
        }
    }
}
