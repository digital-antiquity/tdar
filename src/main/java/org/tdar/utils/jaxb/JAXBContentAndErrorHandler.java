package org.tdar.utils.jaxb;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class JAXBContentAndErrorHandler implements ContentHandler, ErrorHandler {

    private ContentHandler contentHandler;
    private String qname;
    private String namespaceURI;

    public JAXBContentAndErrorHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        contentHandler.characters(arg0, arg1, arg2);
    }

    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        qname = arg2;
        namespaceURI = arg0;
        contentHandler.endElement(arg0, arg1, arg2);
    }

    public void endPrefixMapping(String arg0) throws SAXException {
        contentHandler.endPrefixMapping(arg0);
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        contentHandler.ignorableWhitespace(arg0, arg1, arg2);
    }

    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        contentHandler.processingInstruction(arg0, arg1);
    }

    public void setDocumentLocator(Locator arg0) {
        contentHandler.setDocumentLocator(arg0);
    }

    public void skippedEntity(String arg0) throws SAXException {
        contentHandler.skippedEntity(arg0);
    }

    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }

    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        qname = arg2;
        namespaceURI = arg0;
        contentHandler.startElement(arg0, arg1, arg2, arg3);
    }

    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        contentHandler.startPrefixMapping(arg0, arg1);
    }

    public void error(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

    public void fatalError(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

    public void warning(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

}