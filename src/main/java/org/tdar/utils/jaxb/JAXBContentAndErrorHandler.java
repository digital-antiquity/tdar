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

    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        contentHandler.characters(arg0, arg1, arg2);
    }

    @Override
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        qname = arg2;
        namespaceURI = arg0;
        contentHandler.endElement(arg0, arg1, arg2);
    }

    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
        contentHandler.endPrefixMapping(arg0);
    }

    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        contentHandler.ignorableWhitespace(arg0, arg1, arg2);
    }

    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        contentHandler.processingInstruction(arg0, arg1);
    }

    @Override
    public void setDocumentLocator(Locator arg0) {
        contentHandler.setDocumentLocator(arg0);
    }

    @Override
    public void skippedEntity(String arg0) throws SAXException {
        contentHandler.skippedEntity(arg0);
    }

    @Override
    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }

    @Override
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        qname = arg2;
        namespaceURI = arg0;
        contentHandler.startElement(arg0, arg1, arg2, arg3);
    }

    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        contentHandler.startPrefixMapping(arg0, arg1);
    }

    @Override
    public void error(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

    @Override
    public void fatalError(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

    @Override
    public void warning(SAXParseException arg0) throws SAXException {
        System.out.println("{" + namespaceURI + "}" + qname);
        arg0.printStackTrace(System.out);
    }

}