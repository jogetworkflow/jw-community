/**
 * Miroslav Popov, Apr 6, 2006 miroslav.popov@gmail.com
 */
package org.enhydra.shark.xpdl;

import java.util.Iterator;
import java.util.List;

import org.enhydra.shark.xpdl.elements.Condition;
import org.enhydra.shark.xpdl.elements.Deadline;
import org.enhydra.shark.xpdl.elements.DeadlineLimit;
import org.enhydra.shark.xpdl.elements.Deadlines;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.ImplementationTypes;
import org.enhydra.shark.xpdl.elements.Namespace;
import org.enhydra.shark.xpdl.elements.Namespaces;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.SchemaType;
import org.enhydra.shark.xpdl.elements.Tools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Miroslav Popov
 */
public class XPDLRepositoryHandler {

    protected static boolean logging = false;
    protected String xpdlPrefix = "";

    public void setXPDLPrefixEnabled(boolean enable) {
        if (enable) {
            this.xpdlPrefix = "xpdl:";
        } else {
            this.xpdlPrefix = "";
        }
    }

    public boolean isXPDLPrefixEnabled() {
        return "xpdl:".equals(this.xpdlPrefix);
    }

    public void fromXML(Element node, Package pkg) {
        NamedNodeMap attribs = node.getAttributes();
        Namespaces nss = pkg.getNamespaces();
        for (int i = 0; i < attribs.getLength(); i++) {
            Node n = attribs.item(i);
            String nn = n.getNodeName();
            if (nn.startsWith("xmlns:") && !nn.equals("xmlns:xsi")) {
                Namespace ns = (Namespace) nss.generateNewElement();
                ns.setName(nn.substring(6, nn.length()));
                fromXML(n, (XMLAttribute) ns.get("location"));
                nss.add(ns);
            }
        }
        fromXML(node, (XMLComplexElement) pkg);
    }

