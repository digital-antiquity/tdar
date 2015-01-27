package org.tdar.search.index.field;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted from
 * Roberto Bicchierai rbicchierai@open-lab.com
 * Pietro Polsinelli ppolsinelli@open-lab.com
 * for the Teamwork Project Management application - http://www.twproject.com
 * Open Lab - Florence - Italy
 * Released under LGPL - use it as you want
 * 
 * This is a lazy and minimal implementation for Lucene Fieldable
 */
public class LazyReaderField extends Field implements IndexableField {

    private static final long serialVersionUID = 2428565315964872093L;
    private Reader reader;
    private final static transient Logger logger = LoggerFactory.getLogger(LazyReaderField.class);
    private List<URI> paths;
    private String name;

    public LazyReaderField(String name, List<URI> paths, Reader reader, Float boost) {
        super(name, reader, TextField.TYPE_STORED);
        this.paths = paths;
        this.name= name;
        // fundamental set: this instructs Lucene not to call the stringValue on field creation, but only when needed
//        super.lazy = true;
        if (boost != null) {
//            setBoost(boost);
        }
    }



    @Override
    public String stringValue() {
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (reader != null) {
            logger.trace("closing reader");
            IOUtils.closeQuietly(reader);
        }
        super.finalize();
    }


    @Override
    public String name() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public float boost() {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public BytesRef binaryValue() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Number numericValue() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
