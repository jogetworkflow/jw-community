package org.joget.apps.form.dao;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class XMLUtil {

    public static void removeChildren(Node node) {
        NodeList childNodes = node.getChildNodes();
        int length = childNodes.getLength();
        for (int i = length - 1; i > -1; i--) {
            node.removeChild(childNodes.item(i));
        }
    }

    public static Document loadDocument(String file)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    public static Document loadDocument(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //skip DTD validation
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        });

        return builder.parse(is);
    }

    public static void saveDocument(Document dom, String file)
            throws TransformerException, IOException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dom.getDoctype().getPublicId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dom.getDoctype().getSystemId());

        DOMSource source = new DOMSource(dom);
        StreamResult result = new StreamResult();

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            result.setOutputStream(outputStream);
            transformer.transform(source, result);
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
