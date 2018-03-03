package org.tdar.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utility class for creating x-www-form-urlencoded body data.
 */
public class SimplePostRequestEncoder {
    private String encoding;
    private int count = 0;
    private StringBuilder data = new StringBuilder();

    /**
     * Create a new encoder with the character encoding  (e.g. UTF-8).
     * @param encoding
     * @throws UnsupportedEncodingException
     */
    public SimplePostRequestEncoder(String encoding) throws UnsupportedEncodingException {
        //create throwaway string to ensure encoding is valid
        URLEncoder.encode("test", encoding);
        this.encoding = encoding;
    }

    /**
     * Create a new encoder with default character encoding.
     * @throws UnsupportedEncodingException
     */
    public SimplePostRequestEncoder() throws UnsupportedEncodingException {
        this("UTF-8");
    }

    /**
     * Add a name/value to the message body.
     * @param name
     * @param data
     * @return the encoder object, for method chaining.
     */
    public SimplePostRequestEncoder put(String name, String data) {
        try {
            if(count++ > 0) {
                data.concat("&");
            }
            data.concat(name + "=" + URLEncoder.encode(data, encoding));
        } catch (UnsupportedEncodingException ignored) {}
        return this;
    }

    /**
     * return a string containing the message body in x-www-form-urlencoded format.
     * @return
     */
    public String toString() {
        return data.toString();
    }
}
