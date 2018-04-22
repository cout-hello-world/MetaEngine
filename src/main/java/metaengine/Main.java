package metaengine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {

    private static ThreadPool mainPool = new ThreadPool();

    public static void main(String[] args)
      // TODO: Real exception handling
      throws ParserConfigurationException, IOException,
             InvalidConfigurationException, InterruptedException,
             ExecutionException {
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

        List<Future<UCIEngine>> futureEngines =
          new ArrayList<Future<UCIEngine>>();
        for (List<String> argsList : listOfArgLists) {
            Callable<UCIEngine> constructEngine = () -> {
                return new UCIEngine(argsList);
            };
            mainPool.submit(constructEngine);
        }

        List<UCIEngine> engines = new ArrayList<UCIEngine>();
        for (Future<UCIEngine> future : futureEngines) {
            engines.add(future.get());
        }

        for (UCIEngine engine : engines) {
            System.out.println("Name: " + engine.getInvokedName());
            System.out.println("uid: " + engine.getUniqueId());
            for (UCIOption opt : engine.getOptions()) {
                System.out.println(opt.getOptionString());
                System.out.println(opt.getSetoptionString());
            }
            System.out.println();
            engine.quit();
        }
    }

    private static void printHelp(PrintStream out) {
        out.println("Usage: java -jar <MetaEngine_jar> <xml_config_file>");
    }
}
