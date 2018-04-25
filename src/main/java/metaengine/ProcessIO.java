package metaengine;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ProcessIO {
    private final BufferedReader fromProc;
    private final PrintWriter toProc;
    private final boolean debug;
    private final String name;

    public ProcessIO(Process proc, String name, boolean debug) {
        fromProc = new BufferedReader(new InputStreamReader(
                       proc.getInputStream()));
        toProc =
            new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(proc.getOutputStream())), true);
        this.debug = debug;
        this.name = name;
    }

    public ProcessIO(Process proc, String name) {
        this(proc, name, false);
    }

    public ProcessIO(Process proc) {
        this(proc, null);
    }

    public String readLine() {
        String result;
        try {
            result = fromProc.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Unexpted error reading from process");
        }
        if (debug) {
            String toPrint = "Read \"" + result + "\"";
            if (name != null) {
                toPrint += " from \"" + name + "\"";
            }
            System.err.println(toPrint);
        }
        return result;
    }

    public void sendLine(String line) {
        toProc.println(line);
        if (debug) {
            String toPrint = "Sent \"" + line + "\"";
            if (name != null) {
                toPrint += " to \"" + name + "\"";
            }
            System.err.println(toPrint);
        }
    }
}