    public void fromXML(Node node, XMLCollection cel) {
        if (node == null || !node.hasChildNodes()) {
            return;
        }
        String nameSpacePrefix = XMLUtil.getNameSpacePrefix(node);

        XMLElement newOne = cel.generateNewElement();
        String elName = newOne.toName();

        NodeList children = node.getChildNodes();
        int lng = children.getLength();
        if (logging) {
            System.out.println("FROMXML for " + cel.toName() + ", c=" + cel.getClass().getName());
        }
        for (int i = 0; i < lng; i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(nameSpacePrefix + elName)) {
                newOne = cel.generateNewElement();

                if (newOne instanceof XMLComplexElement) {
                    fromXML(children.item(i), (XMLComplexElement) newOne);
                } else {
                    fromXML(children.item(i), (XMLSimpleElement) newOne);
                }
                cel.add(newOne);

                // CUSTOM
                // remove additional columns for Deadlines
                if (newOne instanceof Deadline) {
                    ((Deadline)newOne).hideCustomElements();
                }
                // END CUSTOM
            }
        }
    }

    public void fromXML(Node node, XMLComplexElement cel) {
        if (node == null || (!node.hasChildNodes() && !node.hasAttributes())) {
            return;
        }

        String nameSpacePrefix = node.getPrefix();
        if (nameSpacePrefix != null) {
            nameSpacePrefix += ":";
        } else {
            nameSpacePrefix = "";
        }
        if (logging) {
            System.out.println("FROMXML for " + cel.toName() + ", c=" + cel.getClass().getName());
        }

        if (node.hasAttributes()) {
            NamedNodeMap attribs = node.getAttributes();
            for (int i = 0; i < attribs.getLength(); ++i) {
                Node attrib = attribs.item(i);
                try {
                    fromXML(attrib, (XMLAttribute) cel.get(attrib.getNodeName()));
                } catch (NullPointerException npe) {
                }
            }
        }
        // getting elements
        if (node.hasChildNodes()) {
            // Specific code for handling Condition element - we don't support Xpression
            // element
            if (cel instanceof Condition) {
                String newVal = node.getChildNodes().item(0).getNodeValue();
                if (newVal != null) {
                    cel.setValue(newVal);
                }
            }
            // Specific code for handling SchemaType element
            if (cel instanceof SchemaType) {
                NodeList nl = node.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node sn = nl.item(j);
                    if (sn instanceof Element) {
                        cel.setValue(XMLUtil.getContent(sn, true));
                        break;
                    }
                }
            }
            // Specific code for handling ExtendedAttribute element
            if (cel instanceof ExtendedAttribute) {
                cel.setValue(XMLUtil.getChildNodesContent(node));
            }
            Iterator it = cel.getXMLElements().iterator();
            while (it.hasNext()) {
                XMLElement el = (XMLElement) it.next();
                String elName = el.toName();
                if (el instanceof XMLComplexElement) {
                    Node child = XMLUtil.getChildByName(node, nameSpacePrefix + elName);
                    fromXML(child, (XMLComplexElement) el);
                    // Specific case if element is Deadlines
                } else if (el instanceof Deadlines) {
                    fromXML(node, (XMLCollection) el);
                } else if (el instanceof XMLCollection) {
                    Node child = XMLUtil.getChildByName(node, nameSpacePrefix + elName);
                    fromXML(child, (XMLCollection) el);
                } else if (el instanceof XMLComplexChoice) {
                    fromXML(node, (XMLComplexChoice) el);
                } else if (el instanceof XMLSimpleElement) {
                    Node child = XMLUtil.getChildByName(node, nameSpacePrefix + elName);
                    fromXML(child, (XMLSimpleElement) el);
                }
            }
        }
    }

    public void fromXML(Node node, XMLComplexChoice el) {
        String nameSpacePrefix = XMLUtil.getNameSpacePrefix(node);
        List ch = el.getChoices();
        if (logging) {
            System.out.println("FROMXML for " + el.toName() + ", c=" + el.getClass().getName());
        }
        for (int i = 0; i < ch.size(); i++) {
            XMLElement chc = (XMLElement) ch.get(i);
            String chname = chc.toName();
            // Specific code for handling Tools
            if (chname.equals("Tools")) {
                chname = "Tool";
            }
            Node child = XMLUtil.getChildByName(node, nameSpacePrefix + chname);
            if (child != null) {
                if (chc instanceof XMLComplexElement) {
                    fromXML(child, (XMLComplexElement) chc);
                } else { // it is XMLCollection
                    // Specific code for handling Tools
                    if (chc instanceof Tools) {
                        fromXML(node, (XMLCollection) chc);
                    } else {
                        fromXML(child, (XMLCollection) chc);
                    }
                }
                el.setChoosen(chc);
                break;
            }
        }
    }

    public void fromXML(Node node, XMLSimpleElement el) {
        fromXMLBasic(node, el);
    }

    public void fromXML(Node node, XMLAttribute el) {
        fromXMLBasic(node, el);
    }

    public void fromXMLBasic(Node node, XMLElement el) {
        if (node != null) {
            if (logging) {
                System.out.println("FROMXML for " + el.toName() + ", c=" + el.getClass().getName());
            }
            String newVal;
            if (node.hasChildNodes()) {
                newVal = node.getChildNodes().item(0).getNodeValue();
                // should never happen
            } else {
                newVal = node.getNodeValue();
            }

            // CUSTOM: catch exception for non-supported values
            if (el != null && newVal != null) {
                try {
                    el.setValue(newVal);
                }
                catch(RuntimeException re) {
                    // ignore
                    System.out.println("Error setting value for el: " + newVal + "; " + re.toString());
                    re.printStackTrace();
                }
            }
            // END CUSTOM
        }
    }

    public void toXML(Document parent, Package pkg) {
        Node node = parent.createElement(xpdlPrefix + pkg.toName());
        ((Element) node).setAttribute("xmlns", XMLUtil.XMLNS);
        // save additional namespaces
        Iterator itNs = pkg.getNamespaces().toElements().iterator();
        while (itNs.hasNext()) {
            Namespace ns = (Namespace) itNs.next();
            ((Element) node).setAttribute("xmlns:" + ns.getName(), ns.getLocation());
        }
        ((Element) node).setAttribute("xmlns:xsi", XMLUtil.XMLNS_XSI);
        ((Element) node).setAttribute("xsi:schemaLocation", XMLUtil.XSI_SCHEMA_LOCATION);

        toXML(node, pkg);
        parent.appendChild(node);
    }

    public void toXML(Node parent, XMLCollection cel) {
        if (!cel.isEmpty() || cel.isRequired()) {
            if (parent != null) {
                if (logging) {
                    System.out.println("TOXML for " + cel.toName() + ", c=" + cel.getClass().getName() + ", parent=" + cel.getParent() + ", size=" + cel.size());
                }
                String elName = cel.toName();
                Node node = parent;
                // Specific code for handling Deadlines and Tools
                if (!(elName.equals("Deadlines") || elName.equals("Tools"))) {
                    node = (parent.getOwnerDocument()).createElement(xpdlPrefix + elName);
                }

                for (Iterator it = cel.toElements().iterator(); it.hasNext();) {
                    XMLElement el = (XMLElement) it.next();

                    if (el instanceof XMLSimpleElement) {
                        toXML(node, (XMLSimpleElement) el);
                    } else {
                        toXML(node, (XMLComplexElement) el);
                    }
                }
                // If Deadlines or Tools are handled, node==parent
                if (node != parent) {
                    parent.appendChild(node);
                }
            }
        }
    }

    // CUSTOM: toXML to handle Deadlines
    public void toXML(Node parent, Deadlines cel) {
        if (cel.isEmpty() && !cel.isRequired()) {
            return;
        }
        if (parent != null) {
            if (logging) {
                System.out.println("TOXML for " + cel.toName() + ", c=" + cel.getClass().getName() + ", parent=" + cel.getParent());
            }
            Node node = parent;
            if (cel.toValue() != null && cel.toValue().length() > 0) {

            }
            for (Iterator it = cel.toElements().iterator(); it.hasNext();) {
                XMLElement el = (XMLElement) it.next();
                if (el instanceof DeadlineLimit) {
                    continue;
                }
                if (el instanceof XMLComplexElement) {
                    toXML(node, (XMLComplexElement) el);
                } else if (el instanceof XMLCollection) {
                    toXML(node, (XMLCollection) el);
                } else if (el instanceof XMLComplexChoice) {
                    toXML(node, (XMLComplexChoice) el);
                } else if (el instanceof XMLSimpleElement) {
                    toXML(node, (XMLSimpleElement) el);
                } else { // it's XMLAttribute
                    toXML(node, (XMLAttribute) el);
                }
            }
            // If Package is handled, parent==node
            if (node != parent) {
                parent.appendChild(node);
            }
        }
    }
    // END CUSTOM

    public void toXML(Node parent, XMLComplexElement cel) {
        if (cel.isEmpty() && !cel.isRequired()) {
            return;
        }
        if (parent != null) {
            if (logging) {
                System.out.println("TOXML for " + cel.toName() + ", c=" + cel.getClass().getName() + ", parent=" + cel.getParent());
            }
            Node node = parent;
            // Specific code for handling Package
            if (!(cel instanceof Package)) {
                node = (parent.getOwnerDocument()).createElement(xpdlPrefix + cel.toName());
            }
            if (cel.toValue() != null && cel.toValue().length() > 0) {
                // Specific code for handling Condition
                if (cel instanceof Condition) {
                    if (!cel.toValue().equals("")) {
                        Node textNode = node.getOwnerDocument().createTextNode(cel.toValue());
                        node.appendChild(textNode);
                    }
                }
                // Specific code for handling SchemaType
                if (cel instanceof SchemaType) {
                    Node schema = XMLUtil.parseSchemaNode(cel.toValue(), false);
                    if (schema != null) {
                        node.appendChild(node.getOwnerDocument().importNode(schema, true));
                    }
                }
                // Specific code for handling ExtendedAttribute
                if (cel instanceof ExtendedAttribute) {
                    try {
                        Node n = XMLUtil.parseExtendedAttributeContent(cel.toValue());
                        NodeList nl = n.getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++) {
                            node.appendChild(parent.getOwnerDocument().importNode(nl.item(i),
                                    true));
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            for (Iterator it = cel.toElements().iterator(); it.hasNext();) {
                XMLElement el = (XMLElement) it.next();
                if (el instanceof XMLComplexElement) {
                    toXML(node, (XMLComplexElement) el);
                } else if (el instanceof XMLCollection) {
                    toXML(node, (XMLCollection) el);
                } else if (el instanceof XMLComplexChoice) {
                    toXML(node, (XMLComplexChoice) el);
                } else if (el instanceof XMLSimpleElement) {
                    toXML(node, (XMLSimpleElement) el);
                } else { // it's XMLAttribute
                    toXML(node, (XMLAttribute) el);
                }
            }
            // If Package is handled, parent==node
            if (node != parent) {
                parent.appendChild(node);
            }
        }
    }

    public void toXML(Node parent, XMLComplexChoice el) {
        XMLElement choosen = el.getChoosen();
        if (logging) {
            System.out.println("TOXML for " + el.toName() + ", c=" + el.getClass().getName() + ", parent=" + el.getParent());
        }
        if (choosen != null) {
            if (choosen instanceof XMLComplexElement) {
                toXML(parent, (XMLComplexElement) choosen);
            } else {
                if (choosen.toName().equals("Tools") && ((Tools) choosen).size() == 0) {
                    toXML(parent, ((ImplementationTypes) el).getNo());
                } else {
                    toXML(parent, (XMLCollection) choosen);
                }
            }
        }
    }

    public void toXML(Node parent, XMLSimpleElement el) {
        if (!el.isEmpty() || el.isRequired()) {
            if (parent != null) {
                if (logging) {
                    System.out.println("TOXML for " + el.toName() + ", c=" + el.getClass().getName() + ", parent=" + el.getParent() + ", val=" + el.toValue());
                }
                Node node = (parent.getOwnerDocument()).createElement(xpdlPrefix + el.toName());
                node.appendChild(parent.getOwnerDocument().createTextNode(el.toValue().trim()));
                parent.appendChild(node);
            }
        }
    }

    public void toXML(Node parent, XMLAttribute el) {
        if (!el.isEmpty() || el.isRequired()) {
            if (parent != null) {
                if (logging) {
                    System.out.println("TOXML for " + el.toName() + ", c=" + el.getClass().getName() + ", parent=" + el.getParent() + ", val=" + el.toValue());
                }
                Attr node = (parent.getOwnerDocument()).createAttribute(el.toName());
                node.setValue(el.toValue().trim());
                ((Element) parent).setAttributeNode(node);
            }
        }
    }
}
