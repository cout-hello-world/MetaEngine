package metaengine;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ProcessIO {
    private static final boolean DEBUG_WRITES = true;
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

    public ProcessIO(Process proc) {
        this(proc, null);
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
            String toPrint = "DEBUG: Read \"" + result + "\"";
            if (name != null) {
                toPrint += " from \"" + name + "\"";
            }
            System.err.println(toPrint);
        }
        return result;
    }

    public void sendLine(String line) {
        synchronized (writeMutex) {
            toProc.println(line);
        }
        if (DEBUG_WRITES) {
            String toPrint = "DEBUG: Sent \"" + line + "\"";
            if (name != null) {
                toPrint += " to \"" + name + "\"";
            }
            System.err.println(toPrint);
        }
    }
}
