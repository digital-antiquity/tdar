package org.tdar.utils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ApiClientResponse implements Serializable {

    private static final long serialVersionUID = -5890656648322899809L;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Header contentType;
    private String body;
    private Header[] headers;
    private StatusLine statusLine;
    private int statusCode;

    public ApiClientResponse(CloseableHttpResponse response) {
        this.statusCode = response.getStatusLine().getStatusCode();
        this.statusLine = response.getStatusLine();
        this.headers = response.getAllHeaders();
        logger.trace("response: {}", response);
        try {
            this.body = IOUtils.toString(response.getEntity().getContent());
        } catch (UnsupportedOperationException | IOException e) {
            logger.error("error in response", e);
        }
        logger.trace("done reading body");
        this.contentType = response.getEntity().getContentType();
    }

    public Header getContentType() {
        return contentType;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private Long id = null;

    public Long getTdarId() {
        if (id != null) {
            return id;
        }
        try {
            Document document = getXmlDocument();
            String id_ = null;
            if (document.getElementsByTagName("tdar:id").getLength() > 0) {
                id_ = document.getElementsByTagName("tdar:id").item(0).getTextContent();
            }
            if (document.getElementsByTagName("tdar:recordId").getLength() > 0) {
                id_ = document.getElementsByTagName("tdar:recordId").item(0).getTextContent();
            }
            logger.debug("ID:: {}", id_);
            if (id_ != null) {
                id = Long.parseLong(id_);
                return id;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("cannot find tDAR Id", e);
        }
        return null;
    }

    public Document getXmlDocument() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(getBody())));
        return document;
    }

}
