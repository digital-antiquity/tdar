package org.tdar.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.bean.SearchFieldType;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;

/**
 * testing searches that are more involved than the basic browsing use cases.
 * 
 * @author jimdevos
 * 
 */
public class SearchWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String ADVANCED_SEARCH_BASE_URL = "/search/advanced";
    private static final String BASIC_SEARCH_BASE_URL = "/search/basic";
    private static final String SEARCH_RESULTS_BASE_URL = "/search/results";
    private static int indexCount = 0;

    private void selectAllResourceTypes() {
        List<DomElement> iter = getHtmlPage().getElementsByName("resourceTypes");
        for (int i = 0; i < iter.size(); i++) {
            HtmlCheckBoxInput cb = ((HtmlCheckBoxInput) iter.get(i));
            cb.setChecked(false);
            logger.trace("checkbox: " + cb);
        }

    }

    @Before
    // FIXME: bad for several reasons. @BeforeClass would be better, but need to make reindex public void static first.
    public void indexFirst() {
        if (indexCount++ < 1) {
            reindex();
        }
    }

    // FIXME: I get an exception when I try to modify the contents of the
    // checkbox collection... why? I know the collection itself is immutable, but
    // I still thought you could modify the individual items... is this a bug or
    // am I doing it wrong?
    public void thisTestIsBroken() {
        // search with all input fields unchecked/cleared.
        gotoPage(BASIC_SEARCH_BASE_URL);
        List<DomElement> elements = getHtmlPage().getElementsByName(
                "resourceTypes");
        for (DomElement element : elements) {
            HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) element;
            checkbox.setChecked(false);
        }
        submitForm("Search");
    }

    @Test
    public void testBasicSearchView() {
        gotoPage("/search/simple");
        assertTextPresentInPage("Search");
        submitForm("Search");
        assertNoErrorTextPresent();
    }

    @Test
    @Override
    public void testAdvancedSearchView() {
        super.testAdvancedSearchView();
        submitForm("Search");
        assertNoErrorTextPresent();
    }

    @Test
    public void testAdvanceSearchWithCultureKeywords() {
        reindex();
        gotoPage(ADVANCED_SEARCH_BASE_URL);

        selectAllResourceTypes();
        // FIXME: magic numbers
        String[] cultureIds = { "19", "39", "40", "8", "25" };
        // we are creating text inputs instead of checkboxes, but, like, who
        // cares?
        createTextInput("groups[0].approvedCultureKeywordIdLists[0]",
                Arrays.asList(cultureIds));
        submitForm("Search");
        // logger.debug(getPageText());
        assertTextPresent("Archeological Survey and Architectural Study of Montezuma Castle National Monument");
        assertTextPresent("2008 New Philadelphia Archaeology Report");
    }

    @Test
    public void testUncontrolledSiteTypeKeywords() {
        reindex();
        gotoPage("/document/add");
        String title = "testing uncontrolled site type keywords";
        String keyword = "uncontrolledsitetypeone";
        setInput("document.title", title);
        setInput("document.date", 2000);
        setInput("document.description",
                "testing uncontrolled site type keywords");
        setInput("status", "ACTIVE");
        setInput("projectId", "1");
        setInput("uncontrolledSiteTypeKeywords[0]", keyword);
        submitForm();
        gotoPage(ADVANCED_SEARCH_BASE_URL);
        selectAllResourceTypes();
        setInput("groups[0].fieldTypes[0]",
                SearchFieldType.FFK_SITE_TYPE.name());
        createTextInput("groups[0].uncontrolledSiteTypes[0]", keyword);
        logger.trace("page code\n\n {}\n\n", getPageCode());
        submitForm("Search");
        assertTextPresent(title);
    }

    @Test
    public void testSorting() {
        for (SortOption option : SortOption
                .getOptionsForContext(Resource.class)) {
            gotoPage(SEARCH_RESULTS_BASE_URL + "?query=&sortField="
                    + option.name());
        }
    }

    @Test
    public void testMapAndOtherOrientations() {
        for (DisplayOrientation orientation : DisplayOrientation.values()) {
            gotoPage(SEARCH_RESULTS_BASE_URL + "?query=Philadelphia&orientation=" + orientation.name()); // has obfuscated data
            gotoPage(SEARCH_RESULTS_BASE_URL + "?query=Nebraska&orientation=" + orientation.name()); // doesn't
        }
    }

    @Test
    public void testIdSearch() {
        gotoPage(SEARCH_RESULTS_BASE_URL + "?query=&id=" + TestConstants.PROJECT_ID);
    }

    @Test
    public void testInstitutionSearch() {
        gotoPage("/search/institutions?query=Arizona");
    }

    @Test
    public void testPeopleSearch() {
        gotoPage("/search/people?query=Kintigh");
    }

    @SuppressWarnings("unused")
    @Test
    public void testLatLongSearch() throws InterruptedException {

        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setNorth(45.336701909968106);
        latLong.setSouth(32.175612478499325);
        latLong.setEast(-83.0126953125);
        latLong.setWest(-93.7412109375);
        Long draft = setupDocumentWithProject("Philadelphia 1", latLong, Status.ACTIVE, null, null);

        gotoPage(SEARCH_RESULTS_BASE_URL
                + "?groups%5B0%5D.operator=AND&groups%5B0%5D.fieldTypes%5B0%5D=ALL_FIELDS&groups%5B0%5D.allFields%5B0%5D=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLongitude=-85.078125&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLatitude=38.341656192795924&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLongitude=-92.373046875&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLatitude=43.58039085560786&");
        assertTextPresent("Philadelphia");
        gotoPage(SEARCH_RESULTS_BASE_URL + "?latLongBox=-92.373046875,38.341656192795924,-85.078125,43.58039085560786&");
        assertTextPresent("Philadelphia");
    }

    @Test
    @Rollback
    public void testFacets() throws InstantiationException,
            IllegalAccessException {
    	 List<ResourceType> types = Arrays.asList(ResourceType.DATASET, ResourceType.DOCUMENT, ResourceType.IMAGE, ResourceType.GEOSPATIAL);
        for (ResourceType rt : types) {
            createResourceFromType(rt, "test");
        }

        reindex();
        for (ResourceType type :types) {
            gotoPage(SEARCH_RESULTS_BASE_URL + "?");
            logger.debug("{} -- TYPE", type);
            if (getPageLink(type.getPlural()) != null) {
                clickLinkOnPage(type.getPlural());
            } else {
                clickLinkOnPage(type.getLabel());
            }
            boolean sawSomething = false;
            for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll("h3 a")) {
                Element element = (Element) element_;
                String href = element.getAttribute("href");
                if (!element.getAttribute("href").toLowerCase()
                        .contains("bookmark")) {
                    assertTrue(
                            String.format("element should have the resource type (%s) in the url: %s", type.getUrlNamespace(), href),
                            href.contains(String.format("/%s/", type.getUrlNamespace())));
                    sawSomething = true;
                }
            }
            logger.info("{} - {}", type, sawSomething);
            assertTrue(String.format("should have at least one result for: %s", type.name()), sawSomething);
        }
    }

    private HtmlAnchor getPageLink(String plural) {
        try {
            return getHtmlPage().getAnchorByText(plural);
        } catch (Exception e) {
            
        }
        return null;
    }

    @Test
    @Rollback
    public void testPagination() {
        gotoPage("/search/results?recordsPerPage=2&includedStatuses=DRAFT&includedStatuses=ACTIVE&includedStatuses=DELETED");
        boolean sawSomething = false;
        for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll(".pagin a")) {
            Element element = (Element) element_;
            String href = element.getAttribute("href");
            if (!element.getAttribute("href").toLowerCase().contains("bookmark")) {
                gotoPage("/search/results" + href);
                assertTextNotPresent("greater than total");
                if (href.contains("25")) {
                    logger.info(getPageBodyCode());
                }
                sawSomething = true;
            }
        }
        assertTrue("should see links on the page", sawSomething);
    }

    @Test
    public void testRangeSearch() {
        gotoPage(SEARCH_RESULTS_BASE_URL
                + "?&groups%5B0%5D.fieldTypes%5B1%5D=DATE_CREATED&groups%5B0%5D.createdDates%5B1%5D.start=2001&groups%5B0%5D.createdDates%5B1%5D.end=2009&__multiselect_includedStatuses=");
        assertTextPresent("Date is between 2001 and 2009");
    }

    @Test
    @Rollback
    public void testSearchURLs() {
        List<String> urls = new ArrayList<String>();
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22St.+Bernard+(County)%22");
        urls.add(SEARCH_RESULTS_BASE_URL + "?startRecord=0&documentType=BOOK&query=%22lightning%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?startRecord=0&query=%22Nowood+River+Drainage%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL + "?startRecord=0&query=%22Neptune%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL + "?startRecord=0&uncontrolledCultureKeywords=Roman&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&recordsPerPage=10&siteNameKeywords=5MT4683&resourceTypes=DOCUMENT&documentType=BOOK&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL + "?uncontrolledSiteTypeKeywords=dam&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL + "?geographicKeywords=US%20(ISO%20Country%20Code)");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=ontario&title=&id=&projectIds%5B0%5D=&resourceTypes=DATASET&coverageDates%5B0%5D.id=&coverageDates%5B0%5D.dateType=NONE&coverageDates%5B0%5D.startDate=&coverageDates%5B0%5D.endDate=&coverageDates%5B0%5D.description=&geographicKeywords%5B0%5D=&minx=&maxx=&miny=&maxy=&__multiselect_investigationTypeIds=&siteNameKeywords%5B0%5D=&__multiselect_approvedSiteTypeKeywordIds=&uncontrolledSiteTypeKeywords%5B0%5D=&__multiselect_materialKeywordIds=&__multiselect_approvedCultureKeywordIds=&uncontrolledCultureKeywords%5B0%5D=&temporalKeywords%5B0%5D=&otherKeywords%5B0%5D=&searchSubmitterIds%5B0%5D=&searchSubmitter.lastName=abc&searchSubmitter.firstName=&searchSubmitter.email=&searchSubmitter.institution.name=&searchContributorIds%5B0%5D=&searchContributor.lastName=&searchContributor.firstName=&searchContributor.email=&searchContributor.institution.name=&sortField=RELEVANCE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=canada&title=&id=&projectIds%5B0%5D=&resourceTypes=DATASET&coverageDates%5B0%5D.id=&coverageDates%5B0%5D.dateType=NONE&coverageDates%5B0%5D.startDate=&coverageDates%5B0%5D.endDate=&coverageDates%5B0%5D.description=&geographicKeywords%5B0%5D=&minx=&maxx=&miny=&maxy=&__multiselect_investigationTypeIds=&siteNameKeywords%5B0%5D=&__multiselect_approvedSiteTypeKeywordIds=&uncontrolledSiteTypeKeywords%5B0%5D=&__multiselect_materialKeywordIds=&__multiselect_approvedCultureKeywordIds=&uncontrolledCultureKeywords%5B0%5D=&temporalKeywords%5B0%5D=&otherKeywords%5B0%5D=&searchSubmitterIds%5B0%5D=&searchSubmitter.lastName=abc&searchSubmitter.firstName=&searchSubmitter.email=&searchSubmitter.institution.name=&searchContributorIds%5B0%5D=&searchContributor.lastName=&searchContributor.firstName=adam&searchContributor.email=&searchContributor.institution.name=&sortField=RELEVANCE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?__multiselect_groups%5B0%5D.approvedSiteTypeIdLists%5B0%5D=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLatitude=&groups%5B0%5D.operator=AND&groups%5B0%5D.fieldTypes%5B2%5D=KEYWORD_CULTURAL&sortField=RELEVANCE&__multiselect_groups%5B0%5D.approvedCultureKeywordIdLists%5B1%5D=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLongitude=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLongitude=&groups%5B0%5D.approvedSiteTypeIdLists%5B0%5D=272&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLatitude=&groups%5B0%5D.fieldTypes%5B0%5D=KEYWORD_SITE&groups%5B0%5D.approvedCultureKeywordIdLists%5B1%5D=4");

        List<String> empty = new ArrayList<String>();
        List<String> toobig = new ArrayList<String>();
        for (String url : urls) {
            gotoPage(url);
            if (getPageCode().contains("No records match the query")) {
                empty.add(url);
            }

            if (getPageCode().contains(" is greater than total number of results")) {
                toobig.add(url);
            }

            assertNoErrorTextPresent();
        }
        for (String url : empty) {
            logger.warn("SEARCH EMPTY: {}", url);
        }
        for (String url : toobig) {
            logger.warn("RESULTS REQUEST TOO HIGH: {}", url);
        }
    }

    @Test
    public void testPerson() {
        reindex();
        gotoPage("/search/person");
        gotoPage("/search/people?query=Manney");
    }

    @Test
    public void testInstitution() {
        reindex();
        gotoPage("/search/institution");
        gotoPage("/search/institutions?query=Arizona");
    }

    @Test
    public void testCollection() {
        reindex();
        gotoPage("/search/collection");
        gotoPage("/search/collections?query=test");
    }

    @Test
    public void testPaginationIssuesReturningInput() {
        testPaginationError("/search/collections?query=test&startRecord=100000");
        testPaginationError("/search/people?query=test&startRecord=100000");
        testPaginationError("/search/institutions?query=test&startRecord=100000");
        testPaginationError("/search/results?query=test&startRecord=100000");

    }

    private void testPaginationError(String string) {
        int status = gotoPageWithoutErrorCheck(string);
        logger.debug("status: " + status);
        logger.debug(getPageText());
        // assertNotEquals(200, status);
        assertNoEscapeIssues();
        // assertNoErrorTextPresent();
        assertTrue(getPageText().contains("cannot be found") || getPageText().contains("greater than total number of"));

    }

    @Test
    public void testTitleSearch() {
        reindex();
        String testTitle = "Archeological Survey and Architectural Study of Montezuma Castle National Monument";
        gotoPage(ADVANCED_SEARCH_BASE_URL);

        createTextInput("groups[0].titles[0]", testTitle);
        submitForm("Search");
        assertNoErrorTextPresent();
        assertTextPresent(testTitle);
    }

    @Test
    public void testSearchRegistrationDate() {
        // mostly just test whether struts can handle typeconversion to
        // range<date>
        reindex();
        gotoPage(ADVANCED_SEARCH_BASE_URL);
        createTextInput("groups[0].registeredDates[0].start", "1/1/10");
        createTextInput("groups[0].registeredDates[0].end", "1/1/12");
        logger.trace(getPageText());
        submitForm("Search");
        // FIXME: I can tell from looking that this search is failing, but this
        // assert isn't failing... basically we should not be on the INPUT page
        // at this
        // point
        assertTextNotPresent("id=\"searchGroups\"");
    }

    @Test
    // perform simple type search, then retrn to the form via "modify search" and confirm form values are retained
    // FIXME: magic numbers
    public void testModifySearchApprovedSiteType() throws IOException {
        // reindex not necessary, we don't care about the results

        gotoPage(ADVANCED_SEARCH_BASE_URL);
        setInput("groups[0].fieldTypes[0]", SearchFieldType.KEYWORD_SITE.name());
        createTextInput("groups[0].approvedSiteTypeIdLists[0]", "251");
        submitForm("Search");
        clickLinkWithText("Refine your search »");
        HtmlCheckBoxInput element = (HtmlCheckBoxInput) htmlPage.getElementById("groups[0].approvedSiteTypeIdLists[0]-1");
        assertNotNull("could not find element", element);
        assertTrue("checkbox isn't checked", element.isChecked());
    }

    @Test
    // refine a collection search
    public void testModifyCollectionSearch() {
        String name = "superduper";
        createTestCollection(name, "description goes here", getSomeResources());
        gotoPage("/search/collection");
        setInput("query", name);
        submitForm("Search");
        clickLinkWithText("Refine your search »");
        checkInput("query", name);
    }

    @Test
    public void testModifyLegacySiteNameSearch() {
        // here we skip the step of creating the site name... we just need to make sure search terms on form are populated
        String siteName = "disneyland";
        gotoPage("/search/advanced?siteNameKeywords=" + siteName);
        checkInput("groups[0].siteNames[0]", siteName);
    }

}
