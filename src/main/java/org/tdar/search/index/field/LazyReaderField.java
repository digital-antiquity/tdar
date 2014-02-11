package org.tdar.search.index.field;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
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
public class LazyReaderField extends AbstractField implements Fieldable {

    private static final long serialVersionUID = 2428565315964872093L;
    private Reader reader;
    private final static transient Logger logger = LoggerFactory.getLogger(LazyReaderField.class);
    private List<URI> paths;

    public LazyReaderField(String name, List<URI> paths, Field.Store store, Field.Index index, Float boost) {
        super(name, store, index, Field.TermVector.NO);
        // fundamental set: this instructs Lucene not to call the stringValue on field creation, but only when needed
        super.lazy = true;
        if (boost != null)
            setBoost(boost);
        this.paths = paths;
    }

    public byte[] binaryValue() {
        return null;
    }

    @Override
    public Reader readerValue() {
        logger.trace("getting reader for: {}", name);

        List<InputStream> streams = new ArrayList<InputStream>();
        for (URI uri : paths) {
            try {
                streams.add(uri.toURL().openStream());
            } catch (Exception e) {
                logger.debug("cannot read url:", e);
            }
        }
        SequenceInputStream stream = new SequenceInputStream(Collections.enumeration(streams));
        logger.trace("returning stream reader {}", streams);

        reader = new BufferedReader(new InputStreamReader(stream));
        return reader;
    }

    @Override
    public TokenStream tokenStreamValue() {
        return null;
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
}
