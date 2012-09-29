package org.tdar.core.bean.resource;



/**
 * $Id$
 * 
 * Controlled vocabulary for document types.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum DocumentType { 

    BOOK("Book / Report"), 
    BOOK_SECTION("Book Chapter / Section"),
    JOURNAL_ARTICLE("Journal Article"), 
    THESIS("Thesis / Dissertation"),
    CONFERENCE_PRESENTATION("Conference Presentation"),
    OTHER("Other");

    private final String label;
    
    private DocumentType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns the ResourceType corresponding to the String given or null if none exists.  Used in place of valueOf since
     * valueOf throws RuntimeExceptions.
     */
    public static DocumentType fromString(String string) {
        if (string == null || "".equals(string)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to ResourceType enum.. unfortunately valueOf only throws RuntimeExceptions.
        try {
            return DocumentType.valueOf(string);
        }
        catch (Exception exception) {
            return null;
        }
    }
    
    /**
     * Returns the name of the URL fragment and template basename minus the file extension (e.g., instead of book.ftl it returns book).  
     * 
     * The convention used is to lowercase the enum name and replace all underscores with dashes, e.g., BOOK_SECTION to book-section and JOURNAL_ARTICLE to journal-article.
     * 
     * @return
     */
    public String toUrlFragment() {
        return name().toLowerCase().replace('_', '-');
    }
   
//    public static List<String> getDocumentTypes() {
//    	  List<String> types = new ArrayList<String>();
//
//    	  for (DocumentType docType : DocumentType.values()) {
//    	    types.add();  
//    	  }
//
//    	  return colours;
//    	}
}
