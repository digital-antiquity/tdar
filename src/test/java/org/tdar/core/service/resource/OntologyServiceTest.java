package org.tdar.core.service.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test case for OntologyService.
 * 
 * FIXME: Currently only exercises string manipulation functions. Test inputs
 * can be improved.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public class OntologyServiceTest {

    @Autowired
    private OntologyService ontologyService;

    private Logger logger = Logger.getLogger(getClass());

    @Before
    public void setUp() {
        ontologyService = new OntologyService();
        // no set up
    }

    @Test
    public void testSuggestions() {
        String target = "eel";
        String[] badMatches = { "meal", "AVES", "Alaudidae", "Corvidae_(Corvid)", "Corvus_corax_(Raven)", "large_corvid", "small_corvid", "Motacillidae",
                "Turdidae", "Goose_(Goose)", "Ardeidae", "Columbidae_(Columbid)", "Cuculidae", "Gaviidae", "Gruidae", "Laridae_(Gull)",
                "Gallus_gallus_or_Numida_meleagris_(Chicken_or_Guinea_fowl)",
                "Gallus_gallus_or_Numida_meleagris_or_Phasianus_colchicus_(Chicken_or_Guinea_fowl_or_Pheasant)", "Rallidae", "Crex_crex_(Corncrake)",
                "Alcidae", "Alle_alle_(Little_auk)", "Sulidae", "MAMMALIA", "Carnivora", "Mustela_(Mustelid)", "Meles_meles_(Badger)", "Cetacea",
                "Equidae_(Equid)", "Insectivora", "Medium_mammal_(vertebra_and_ribs)_(pig_and_fallow_deer-sized)", "Murinae_(small)" };
        for (String badMatch : badMatches) {
            assertFalse(badMatch + " should not be similar enough to " + target, ontologyService.isSimilarEnough(target, ontologyService.normalize(badMatch)));
        }
        String[] goodMatches = { "eel", "peel", "wheel", "Happy meel", "electric eel", "unhappy meel", "even keeled", "well heeled", "eelectric light orchestra" };
        for (String goodMatch: goodMatches) {
            assertTrue(goodMatch + " should be similar enough to " + target, ontologyService.isSimilarEnough(target, ontologyService.normalize(goodMatch)));
        }
        
        // added from TDAR-626 and TDAR-642
        assertFalse(ontologyService.isSimilarEnough(ontologyService.normalize("Ilium"), ontologyService.normalize("Distal")));
        assertFalse(ontologyService.isSimilarEnough(ontologyService.normalize("Ischium"), ontologyService.normalize("Shaft")));
        assertFalse(ontologyService.isSimilarEnough(ontologyService.normalize("Ischium"), ontologyService.normalize("Distal")));
        assertTrue(ontologyService.isSimilarEnough("cattle", ontologyService.normalize("Cattle")));
    }

    @Test
    public void testInvalidCharactersRegex() {
        // FIXME: construct exhaustive list of invalid characters dynamically?
        String[] hasInvalidCharacters = { "@!@#", "!!", "^^\t", "*", "&", "^", "$$" };
        for (String invalidCharacterString : hasInvalidCharacters) {
            Assert.assertTrue(invalidCharacterString + " has invalid characters", invalidCharacterString.matches(OntologyService.IRI_INVALID_CHARACTERS_REGEX));
        }
    }

    @Test
    public void testSanitizeOntologyLabel() {
        String[] ontologyLabels = { "   \t\t  Some string wit~h-.%A90F (parentheses in the mix)", "\tAnother (string (with (parentheses)))\t" };
        for (String label : ontologyLabels) {
            String sanitizedLabel = ontologyService.labelToFragmentId(label);
            logger.info("sanitized label: " + sanitizedLabel);
            Assert.assertFalse(sanitizedLabel.matches(OntologyService.IRI_INVALID_CHARACTERS_REGEX));
        }
    }

    @Test
    public void testTrim() {
        String s = "\t\t   what  \t\t ";
        Assert.assertEquals("what", s.trim());
    }

    @Test
    public void testGetDepth() {
        for (int i = 0; i < 64; i++) {
            String repeatedTabs = StringUtils.repeat("\t", i);
            String tabInput = repeatedTabs + " test \t input" + repeatedTabs + " decrescendo boogers\t";
            Assert.assertEquals(i, ontologyService.getNumberOfPrefixTabs(tabInput));
        }
    }

    @Test
    public void testValidTextToOwlXml() {
        String ontologyTextInput = "Parent\n\tFirst Child\n\t\tFirst Child's Child1\n\t\tFirst Child's Second Child\n\t\tFirst Child's Child\n"
                + "\tSecond Child\n" + "\tThird Child\n\t\tThird Child's Child\n\t\tThird Child's Child2\n"
                + "\tFourth Child\n\t\tFourth Child's Child\n\t\tFourth Child's Nondegenerate Child\n"
                + "Second Root Parent\n\tSecond Root Parent's Degenerate Child ";
        String owlXml = ontologyService.toOwlXml(237L, ontologyTextInput);
        // FIXME: make assertions on the generated OWL XML.
        assertNotNull(owlXml);
    }

    @Test
    public void testDegenerateTextToOwlXml() {
        String ontologyTextInput = "Parent\n\tFirst Child\n\t\tFirst Child's Child\n\t\tFirst Child's Second Child\n\t\tFirst Child's Child\n"
                + "\tSecond Child\n" + "\tThird Child\n\t\tThird Child's Child\n\t\tThird Child's Child\n"
                + "\tFourth Child\n\t\t\t\tFourth Child's Degenerate Child\n\t\tFourth Child's Nondegenerate Child\n"
                + "Second Root Parent\n\t\tSecond Root Parent's Degenerate Child ";

        try {
            logger.info(ontologyService.toOwlXml(238L, ontologyTextInput));
            fail("Should raise an java.lang.IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException successException) {
        }
    }
}
