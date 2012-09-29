package org.tdar.web;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

/**
 * testing searches that are more involved than the basic browsing use cases.
 * 
 * @author jimdevos
 * 
 */
public class SearchITCase extends AbstractAdminAuthenticatedWebTestCase {

    @Test
    public void testBasicSearchAll() {
        // FIXME: see thisTestIsBroken()
        gotoPage("/search/basic");
        selectAllResourceTypes();
        submitForm("Search");
    }

    private void selectAllResourceTypes() {
        ArrayList<HtmlCheckBoxInput> checkboxes = new ArrayList<HtmlCheckBoxInput>();
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Document"));
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Dataset"));
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Coding_Sheet"));
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Image"));
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Ontology"));
        checkboxes.add((HtmlCheckBoxInput) getHtmlPage().getElementById("resourceTypes_Project"));
        for (HtmlCheckBoxInput cb : checkboxes) {
            cb.setChecked(false);
            logger.trace("checkbox: " + cb);
        }

    }

    // FIXME: I get an exception when I try to modify the contents of the checkbox collection... why? I know the collection itself is immutable, but
    // I still thought you could modify the individual items... is this a bug or am I doing it wrong?
    public void thisTestIsBroken() {
        // search with all input fields unchecked/cleared.
        gotoPage("/search/basic");
        List<HtmlElement> elements = getHtmlPage().getElementsByName("resourceTypes");
        for (HtmlElement element : elements) {
            HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) element;
            checkbox.setChecked(false);
        }
        submitForm("Search");
    }

    @Test
    public void testBasicSearchView() {
        super.testBasicSearchView();
        submitForm("Search");
        assertNoErrorTextPresent();
    }

    @Test
    public void testAdvancedSearchView() {
        super.testAdvancedSearchView();
        submitForm("Search");
        assertNoErrorTextPresent();
    }

    @Test
    public void testAdvanceSearchWithCultureKeywords()  {
        reindex();
        super.testAdvancedSearchView();
        selectAllResourceTypes();
        // FIXME: magic numbers
        String[] cultureIds = { "19", "39", "40", "8", "25" };
        setInput("approvedCultureKeywordIds", cultureIds);
        submitForm("Search");
        logger.debug(getPageText());
        assertTextPresent("Archeological Survey and Architectural Study of Montezuma Castle National Monument");
        assertTextPresent("2008 New Philadelphia Archaeology Report");

    }
    
     
     private void reindex() {
         gotoPage("/searchindex/build");
         gotoPage("/searchindex/checkstatus");
         logger.info(getPageCode());
         int count = 0;
         while (!getPageCode().contains("\"percentDone\" : 100")) {
             try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("InterruptedException during reindex.  sorry.");
            }
             gotoPage("/searchindex/checkstatus");
             logger.info(getPageCode());
             if (count == 1000) {
                 fail("we went through 1000 iterations of waiting for the search index to build... assuming something is wrong");
             }
             count++;
         }
     }
     
     @Test
     public void testUncontrolledSiteTypeKeywords() {
         reindex();
         gotoPage("/project/add");
         String title = "testing uncontrolled site type keywords";
         String keyword = "uncontrolledsitetypeone";
         setInput("project.title", title);
         setInput("project.description", "testing uncontrolled site type keywords");
         setInput("status", "ACTIVE");
         setInput("uncontrolledSiteTypeKeywords[0]", keyword);
         submitForm();
         gotoPage("/search/advanced");
         selectAllResourceTypes();
         setInput("uncontrolledSiteTypeKeywords[0]", keyword);
         submitForm("Search");
         assertTextPresent(title);
         
     }
     
     
        
    

}
