package org.tdar.core.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * $Id$
 * 
 * RuntimeException for any coding sheet parse exceptions (should eventually contain error conditions to
 * distinguish between malformed coding sheets and IO Exceptions).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CodingSheetParserException extends Exception {

    private static final long serialVersionUID = 6246686753761896569L;
    // TODO: considering immutableList, but remember we have pom exclusions for the latest version (i think)
    private List<String> contributingFactors = new ArrayList<String>();

    public CodingSheetParserException() {
        super();
    }

    public CodingSheetParserException(String message) {
        super(message);
    }

    public CodingSheetParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodingSheetParserException(Throwable cause) {
        super(cause);
    }

    public CodingSheetParserException(String message, Collection<String> contributingFactors) {
        // FIXME: don't concat contributing factors. rely on view logic for that.
        super(message + contributingFactors);
        this.contributingFactors.addAll(contributingFactors);
    }

    public List<String> getContributingFactors() {
        return contributingFactors;
    }

}
