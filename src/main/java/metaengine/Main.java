package metaengine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {
    public static void main(String[] args)
      throws ParserConfigurationException, IOException,
             InvalidConfigurationException {
        if (args.length != 1) {
            printHelp(System.err);
            System.exit(1);
            return;
        }
        switch (args[0]) {
        case "-h":
        case "--help":
            printHelp(System.out);
            return;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File config = new File(args[0]);
        Document doc;
        try {
            doc = db.parse(new File(args[0]));
        } catch (IOException e){
            System.err.println("Error reading from config file: " + config);
            System.exit(1);
            return;
        } catch (SAXException e) {
            System.err.println("Error parsing config file: " + e.getMessage());
            System.exit(1);
            return;
        }

        List<List<String>> listOfArgLists =
          Configuration.newConfigurationFromDocument(doc)
            .getUnmodifiableEngineArguments();
        /*
        for (List<String> engineArgs : arguments) {
            for (String arg : engineArgs) {
                System.out.println(arg);
            }
            System.out.println();
        }*/

        List<UCIEngine> engines = new ArrayList<UCIEngine>();
        for (List<String> engineArgs : listOfArgLists) {
            engines.add(new UCIEngine(engineArgs));
        }

        for (UCIEngine engine : engines) {
            System.out.println(engine.getInvokedName());
            List<UCIOption> options = engine.getOptions();
            for (UCIOption opt : options) {
                System.out.println(opt.getOptionString());
                System.out.println(opt.getSetoptionString());
            }
            engine.quit();
        }
    }

    private static void printHelp(PrintStream out) {
        out.println("Usage: java -jar <MetaEngine_jar> <xml_config_file>");
    }
}
