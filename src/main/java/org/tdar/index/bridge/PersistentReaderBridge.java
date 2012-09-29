/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.index.bridge;

import java.net.URI;
import java.util.List;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.tdar.index.field.LazyReaderField;

/**
 * @author Adam Brin
 * 
 */
public class PersistentReaderBridge implements FieldBridge {

    List<URI> input;

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document,
     * org.hibernate.search.bridge.LuceneOptions)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        // TODO Auto-generated method stub
        if (value != null) {
            input = (List<URI>) value;
            LazyReaderField field = new LazyReaderField(name, input, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getBoost());
            document.add(field);
        }

    }
}
