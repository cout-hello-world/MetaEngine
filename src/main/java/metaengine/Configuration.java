package metaengine;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;

public class Configuration {
    public static Configuration newConfigurationFromDocument(Document doc)
      throws InvalidConfigurationException {
        Element root = doc.getDocumentElement();
        if (!root.getTagName().equals("config")) {
            throw new InvalidConfigurationException("Root element not \"config\"");
        }

        NodeList enginesList = root.getElementsByTagName("engines");
        if (enginesList.getLength() != 1) {
            throw new InvalidConfigurationException(
              "\"config\" element must contain exactly one \"engines\" element");
        }

        Element engines = (Element)enginesList.item(0);
        NodeList engineList = engines.getElementsByTagName("engine");

        List<List<String>> returnList = new ArrayList<List<String>>();
        for (int i = 0; i < engineList.getLength(); ++i) {
            Element engineElement = (Element)engineList.item(i);
            Attr exeAttr = engineElement.getAttributeNode("exe");
            if (exeAttr == null) {
                throw new InvalidConfigurationException(
                  "\"engine\" element missing required attribute \"exe\"");
            }
            String exe = exeAttr.getValue();
            int copies = 1;
            Attr copiesAttr = engineElement.getAttributeNode("copies");
            if (copiesAttr != null) {
                try {
                    copies = Integer.parseInt(copiesAttr.getValue());
                } catch (NumberFormatException e) {
                    throw
                      new InvalidConfigurationException("Could not pasre \"copies\" attribute as an integer.");
                }
            }
            List<String> engineArgs = new ArrayList<String>();
            engineArgs.add(exe);
            NodeList argsList = engineElement.getElementsByTagName("args");

            int numArgs = argsList.getLength();
            switch (numArgs) {
            case 0:
                break;
            case 1:
            {
                Element argsElement = (Element)argsList.item(0);
                NodeList argList = argsElement.getElementsByTagName("arg");
                for (int j = 0; j < argList.getLength(); ++j) {
                    engineArgs.add(argList.item(j).getTextContent().trim());
                }
                break;
            }
            default:
                throw new InvalidConfigurationException(
                  "\"engine\" element must contain 0 or 1 \"args\" elements");
            }

            for (int j = 0; j < copies; ++j) {
                returnList.add(Collections.unmodifiableList(engineArgs));
            }
        }

        return new Configuration(Collections.unmodifiableList(returnList));
    }

    private final List<List<String>> engineArguments;

    private Configuration(List<List<String>> unmodifiable) {
        engineArguments = unmodifiable;
    }

    public List<List<String>> getUnmodifiableEngineArguments() {
        return engineArguments;
    }
}
