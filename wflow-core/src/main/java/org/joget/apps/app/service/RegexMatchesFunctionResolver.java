package org.joget.apps.app.service;

import java.util.List;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import org.w3c.dom.NodeList;

public class RegexMatchesFunctionResolver implements XPathFunctionResolver {
    
    @Override
    public XPathFunction resolveFunction(javax.xml.namespace.QName functionName, int arity) {
        String localName = functionName.getLocalPart();
        if ("regexmatches".equals(localName)) {
            return new RegexMatchesFunction();
        } else {
            return null;
        }
    }

    class RegexMatchesFunction implements XPathFunction {

        @Override
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 2) {
                String text = ((NodeList) args.get(0)).item(0).getTextContent();
                String regex = (String) args.get(1);
                if (text != null && regex != null) {
                    return text.matches(regex);
                }
            }
            return false;
        }
    }
}
