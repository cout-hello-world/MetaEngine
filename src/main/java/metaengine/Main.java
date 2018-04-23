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
      // TODO: Real exception handling
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

        Configuration configuration =
          Configuration.newConfigurationFromDocument(doc);
        UCIEnginesManager engines = UCIEnginesManager.create(configuration);

        System.exit(new UCI(engines).loop());
    }

    private static void printHelp(PrintStream out) {
        out.println("Usage: java -jar <MetaEngine_jar> <xml_config_file>");
    }
}
