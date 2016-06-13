/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.oai.bean;

/**
 * @author Adam Brin
 * 
 */
public enum OAIVerb {
    IDENTIFY("Identify"),
    LIST_METADATA_FORMATS("ListMetadataFormats"),
    LIST_IDENTIFIERS("ListIdentifiers"),
    LIST_SETS("ListSets"),
    LIST_RECORDS("ListRecords"),
    GET_RECORD("GetRecord");

    private String verb;

    private OAIVerb(String verb) {
        this.setVerb(verb);
    }

    public static OAIVerb fromString(String val) {
        for (OAIVerb verb_ : OAIVerb.values()) {
            if (verb_.getVerb().equals(val)) {
                return verb_;
            }
        }
        return null;
    }

    /**
     * @return the verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * @param verb
     *            the verb to set
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

}
