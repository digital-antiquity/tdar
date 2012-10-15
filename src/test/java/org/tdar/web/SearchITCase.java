package org.tdar.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.SearchIndexService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.search.SearchFieldType;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

/**
 * testing searches that are more involved than the basic browsing use cases.
 * 
 * @author jimdevos
 * 
 */
public class SearchITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String ADVANCED_SEARCH_BASE_URL = "/search/advanced";
    private static final String BASIC_SEARCH_BASE_URL = "/search/basic";
    private static final String SEARCH_RESULTS_BASE_URL = "/search/results";

    @Autowired
    SearchIndexService indexService;

    private void selectAllResourceTypes() {
        List<HtmlElement> iter = getHtmlPage().getElementsByName("resourceTypes");
        for (int i=0; i <= iter.size(); i++) {
            HtmlCheckBoxInput cb = ((HtmlCheckBoxInput) iter.get(i));
            cb.setChecked(false);
            logger.trace("checkbox: " + cb);
        }

    }

    // FIXME: I get an exception when I try to modify the contents of the
    // checkbox collection... why? I know the collection itself is immutable, but
    // I still thought you could modify the individual items... is this a bug or
    // am I doing it wrong?
    public void thisTestIsBroken() {
        // search with all input fields unchecked/cleared.
        gotoPage(BASIC_SEARCH_BASE_URL);
        List<HtmlElement> elements = getHtmlPage().getElementsByName(
                "resourceTypes");
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

    private void reindex() {
        gotoPage("/admin/searchindex/build");
        gotoPage("/admin/searchindex/checkstatus");
        logger.info(getPageCode());
        int count = 0;
        while (!getPageCode().contains("\"percentDone\" : 100")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("InterruptedException during reindex.  sorry.");
            }
            gotoPage("/admin/searchindex/checkstatus");
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
        setInput("project.description",
                "testing uncontrolled site type keywords");
        setInput("status", "ACTIVE");
        setInput("uncontrolledSiteTypeKeywords[0]", keyword);
        submitForm();
        gotoPage(ADVANCED_SEARCH_BASE_URL);
        selectAllResourceTypes();
        setInput("groups[0].fieldTypes[0]",
                SearchFieldType.FFK_SITE_TYPE.name());
        createTextInput("groups[0].uncontrolledSiteTypes[0]", keyword);
        logger.debug("page code\n\n {}\n\n", getPageCode());
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
    public void testIdSearch() {
        gotoPage(SEARCH_RESULTS_BASE_URL + "?query=&id="
                + TestConstants.PROJECT_ID);
    }

    @Test
    @Rollback
    public void testFacets() throws InstantiationException,
            IllegalAccessException {
        for (ResourceType rt : ResourceType.values()) {
            final String path = "/" + rt.getUrlNamespace() + "/add";
            gotoPage(path);
            setInput(String.format("%s.%s", rt.getFieldName(), "title"), "test");
            setInput(String.format("%s.%s", rt.getFieldName(), "description"), "test");
            if (isCopyrightMandatory() && isNotAddProject(path)) {
                setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
                setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
            }
            if (!rt.isProject()) {
                setInput(String.format("%s.%s", rt.getFieldName(), "date"), "2134");
            }
            if (rt.isSupporting()) {
                setInput("fileInputMethod", "text");
                if (rt == ResourceType.ONTOLOGY) {
                    setInput("fileTextInput", "text\ttext\r\n");
                } else {
                    setInput("fileTextInput", "text,text\r\n");
                }
            }
            submitForm();
        }

        reindex();
        for (ResourceType type : ResourceType.values()) {
            gotoPage(SEARCH_RESULTS_BASE_URL + "?");
            clickLinkOnPage(type.getPlural());
            boolean sawSomething = false;
            for (Element element : querySelectorAll(".resource-list h5 a")) {
                String href = element.getAttribute("href");
                if (!element.getAttribute("href").toLowerCase()
                        .contains("bookmark")) {
                    assertTrue(
                            String.format(
                                    "element should have the resource type (%s) in the url: %s",
                                    type.getUrlNamespace(), href),
                            href.contains(String.format("/%s/",
                                    type.getUrlNamespace())));
                    sawSomething = true;
                }
            }
            logger.info("{} - {}", type, sawSomething);
            assertTrue(String.format("should have at least one result for: %s", type.name()), sawSomething);
        }
    }

    private boolean isCopyrightMandatory() {
        return TdarConfiguration.getInstance().getCopyrightMandatory();
    }

    private boolean isNotAddProject(final String path) {
        return !path.endsWith("project/add");
    }

    @Test
    @Rollback
    public void testSearchURLs() {
        List<String> urls = new ArrayList<String>();
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%2215DA140%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%2215HE698%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%2238BK235%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22400+Sq+Ft%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%2241VV456%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Crosby+Site%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Dendroclimatic+Studies%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22DLNR+%2F+SHPD%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22J.+D.+Swenson%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22LA+Plata+Archaeological+District%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Short-Term+Habitation%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Sieco%2C+Inc.%2C+Columbus%2C+In%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22St.+Marys%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Telegraph+Peak+7.5'+quad%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Utilized+Ground+Stone+Slabs%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&query=%22Western+United+States%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%221DK94%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%2223PM21%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22Bellcow+Creek%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22J.+Donahue%22&recordsPerPage=20&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22James+P.+Quinn%22&recordsPerPage=20&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22Jo+Ann+E.+Kisselburge%22&recordsPerPage=20&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22Michael+L.+Hargrave%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22Monterey+(County)%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22North+Fork+of+the+Red+River%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22P1063-71H%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22San+Francisco%22&recordsPerPage=10&fileAccess=RESTRICTED&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&query=%22Sociopolitical+Studies%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&startRecord=0&uncontrolledCultureKeywords=Euroamerican&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&uncontrolledCultureKeywords=MEDLEY+PHASE&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK&uncontrolledCultureKeywords=Proto-Iroquoian&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%2222025+(Fips+Code)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22CONSTRUCTION+TECHNIQUES%22&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22Craft+production%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22Curation%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22Legislation%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22METHOD+AND+THEORY%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22Patent+Medicine%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&query=%22Rio+de+Flag%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&siteNameKeywords=Usedtobe+Ruin&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=BOOK_SECTION&startRecord=0&query=%2240109+(Fips+Code)%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=CONFERENCE_PRESENTATION&query=%2253013+(Fips+Code)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=CONFERENCE_PRESENTATION&query=%22Cultural+Resource+Management%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=CONFERENCE_PRESENTATION&query=%22State%2C+County%2C+and+Local+Government%22&startRecord=0&recordsPerPage=20&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=CONFERENCE_PRESENTATION&startRecord=0&query=%22Ili-002%22&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22HEARTHS%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22Platte+(County)%22&startRecord=0&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22Prehistoric+Copper+Artifacts%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22Reelfoot+Lake%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22Townsend+Ware%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&query=%22Two+Buttes+West+Site%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&siteNameKeywords=Blue+Creek&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&startRecord=0&query=%22Refuse+Disposal+Site%22&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=JOURNAL_ARTICLE&uncontrolledCultureKeywords=Antelope+Creek+Phase&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=OTHER&query=%22Bland+(County)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=OTHER&query=%22Geometric%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=OTHER&startRecord=0&query=%22Milwaukee%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=OTHER&uncontrolledCultureKeywords=Yupik+Eskimo&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=THESIS&query=%22Ceramic+Materials%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?documentType=THESIS&startRecord=0&uncontrolledSiteTypeKeywords=Ancient+earthwork&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?fileAccess=CITATION&startRecord=0&uncontrolledSiteTypeKeywords=Ball+Court&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?fileAccess=PARTIALLY_RESTRICTED&resourceTypes=DOCUMENT&startRecord=0&uncontrolledSiteTypeKeywords=Rock+alignment&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?fileAccess=RESTRICTED&resourceTypes=DOCUMENT&startRecord=0&uncontrolledSiteTypeKeywords=Domestic+Structures&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2201103+(Fips+Code)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221.10+Miles%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2212+Archeological+Sites%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2212089+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2213LE284+TO+286%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2213PM61+-+LARSEN+SITE%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2214RY411%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215Ba52%2C+15Ba125%2C+15Ba127%2C+15Ba128%2C+15Ba129%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215Fd78+through+15Fd82%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215FL100%2C+15FL101%2C+15FL102%2C+15FL103%2C+15FL104%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215HE671%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215MA34%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215ML332%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215NE58+-+1790S+HOUSE%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215NE58+-+1790S+HOUSE%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215PE72%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2215Si22%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2216+Archeological+Sites%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2216CL23%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2216LF96%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221838%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221848+Corporation%2C+Unknown+Location%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2218AN781%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2218HA222%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221BB181%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221BB432%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221CB153%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221CK75%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221JE406%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221LA96%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221LE185%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221LO42%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221MA749%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221MG135%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%221MR172%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2220th+Century+Building%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2221017+(Fips+Code)%22&startRecord=0&recordsPerPage=10&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2221085+(Fips+Code)%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2221Sc37%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2222053+(Fips+Code)%22&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2223CT169%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2223MD51%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2223NM162%22&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2223OZ94%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22260+BP%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%223+Sites+Noted+But+Not+Recorded%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2231GS30%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2232BA403%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2233+DL+383%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2233+TU+443%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2234DL30%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2234WN55%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2235043+(Fips+Code)%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2238079+(Fips+Code)%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%224+Pages%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241AN18%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241AN59%22&fileAccess=CITATION&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241AT226%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241BO80%22&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241HI80%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241JF23%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241KA36%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241LB2%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241LB40%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2241LU69%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2242043+(Fips+Code)%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2248405+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2248443+(Fips+Code)%22&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2248469+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2248471+(Fips+Code)%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%224927+West+11th+Street%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22500+Square+Feet%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%225000+-+6000+B.P.%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2251167+(Fips+Code)%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2255125+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2280-6.6%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2286+%2F+02+%2F+20%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2287+%2F+05+%2F+12%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2290-10.1%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%2292-4.4%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%229FU10%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22A.D.+1250+-+5650+B.C.%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22A.D.+1954+-+A.D.+1861%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22A.D.+200+-+1500+B.C.%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Aboriginal+Monuments%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Aboriginal+Subsistence%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Adaptation%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22ALABAMA+DEPARTMENT+OF+CONSERVATION%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Alachua+(County)%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Alaska+Commercial+Company%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Alice+M.+Emerson%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Allegany+(County)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Anne+Frazer+Rogers%22&startRecord=0&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Archaeological+Overview+%2F+Perspective%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Ashland+%2F+Belle+Helene+Plantation%22&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Athapaskan+Prehistory%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22athens%22&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Avon+Park+Air+Force+Range%22&fileAccess=CITATION&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22AXE+HEAD%22&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Baltimore-Cumberland+Turnpike%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Barry+Scott%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bartholomew+(County)%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bayou+LA+Batre%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bear+Creek+Ridge+Site%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Beaver+Creek+(Branch)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bee+(County)%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Belmont+(County)%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Berks+(County)%22&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bernards%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bettina+H.+Rosenburg%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Big+Sioux+River+(Branch)%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Bill+Wisler+Wetland+Restoration%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Blackburn+Canyon+7.5'+quad.%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22BLM+5-1087(N)%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Block+Mtn+7.5'+quad%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Boca+DE+Santa+Monica%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22BULGARIA%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Burlington+Chert%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22C.+M.+De+Ferrari%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-INY-21%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-INY-4581-H%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Caniometric+Relationships%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-RIV-1044-H%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-SBR-1224%22&fileAccess=CITATION&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-SBR-1633%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-SBR-4856%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-SBR-717%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CA-TRI-313%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cellar%22&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Central+Montana+Abstract+Style%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Central+Plains+Archeology%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Central+South+Dakota%22&fileAccess=CITATION&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cheyenne-Deadwood+Stage+Line%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Chimney+Foundations%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Christian+J.+Zier%22&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Christianity%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22City+of+Florence%2C+AL%22&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Clark+%28County%29%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CM420-Prehistoric+lithic+scatter%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Coco-Maricopa+Trail%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cookery%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Corduroy+Road%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cornplanter%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cos+Cob%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Creta+Water+System%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22CRUCIFIX%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cultural+resources+management+plan%22&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Culture+Contact%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Curated+At%3A+Arch+Ctr+U+Wisc%2C+Madison%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Cutting+Tools%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Daryl+G.+Noble%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Deer+Lodge+(County)%22&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Defence%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Dental+Evidence%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Disease+Concepts%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Dorothea+J.+Theodoratus%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Dune+Plant+Processing+Area%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E.+Blinman%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78+++++++AMA-TMP+++.J6+++++++1967%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78+++++++MNO-NEG+++.W5+++++++1978%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78+++++++SHA-NEG+++.C6+++++++1979%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78+++++++YUB-902+++.S7+++++++1978%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78+GLE-NEG+.03+1988%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22E78C15++++RIV.OVR+++.B4+++++++1981%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Early+Historic+Period%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Early+medieval%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Environmental+Impact%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Eric+Hill+and+Associates%2C+Inc%2C+Atlanta%2C+GA%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Eric+Hill+and+Associates%2C+Inc%2C+Atlanta%2C+GA%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Ethnohistorical%22&fileAccess=CITATION&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Excavations+At+Nashport+Mound%2C+Dillon+Lake%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Falcon+Dancer%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22FCR%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22FCR%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Field+Procedures%22&resourceTypes=DOCUMENT&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Field+School+Excavations%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Flaked+Lithics%2C+Faunal+Material%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Flint+Flakes%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Florida+Dahrm+Number+923%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Florida+Dahrm+Number+934%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Flying+D+Archaeological+Project%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Forager%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Fort+Leonard+Wood+Timber+Sale%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22G.+L.+Grosscup%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Gary+and+Scallorn+Projectile+Pts.%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Gary+M.+Selinger%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22George+S.+Smith%22&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Giddings%22&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Glacier+Bay+National+Park+and+Preserve+(Alaska)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22GLASS+ARTIFACT+ANALYSIS%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Glenn+Unit+Well%2C+Site+Lt280%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Gouge+Eye+Well+QUAD%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22grade+stabilization+structure%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Granite%22&startRecord=0&recordsPerPage=10&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Greagel-Plumas+County+Line+Hwy.+89%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Greenville+Township%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Gresham+Site%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Ground+Stone+Analysis%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22GUNFLINT%22&resourceTypes=DOCUMENT&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Gypsum+Period+To+Shoshonean+Period%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Halls+Canyon%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Harvey+Kohnitz%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Helladic%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Herdegren's+Birdtail+Butte+Site%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hillsboro%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Historic+Archaeology+Research%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Historic+Component%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Historic+Road+Remnant%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Historic+Site+Management+Plans%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22History+of+Research%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hohokam+Shell+Adornments%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Holly+Geognegan%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Holly+Geognegan%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Holtville+East+7.5'+quad%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hot+Spring+(County)%22&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22House+Foundation%22&startRecord=0&recordsPerPage=10&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22huckleberry+creek%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hugo+Reservoir%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hunter+H.+Martin+%26+Associates%2C+Paducah%2C+KY%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Hwy+398%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Indian+Health+Service+New+Mexico%22&fileAccess=CITATION&startRecord=0&recordsPerPage=20&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Iowa+Cedarriver+Basin%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Isolated+Disturbances%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Jacal+Structure+Sites%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Jacal+Structure+Sites%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Jack+T.+Hughes%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22John+A.+Gifford%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22John+H.+Brumley%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22John+S.+Kopper%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Johnson+Rancho%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Journals%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Kenneth+G.+and+Eleanor+B.+Shockey%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Kirk+Serrated%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Kise+Franks+%26+Straw+Historic+Preservation+Group%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Known+Prehist.+Lithic+Quarry%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Kurt+Schweigert%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22L.+Fitzwater%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22La+Plata%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lake+Mead+Culture+History%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lake+Rodemacher+Basin%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Laplata+Archeological+Consultants%2C+Inc.+%2C+Dolores%2C+Co%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Las-968%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22late+Middle+and+early+Late+Woodland+periods%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Late+Prehistoric+Remains%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Laura+E.+Miller%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lauren+Cook%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lettieri%2C+McIntyre%2C+and+Assoc.%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Life+Expectancy%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lisa+Huckell%22&startRecord=0&fileAccess=PARTIALLY_RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22List+of+Point+Classes+By+Site%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lithic+Processing+Site%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Living+Components%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Lorrie+Lincoln-Babb%22&startRecord=0&recordsPerPage=20&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22M.+Cassandra+Hill%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Malcolm+Pirnie%2C+Inc.%2C+Minneapolis%2C+MN%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Marathon+Pipeline+Company%2C+Unknown+Location%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Marco+Island+(Fla.+%3A+Island)%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Marian+E.+White%2C+SUNY-Buffalo%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Marine+Species+List%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22McKean+Complex+Points%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22McKenna+Et+AL.%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mf+%231038%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mf+%23229%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mf+%232627%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mf+%23526%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mf+%23998%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Michael+J.+Rodeffer%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Midden+Hydrogen-Ion+Analysis%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Middle+Formative%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mod-983%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22MULGA+MINE%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Mus-1107-9+%2F+Dump+Site%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Myles+R.+Miller+III%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22National+Museum+of+Natural+History%2C+Smithsonian+Institution%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22New+Mexico+(State+%2F+Territory)%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22NO+SDI'S+LISTED%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Nonagriculture+Society%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22North+Truro%22&resourceTypes=IMAGE&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Northern+Belize%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22NORTHWEST+QUARTER+OF+SECTION+32%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Northwestern+Plains+Culture+Area%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22NPS%2C+Interagency+Archaeological+Service%2C+Denver%2C+Co%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Old+Slough+Site%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Olin%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Omega%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Onion+Valley+Cemetery%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Onondaga+(County)%22&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Open+Habitation+Site%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Owen+Davis%22&startRecord=0&recordsPerPage=20&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Owens+Reservoir+7.5'%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22P-55-001725+(CA-TUO-000705)%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Pages%3A+23+%2B+Map+and+Site+Records%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Palo+Pinto+(County)%22&resourceTypes=DOCUMENT&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Paul+J.+F.+Schumacher%22&startRecord=0&recordsPerPage=10&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Paymaster+Ridge+QUAD%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Pechanga+7.5'+1968+QUAD%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Pend+Oreille+(County)%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Philadelphia+Turnpike%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Piled+Rock%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Pitted+Stone%22&resourceTypes=IMAGE&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Plate+Fragments%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Possible+Burial+Mound%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22POWAY+SHOPPING+PLAZA%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Prehistoric+Archeological+Sites%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Prehistoric+Occupation+Site%22&resourceTypes=DOCUMENT&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Prentiss%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Probable+Procurement+Activities%22&fileAccess=CITATION&startRecord=0&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Procurement+Processing+Site%22&startRecord=0&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Projectile+points+and+bifaces%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Q+%2F+A+Consulting+Engineers%2C+Inc.%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22QUAIL+CANYON+CREEK%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Quantitative+Methods%22&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22R.+Thomas+Ray%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22R.+Thomas+Ray%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Railroad+Water+Tanks%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Ramada+Express+Hotel+%26+Casino%2C+Inc.%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Red+Horn%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22RED+RIVER+DRAINAGE%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Renovations+of+Clear+Bay%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Republic+Geothermal%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Residence+3686+East+Arch+Road%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Residential+Site%22&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Reynolds+Street%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Rincon+DE+Los+Bueyes%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Rio+Blanco+(County)%22&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Room+47%22&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Rosa+B+%2F+W%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Route+La8+Project%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Roy+F+Weston+Inc%22&resourceTypes=DOCUMENT&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22S.+T.+Heipel%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22San+Antonio+Missions+National+Historical+Park%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22San+Luis+Valley%2C+Colorado%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Santa+Ynez+Mountains%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22SEAC+Accession+Number+871%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22SECTION+29%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Separation+Flats+Basin%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Sergeant+Bluff%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22SHA+contract+no.+A750-233%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Shamonistic+Curing+Practices%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Short-Term+Occupation%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Short-Term+Occupation%22&resourceTypes=PROJECT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Siberian+Ibex+History%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Site+Evaluation+%2F+Testing%22&startRecord=0&fileAccess=PARTIALLY_RESTRICTED&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Site+Stabilization%22&startRecord=0&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Sjc+Report+93-Sjc-016%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22SMALL+RODENT+REMAINS%22&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Smith+River+Drainage+System%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Smuggler+Gulch%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Soils%22&fileAccess=PARTIALLY_RESTRICTED&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22South+Carolina+Dispensary+union+flask%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Southeast%22&resourceTypes=IMAGE&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Spanish+Contact+and+Colonization%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22St.+Lucie+(County)%22&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Steamboat+Building+%2F+Sawmill%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Sterling+Pond%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22stockpile%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22stone%22&startRecord=0&recordsPerPage=10&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Surplus+Land%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22TECATE+7.5'+1960+QUAD%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Teller+Reservoir%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Temporal+Placement%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Tippah+River+Basin%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Tramway%22&fileAccess=CITATION&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Trinidad+Lake+Archeological+Project%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Trout+Run+Creek+Basin%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22True%2C+D.+L.%22&startRecord=0&recordsPerPage=10&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22UNIVERSITY+OF+MEMPHIS%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22University+of+Utah+Archaeological+Center%2C+Salt+Lake+City%2C+UT%22&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Unrecorded+Historical%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22US+41%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22US+Army+Corps+of+Engineers%2C+Kansas+City+District%2C+Purchase+Order+DACW41-76-M-+9957%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22USFS%2C+Routt+Nat.+For.%2C+Hahns+Peak+Dist.%2C+Steamboat+Spgs.%2C+Co%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22USGS+CADY+MOUNTAINS+15'+QUAD%22&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Various+Sites%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22W.+I.+Woods%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22W.+Mark+McCallum%22&fileAccess=CITATION&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Wakulla+Cave%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Walrus%22&startRecord=0&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Was+Katz+Dump+No.S+845052+and+945053%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Washoe+Lake%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Waterway%2C+Cheyenne-Arapaho+allotment+2946%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Wayne+E.+Clark%22&fileAccess=CITATION&startRecord=0&recordsPerPage=20&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Weirton%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22WELCH+09%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22West+Fork+Floodplain%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22WHITE-TAILED+DEER%22&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Whitley+City%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Windover%22&startRecord=0&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Wisconsinan%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Wooden+%26+Concrete+Footings%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Woods+Mine%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Wyoming+Dept.+of+Commerce%2C+Office+of+the+Wyoming+State+Arche%22&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Xpi-017%22&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=%22Yavapai+County%22&startRecord=0&fileAccess=PARTIALLY_RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=burial+in+glen+canyon+national+recreation+area");
        urls.add(SEARCH_RESULTS_BASE_URL + ".action?query=Inca");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=Montana+Archaeological+Society+2001+Trowel+and+Pen+Award+to+Emmett+a+Stallcop");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=related+to+fort+larned");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=this+letter+provides+the+department+of+interior");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?resourceTypes=DOCUMENT&startRecord=0&uncontrolledSiteTypeKeywords=Storage+pit&fileAccess=RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?searchProjects=true&query=&resourceTypes=ONTOLOGY&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=0802&resourceTypes=PROJECT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Desert+Queen+Ranch&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Dumoussay+Grave&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=El+Portal&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Golden+Ball+Tavern&startRecord=0&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Kerma&resourceTypes=DATASET&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Kubur+el-Bid&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DATASET");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Las+Capas&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Midvale+site&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Pueblo+Grande+(AZ+U%3A9%3A7)&startRecord=0&documentType=OTHER&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Shoofly+Village&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Stone+Pipe&startRecord=0&documentType=CONFERENCE_PRESENTATION&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?siteNameKeywords=Tumacacori+National+Historical+Park&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&fileAccess=CITATION&quer=&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%2206115+(Fips+Code)%22&recordsPerPage=10&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%221AU358%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%2240HW46%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%2267-0.1%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Archeological+Reconnaissance+Report%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22City+of+Chula+Vista+Department+of+Planning%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Cultural+Evolution%22&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22E78+++++++SHA-818+++.C5+++++++1979%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Field+Procedures%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Kuaua+Pueblo+(La187)%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Las-967%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Na14099%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Pactolus%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Recommendations+To+Program%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Robert+A.+Warnock%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22Rush+Creek%22&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22University+of+Maryland%22&resourceTypes=DOCUMENT&fileAccess=RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22UT-SA%2C+CAR%2C+San+Antonio%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK&query=%22West+Oneonta%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%2284-0.8%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Arrow+Shaft+Straighteners%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22David+Doyel%22&recordsPerPage=20&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Denton+Mounds%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22formative%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22hydrology%22&recordsPerPage=10&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Pacific+Ocean+%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Till%22&recordsPerPage=20&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Utopia%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=BOOK_SECTION&query=%22Xmk-044%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=CONFERENCE_PRESENTATION&query=%22Lithic+Tools%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=CONFERENCE_PRESENTATION&query=%22Stabilization%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=JOURNAL_ARTICLE&query=%2223MN380%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=JOURNAL_ARTICLE&query=%22Au+Train%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=JOURNAL_ARTICLE&query=%22Castor+Canadensis%22&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=JOURNAL_ARTICLE&query=%22Eugene+A.+Marino%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=OTHER&query=%22Red+Deer+Creek+Watershed%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=OTHER&query=%22St.+Croix%22&fileAccess=RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=OTHER&query=%22Stone+Tools%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=OTHER&query=%22Transylvania%22&resourceTypes=DOCUMENT&fileAccess=RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=THESIS&query=%22Public+Involvement%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&documentType=THESIS&query=%22Vista%22&recordsPerPage=10&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&fileAccess=CITATION&resourceTypes=DOCUMENT&uncontrolledSiteTypeKeywords=Encampment&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE&query=%22Hawkins+(County)%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2205029+(Fips+Code)%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2205225+Rancho+Santa+Fe+7.5'+1967+QUAD%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2207985+Native+American%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2214LT303%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2215CH429%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2215Me75%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2215RU77%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2218PR117%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2219197+(Fips+Code)%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%221EE183%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%221JE113%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%221MN54%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%221SU9%22&resourceTypes=DOCUMENT&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%221WI221%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2221191+(Fips+Code)%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2221207+(Fips+Code)%22&recordsPerPage=10&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2222SU631%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2223CN3%22&fileAccess=CITATION&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2233+FR+107%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2238CH949%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%223LN42%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2241Bp229%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2241GG12%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2248185+(Fips+Code)%22&fileAccess=CITATION&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2249+PP%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2251053+(Fips+Code)%22&recordsPerPage=10&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2255089+(Fips+Code)%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2255089+(Fips+Code)%22&recordsPerPage=10&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%227.+75+Miles%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2280+%2F+01+%2F+11%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%2280+%2F+10+%2F+00%22&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22A.D.+1855%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22A.D.+450+-+1050+B.C.%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Agua+Dulce+7.5'+quad%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Ar068%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Archaeoentomology%22&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Archeological+Site+Significance%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Arlington+Custom+Homes%2C+Inc.%2C+Millersville%2C+Maryland%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Arrowshaft%22&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22B.+Hibbets%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Big+Bone+Lick%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Bone+and+Antler+Artifact+Analysis%22&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Borden+(County)%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22British+Virgin+Islands%22&recordsPerPage=10&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Burns+Cemetery%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Butler+Site%22&recordsPerPage=10&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22C.+Andrew+Buchner%22&recordsPerPage=10&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Cairns+%2F+rock+features%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Carabelle+Harbor+Navigation+Project%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22CA-SBR-8033%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Ceramic%22&fileAccess=CITATION&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Chace+79%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Chipped+Stone%22&resourceTypes=DOCUMENT&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22City+of+Augusta%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Clark+Mountain%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22County+Abbreviations%22&fileAccess=CITATION&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Cultural+Resource+Survey%22&recordsPerPage=10&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Data+Recovery+%2F+Excavation%22&recordsPerPage=10%09&resourceTypes=ONTOLOGY");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Davis+(County)%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Dendroclimatic+Studies%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Depositional+Processes%22&fileAccess=CITATION&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Doug+McKay%22&resourceTypes=DOCUMENT&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Dry+Lake+QUAD%22&recordsPerPage=10&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Dye+Creek+Bridge%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22E.+FORK+STONES+RIVER%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22E-1763%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Early+Settlements%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Ejit%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Ethnohistoric+Overview%22&recordsPerPage=10&resourceTypes=PROJECT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Excavations+(Archaeology)%22&resourceTypes=DOCUMENT&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22F.+H.+Chapman%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Fabrics%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Facilities%22&recordsPerPage=10&resourceTypes=DOCUMENT&fileAccess=PARTIALLY_RESTRICTED");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Flaked+Stone+Artifacts%22&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Florida+Dahrm+Number+297%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Florida+Dahrm+Number+385%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Gainesville+Reservoir%22&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Hidatsa%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Historic+Aboriginal+Artifacts%22&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Historic+Site+Recorded%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Iirm%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22John+River%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Lewis+Berger+and+Associates%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Linguistic%22&fileAccess=CITATION&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Lithics+Analysis+%26+Bones%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Little+Lake%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Lower+Little+River%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Marlboro%22&fileAccess=CITATION&recordsPerPage=10&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Marsha+King%22&resourceTypes=DOCUMENT&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Megan+Hicks%22&recordsPerPage=20&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22MERBEN+ESTATES%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Mf+%23950%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Na2306%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22National+Park+Service%22&recordsPerPage=20&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Neptune%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22No+Clear+Area+Given%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22North+America+Grasslands%22&recordsPerPage=10&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Nowood+River+Drainage%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Paige+R.+Talley%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Pakistan%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Pioneer+period%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Poison+Creek+System%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Proposed+Archaeological+District%22&fileAccess=CITATION&documentType=CONFERENCE_PRESENTATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Regional+Project%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=OTHER");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Road+Surface%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22S.+F.+Anfinson%22&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Salado+Prehistory+%2F+Subsistence%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Seneca+(County)%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=IMAGE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Shovel+Test+Pits%22&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Siberia%22&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22site%3A+RO307%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Site+Description%22&resourceTypes=DATASET");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Southern+Appalachian+Mountains%22&fileAccess=CITATION&recordsPerPage=10&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Southern+IA+Drift+Plain%22&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Spanish+Fort%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22St.+Regis+Reservation%22&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Steven+Planning+Group%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22SW+Group%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Sweetwater+(County)%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Tafuna+Village%22&recordsPerPage=10&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Tano+Santa+Fe+Subdivision%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Temporally+Sensitive+Artifacts%22&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Thomas+L.+Struthers%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Trace+Elements%22&fileAccess=CITATION&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Univ.+of+Kansas.+Museum+of+Anthropology%2C+Lawrence%2C+KS%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22US+Forest+Service+Lincoln+NF-Cloudcroft+Ranger+District%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22USGS+Johannesburg+7.5'+quad%22&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22USGS+QUAD+White+Ledge+Peak+7.5'%22&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Venezuela%22&resourceTypes=DOCUMENT&recordsPerPage=10&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22Virginia+Goldstein%22&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&query=%22William+Maher%22&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&recordsPerPage=10&query=%22Shanty%22&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&recordsPerPage=10&siteNameKeywords=5MT4683&resourceTypes=DOCUMENT&documentType=BOOK&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&resourceTypes=DOCUMENT&query=%22Four+Bear%22&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&resourceTypes=DOCUMENT&query=%22UNESCO+World+Decade+for+Cultural+Development%22&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&searchProjects=true&query=&recordsPerPage=20&resourceTypes=DOCUMENT&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&uncontrolledCultureKeywords=Roman&fileAccess=PUBLICALLY_ACCESSIBLE&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&uncontrolledCultureKeywords=Russian-American+Period&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&uncontrolledCultureKeywords=Tehuacan&fileAccess=CITATION&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=0&uncontrolledCultureKeywords=Vollmers+Phase&resourceTypes=DOCUMENT&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=80&documentType=OTHER&query=%22Jeffery+F.+Burton%22&recordsPerPage=20");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?startRecord=80&query=%22Ronald+R.+Gould%22&recordsPerPage=20&documentType=THESIS");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Afro-Cruzan&startRecord=0&fileAccess=CITATION&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Andean&startRecord=0&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Archaic+-+Historic+Occupation&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Camp+Creek+Member&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Crane+Flat+%2F+Farmington+Complex&fileAccess=CITATION&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Eighteenth+Century&resourceTypes=DOCUMENT&startRecord=0&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Great+Bend+Aspect&startRecord=0&fileAccess=CITATION&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Hannibal+Complex&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Huari&startRecord=0&fileAccess=PUBLICALLY_ACCESSIBLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Kiokee+Creek&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Late+Laurentide&startRecord=0&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=JOURNAL_ARTICLE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=Paleo&startRecord=0&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=PLUM+BAYOU+CULTURE&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=0&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledCultureKeywords=TAENSA&startRecord=0&fileAccess=CITATION&resourceTypes=DOCUMENT&documentType=BOOK");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledSiteTypeKeywords=Brush+structure&resourceTypes=PROJECT&startRecord=0");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?uncontrolledSiteTypeKeywords=dam&startRecord=0&documentType=BOOK_SECTION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentT=&startRecord=182680&recordsPerPage=20");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2201023+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2201035+(Fips+Code)%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2220121+(Fips+Code)%22&fileAccess=CITATION&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2228001+(Fips+Code)%22&startRecord=29&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2228089+(Fips+Code)%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2236109+(Fips+Code)%22&fileAccess=CITATION&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%2253013+(Fips+Code)%22&fileAccess=CITATION&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Archaic+To+Late+Prehistoric%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Ceramic+Chronology%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Charles+E.+Pearson%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Charles+E.+Pearson%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Clearings%22&startRecord=80&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Corrals%22&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Gordon+Watts%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Janice+Artemel%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Rifles%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Sediment+Samples%22&startRecord=80&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22St.+Jones%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Stokes+(County)%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22SWCA%2C+Inc.+Environmental+Consultants%2C+Albuquerque%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Time+Sensitive+Artifacts%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Tool+Fragment%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22US+(ISO+Country+Code)%22&fileAccess=RESTRICTED&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22US+Forest+Service+Gila+National+Forest%22&fileAccess=CITATION&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22W.+Michael+Gear%22&fileAccess=CITATION&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&query=%22Wrangell+Island%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&siteNameKeywords=Chocolate+Plantation&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=10&query=%22Long+Canyon%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=10&query=%22Okwanuchu+%2F+Achomawi%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=120&query=%22Mitzi+Rossillon%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=20&query=%22Mineral+Creek%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=20&query=%22US+Army+COE%2C+St.+Paul+District%2C+St.+Paul%2C+MN%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=30&query=%22Cupules%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=50&query=%22Ceramic+Vessel%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=50&query=%22HOUSE+SITE%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=50&query=%22Mortars%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=60&query=%2254083+(Fips+Code)%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=60&query=%22Cupules%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=62170&query=&recordsPerPage=10&searchProjects=true");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK&startRecord=65620&query=&recordsPerPage=10&searchProjects=true");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK_SECTION&query=%22AAS%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK_SECTION&query=%22Glass%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK_SECTION&query=%22Skeletal+Material%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK_SECTION&query=%22SPACE%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=BOOK_SECTION&startRecord=140&query=%22US+(ISO+Country+Code)%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=CONFERENCE_PRESENTATION&query=%22Woodland%22&resourceTypes=DOCUMENT&startRecord=100&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&query=%22Nevada+(State+%2F+Territory)%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&query=%22Newaygo+(County)%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&query=%22North+Carolina+(State+%2F+Territory)%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&startRecord=0&query=%22Winnebago%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&startRecord=10&query=%22Wooden+Structures%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=JOURNAL_ARTICLE&startRecord=100&query=%22Architecture%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=OTHER&query=%22Greene+County%22&fileAccess=CITATION&startRecord=100&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=OTHER&query=%22Hamilton%22&startRecord=140&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=OTHER&query=%22Lebanon%22&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=OTHER&startRecord=50&query=%22C.+Adams%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?documentType=OTHER&startRecord=8260&searchProjects=true&query=&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?fileAccess=CITATION&startRecord=50&recordsPerPage=10&uncontrolledSiteTypeKeywords=Shell+midden");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=120&recordsPerPage=10&uncontrolledSiteTypeKeywords=Communal+%2F+public+structure");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?geographicKeywords=US%20(ISO%20Country%20Code)");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2201035+(Fips+Code)%22&fileAccess=CITATION&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2201129+(Fips+Code)%22&fileAccess=CITATION&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2219041+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2220133+(Fips+Code)%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2222083+(Fips+Code)%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2248469+(Fips+Code)%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2249021+(Fips+Code)%22&fileAccess=CITATION&startRecord=170&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22499-0+Ad%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2250015+(Fips+Code)%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%2255111+(Fips+Code)%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%2266+%2F+00+%2F+00%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Aboriginal+Village%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22AK+Department+of+Transportation%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Alger+(County)%22&resourceTypes=DOCUMENT&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Alluvial+Stratigraphy%22&startRecord=10&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22American+Resources+Group%2C+Ltd.+Carbondale%2C+IL%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ann+M.+Johnson%22&startRecord=120&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Antebellum%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Archaeological+and+Historical+Sites%22&fileAccess=CITATION&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Archaeological+Research+Services%2C+Inc.%22&fileAccess=CITATION&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Archeological+Impact+Assessment%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Archeometrics%22&resourceTypes=DOCUMENT&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Assignment%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Aztec+Ruins+National+Monument%2C+Aztec%2C+NM%22&startRecord=180&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Bartholomew+(County)%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Basketry%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=90&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22BATTLEGROUND%22&resourceTypes=DOCUMENT&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Brea+Canyon%22&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Burlington+Railroad%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22CA+(ISO+Country+Code)%22&fileAccess=CITATION&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Cadiz%22&startRecord=110&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Calhoun+City%22&resourceTypes=DOCUMENT&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Carmen+A.+Weber%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ceramic%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=200&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ceramics+Sand+Tempered%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Chipped+Stone%22&resourceTypes=SENSORY_DATA&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Church%22&startRecord=120&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22COE%22&startRecord=410&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22COE%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Comparative+Observations%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Construction+Project%22&startRecord=120&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Cooperative+Management%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22County+of+Imperial+Planning+Department%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22Dacw03-86-D-0068%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22DAVID+VOLKERT+AND+ASSOCIATES%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Del+Norte+(County)%22&startRecord=1240&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Department+of+Transportation%2C+District+10%22&resourceTypes=DOCUMENT&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Dept.+of+Anthro.+%2C+College+of+William+and+Mary%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22DRIFT+FENCE%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22E78+++++++MNO-NEG+++.W5+++++++1978%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22East+Union+Township%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ethnohistoric+Overview%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Eureka%2C+California%22&fileAccess=CITATION&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ex+Mission+DE+San+Fernando%22&resourceTypes=DOCUMENT&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Fauna%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22fire-cracked+rock%22&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Fireplace%22&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Gerald+H.+Clark%22&startRecord=29&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22GROUND+STONE%22&resourceTypes=SENSORY_DATA&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22H.+Bell%22&documentType=BOOK&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Harney+(County)%22&startRecord=190&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Hartford+(County)%22&startRecord=100&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Historic%22&startRecord=74800&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Historic+Coal+Mine%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Hohokam+Sedentary+period%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Huts%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Incised+Stone%22&fileAccess=CITATION&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Irvine%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22J.+Tainter%22&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Jefferson+(County)%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22John+Northcutt%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22John+Scarry%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Kathryn+E.+Pedrick%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Katm-002-82%22&startRecord=130&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Kelly+M.+Keim%22&fileAccess=CITATION&startRecord=80&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Kimball+(County)%22&fileAccess=CITATION&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22KIRKWOOD%22&startRecord=40&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Lathel+F.+Duffield%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22Laura+C.+Fulginiti%22");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22Lithic+Typology%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Log+Structure%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22M.+Lappen%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Magnolia+Plantation%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22May+14%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Maya+Prehistory%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Metal%22&resourceTypes=IMAGE&startRecord=790&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Mineral%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=1660&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Mineral%22&startRecord=150&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Mono+Craters+15'+quad%22&fileAccess=CITATION&resourceTypes=DOCUMENT&startRecord=80&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Neutron+Activation%22&startRecord=120&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22New+Hampshire+(State+%2F+Territory)%22&startRecord=100&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Noram%22&fileAccess=CITATION&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22North+New+Mexico%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Oca%22&resourceTypes=DOCUMENT&startRecord=60&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Oconee+National+Forest%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Orange+County%22&startRecord=140&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Penobscot%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Peoria%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Petro-Land+Services%2C+Unknown+Location%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Pike+and+San+Isabel+National+Forests%22&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Pike+Co%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Plaited+Sandal%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22PREHISTORIC+SHERD%22&fileAccess=PUBLICALLY_ACCESSIBLE&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Preservation%22&startRecord=40000&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Probable+Historic+Camp%22&resourceTypes=DOCUMENT&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Probable+Preform%22&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Pueblo+III%22&resourceTypes=DOCUMENT&startRecord=340&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Records+Search%22&startRecord=180&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Research+Objectives%22&startRecord=70&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Ripon-Escalon%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Robert+C.+Dunnell%22&resourceTypes=DOCUMENT&startRecord=0&recordsPerPage=20");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Rockshelter+Excavation%22&startRecord=90&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Roger+Green%22&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22Scarborough%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Scattered+Artifacts%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Seasonal+Campsite%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22SETTLEMENT+PATTERN+ANALYSIS%22&startRecord=160&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Social+Power%22&resourceTypes=DOCUMENT&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Society%22&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DATASET&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Southern+Alaska%22&startRecord=0&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22St.+Bernard+(County)%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Starr+(County)%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Stephen+Williams%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Taylor+Creek%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Thomas+L.+Jackson%22&resourceTypes=DOCUMENT&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Thomas+M.+Origer%22&startRecord=330&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Tonopah+Range%22&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22TRACE+ELEMENT+ANALYSIS%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22U.S.+Military%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22United+States+of+America+%28Country%29%22&startRecord=1010&recordsPerPage=10%09");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22United+States+of+America+%28Country%29%22&startRecord=970&recordsPerPage=10%09");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22US+(ISO+Country+Code)%22&startRecord=220&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22US+Army+COE%2C+Kansas+City+District%22&startRecord=140&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22USDA-Forest+Service+Daniel+Boone+National+Forest%22&fileAccess=CITATION&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Use+Wear+Analysis%22&startRecord=30&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22USGS+Guasti+7.5'+quad%22&startRecord=10&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22utility+line%22&startRecord=80&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Washburn+(County)%22&fileAccess=CITATION&startRecord=120&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Western+Apache%22&resourceTypes=DOCUMENT&startRecord=50&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?query=%22William+F.+Stanyard%22");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Willow+Creek%22&startRecord=270&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=%22Woodbury%22&startRecord=250&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?query=&startRecord=37180&recordsPerPage=20&searchProjects=true");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?resourceTypes=DOCUMENT&startRecord=110&recordsPerPage=10&uncontrolledSiteTypeKeywords=Midden");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?resourceTypes=DOCUMENT&startRecord=180&recordsPerPage=10&uncontrolledSiteTypeKeywords=Midden");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?siteNameKeywords=African+Burial+Ground&startRecord=20&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL + "?siteNameKeywords=Wellfleet+Tavern");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&documentType=BOOK&query=%22lightning%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&documentType=BOOK&uncontrolledCultureKeywords=Stanley&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&query=%22Federal+Highways+Administration%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&query=%22North+Alaska+Range+Early+Man+Project%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=0&query=%22University+of+Missouri%22&resourceTypes=PROJECT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&documentType=BOOK&query=%2201087+(Fips+Code)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&documentType=BOOK&query=%22C.+M.+Johnson%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&documentType=BOOK&query=%22Glen+W.+Lindeman%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&documentType=BOOK_SECTION&query=%22Monitoring%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&documentType=BOOK_SECTION&query=%22Western+South+Dakota%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%2201075+(Fips+Code)%22&fileAccess=CITATION&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%2235049+(Fips+Code)%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Cultural+Landscape+Report%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Hidatsa%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Joyce+McKay%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Petrology%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Reading%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Road+Improvement+Survey%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22USGS+Hesperia+7.5'+quad%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22Vail%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&query=%22X-Ray+Fluorescence%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=10&uncontrolledCultureKeywords=Archaic&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=100&documentType=BOOK&query=%22Shell%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=100&query=%22CH2M+HILL%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=110&documentType=BOOK&query=%22Macrobotanical%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=1190&query=%22Late+Chacoan+-+Post+Chacoan+Period%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=120&query=%22US+Bureau+of+Indian+Affairs+Navajo+Area+Office%22&fileAccess=CITATION&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=130&documentType=BOOK&query=%22US+Bureau+of+Indian+Affairs+Navajo+Area+Office%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=1370&uncontrolledSiteTypeKeywords=Town+%2F+city&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=1480&query=%22Pollen%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=150&query=%22Fire+Cracked+Rock%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=150&query=%22Protohistoric%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=1620&query=%22Collections+Research%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=170&fileAccess=PUBLICALLY_ACCESSIBLE&uncontrolledSiteTypeKeywords=Domestic+Structures&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=17570&query=%22Data+Recovery+%2F+Excavation%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=190&documentType=BOOK&query=%22Macrobotanical%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=190&query=%22Daggett+(County)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=190&uncontrolledCultureKeywords=Euroamerican&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=BOOK&query=%2251107+(Fips+Code)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=BOOK&query=%22B.+Barnette%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=BOOK&query=%22C.+M.+Johnson%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=BOOK&query=%22Miramar%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=BOOK&query=%22Valley+Center+7.5'+1968+QUAD%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&documentType=OTHER&query=%22tin-glazed%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&fileAccess=CITATION&uncontrolledSiteTypeKeywords=Domestic+Structures&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%2255075+(Fips+Code)%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22central+Arizona%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22J.+S.+Hayden%22&resourceTypes=DOCUMENT&recordsPerPage=20");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22Joyce+Bath%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22Metamorphic%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22Regional+Project%22&fileAccess=CITATION&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=20&query=%22Susan+Lindstrom%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=200&documentType=OTHER&query=%22Timothy+G.+Gross%22&recordsPerPage=20");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=2060&query=%22Fauna%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=210&query=%22Hawaii%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=210&query=%22Thomas+(County)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=260&query=%22Pollen%22&recordsPerPage=10&fileAccess=CITATION");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=270&recordsPerPage=10&fileAccess=CITATION&uncontrolledSiteTypeKeywords=Archaeological+Feature");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=2800&query=%22Ceramic%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=290&query=%22Pollen%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&documentType=BOOK&query=%22Missouri+Alluvial+Plains%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&documentType=BOOK&query=%22Railroad+Structure%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&documentType=BOOK&query=%22University+of+Maryland%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%2219197+(Fips+Code)%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Antrim%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Archaeological+Report+Summary%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Brc%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Broadwater+County%2C+Montana%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Hot+Creek%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Lewis+Binford%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Metal+Nails%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Penobscot+(County)%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=30&query=%22Ruth+C.+Alter%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=326160&documentType=BOOK&query=%22United+States+of+America+(Country)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=340&query=%22Thomas+(County)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=350&query=%22Black+Mountain%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=370&query=%22Hillsborough+(County)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=390&query=%22central+Arizona%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&documentType=BOOK&query=%2219147+(Fips+Code)%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&documentType=BOOK&query=%22Fort+Carson+Military+Reservation%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&documentType=BOOK&query=%22Mining+Ditch%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&query=%2211+Pages%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&query=%22Alfred+E.+Johnson%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=40&query=%22Archaeological+and+Historical+Consultants%2C+Inc.%22&fileAccess=CITATION&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=450&documentType=BOOK&query=%22Research+Design+%2F+Data+Recovery+Plan%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=480&query=%22fire-cracked+rock%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=50&documentType=BOOK&query=%22Tribal+Utility+Authority%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=50&fileAccess=CITATION&resourceTypes=DOCUMENT&query=%22Seasonality%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=50&query=%22Jewelry%22&fileAccess=PUBLICALLY_ACCESSIBLE&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=60&query=%2234007+(Fips+Code)%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=60&query=%22David+J.+Hally%22&fileAccess=CITATION&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=60&query=%22l9th+century%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=60&query=%22Nome%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=60&query=%22Ranches%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=6660&uncontrolledCultureKeywords=Archaic&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=670&recordsPerPage=10&uncontrolledSiteTypeKeywords=Funerary+and+Burial+Structures+or+Features%09%09%09");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&fileAccess=PUBLICALLY_ACCESSIBLE&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&query=%22Hamilton+Creek%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&query=%22MOUND+CONSTRUCTION%22&documentType=BOOK&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&query=%22Old+Fort%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&query=%22SITE+%2F+SETTLEMENT%22&recordsPerPage=10%09");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=70&query=%22Yampa+River+Basin%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=720&query=%22Douglas+%28County%29%22&recordsPerPage=10%09");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=7620&query=%22Collections+Research%22&recordsPerPage=10&resourceTypes=DOCUMENT");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=80&query=%22Csuc%22&fileAccess=CITATION&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=80&query=%22Pender+(County)%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=80&query=%22University+of+Alaska+Museum%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=90&query=%2230051+(Fips+Code)%22&resourceTypes=DOCUMENT&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=90&query=%22ARCHAEOLOGICAL+MONITORING+PLAN%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=90&uncontrolledSiteTypeKeywords=Post+hole+%2F+post+mold&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?startRecord=95780&query=%22Site+Evaluation+%2F+Testing%22&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?uncontrolledCultureKeywords=Late+Woodland&resourceTypes=DOCUMENT&startRecord=370&recordsPerPage=10");
        urls.add(SEARCH_RESULTS_BASE_URL
                + "?uncontrolledSiteTypeKeywords=Kiva+%2F+Great+Kiva");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=ontario&title=&id=&projectIds%5B0%5D=&resourceTypes=DATASET&coverageDates%5B0%5D.id=&coverageDates%5B0%5D.dateType=NONE&coverageDates%5B0%5D.startDate=&coverageDates%5B0%5D.endDate=&coverageDates%5B0%5D.description=&geographicKeywords%5B0%5D=&minx=&maxx=&miny=&maxy=&__multiselect_investigationTypeIds=&siteNameKeywords%5B0%5D=&__multiselect_approvedSiteTypeKeywordIds=&uncontrolledSiteTypeKeywords%5B0%5D=&__multiselect_materialKeywordIds=&__multiselect_approvedCultureKeywordIds=&uncontrolledCultureKeywords%5B0%5D=&temporalKeywords%5B0%5D=&otherKeywords%5B0%5D=&searchSubmitterIds%5B0%5D=&searchSubmitter.lastName=abc&searchSubmitter.firstName=&searchSubmitter.email=&searchSubmitter.institution.name=&searchContributorIds%5B0%5D=&searchContributor.lastName=&searchContributor.firstName=&searchContributor.email=&searchContributor.institution.name=&sortField=RELEVANCE");
        urls.add(SEARCH_RESULTS_BASE_URL
                + ".action?query=canada&title=&id=&projectIds%5B0%5D=&resourceTypes=DATASET&coverageDates%5B0%5D.id=&coverageDates%5B0%5D.dateType=NONE&coverageDates%5B0%5D.startDate=&coverageDates%5B0%5D.endDate=&coverageDates%5B0%5D.description=&geographicKeywords%5B0%5D=&minx=&maxx=&miny=&maxy=&__multiselect_investigationTypeIds=&siteNameKeywords%5B0%5D=&__multiselect_approvedSiteTypeKeywordIds=&uncontrolledSiteTypeKeywords%5B0%5D=&__multiselect_materialKeywordIds=&__multiselect_approvedCultureKeywordIds=&uncontrolledCultureKeywords%5B0%5D=&temporalKeywords%5B0%5D=&otherKeywords%5B0%5D=&searchSubmitterIds%5B0%5D=&searchSubmitter.lastName=abc&searchSubmitter.firstName=&searchSubmitter.email=&searchSubmitter.institution.name=&searchContributorIds%5B0%5D=&searchContributor.lastName=&searchContributor.firstName=adam&searchContributor.email=&searchContributor.institution.name=&sortField=RELEVANCE");

        // this url simulates a "modify search" request with a "gap" in the terms; a gap is created, for example, if you create three terms then delete
        // the middle term.
        urls.add("/search/advanced?__multiselect_groups%5B0%5D.approvedSiteTypeIdLists%5B0%5D=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLatitude=&groups%5B0%5D.operator=AND&groups%5B0%5D.fieldTypes%5B2%5D=KEYWORD_CULTURAL&sortField=RELEVANCE&__multiselect_groups%5B0%5D.approvedCultureKeywordIdLists%5B1%5D=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLongitude=&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.minimumLongitude=&groups%5B0%5D.approvedSiteTypeIdLists%5B0%5D=272&groups%5B0%5D.latitudeLongitudeBoxes%5B0%5D.maximumLatitude=&groups%5B0%5D.fieldTypes%5B0%5D=KEYWORD_SITE&groups%5B0%5D.approvedCultureKeywordIdLists%5B1%5D=4");

        for (String url : urls) {
            gotoPage(url);
            assertNoErrorTextPresent();
        }
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
        logger.debug(getPageText());
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
        clickLinkWithText("Modify Search");
        HtmlCheckBoxInput element = (HtmlCheckBoxInput) htmlPage.getElementById("groups[0].approvedSiteTypeIdLists[0]-1");
        assertNotNull("could not find element", element);
        assertTrue("checkbox isn't checked", element.isChecked());
    }
}
