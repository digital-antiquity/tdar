package org.tdar.web;

import org.junit.Before;
import org.junit.Test;
import org.tdar.configuration.TdarConfiguration;

/**
 * We set the following four properties in the open search template:
 * ${siteAcronym}
 * ${siteName}
 * ${contactEmail}
 * ${hostName}
 * 
 * @author Martin Paulo
 */
public class OpenSearchWebITCase extends AbstractWebTestCase {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Before
    public void moveToSearchPage() {
        gotoPage("/opensearch.xml");
    }

    /**
     * We can only test against the property file that has been loaded into the jetty instance on startup. So any property not set in the file
     * will be the default value in the code.
     */
    @Test
    public void testPropertiesAreReadIntoTemplate() {
        gotoPage("/opensearch.xml");
        assertTextPresent(shortNameElementText());
        assertTextPresent(descriptionElementText());
        assertTextPresent(contactElementText());
        assertTextPresent(searchUrlElementText());
        assertTextPresent(atomUrlElementText());
    }

    private String contactElementText() {
        return asXml("Contact", CONFIG.getContactEmail());
    }

    private String descriptionElementText() {
        return asXml("Description", "Search " + CONFIG.getSiteName());
    }

    private String shortNameElementText() {
        return asXml("ShortName", CONFIG.getSiteAcronym());
    }

    private String searchUrlElementText() {
        return "http://" + CONFIG.getHostName();
    }

    private String atomUrlElementText() {
        return "<Url type=\"application/atom+xml\" template=\"http://" + CONFIG.getHostName();
    }

    private String asXml(final String tagname, final String value) {
        return "<" + tagname + ">" + value + "</" + tagname + ">";
    }
}
