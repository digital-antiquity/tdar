package org.tdar.core.service.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.resource.ontology.OntologyNodeSuggestionGenerator;
import org.tdar.core.service.resource.ontology.OwlOntologyConverter;

/**
 * Test case for OntologyService.
 * 
 * FIXME: Currently only exercises string manipulation functions. Test inputs
 * can be improved.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public class OntologyServiceTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testSuggestions() {
        OntologyNodeSuggestionGenerator generator = new OntologyNodeSuggestionGenerator();
        String target = "eel";
        String[] badMatches = { "meal", "AVES", "Alaudidae", "Corvidae_(Corvid)", "Corvus_corax_(Raven)", "large_corvid", "small_corvid", "Motacillidae",
                "Turdidae", "Goose_(Goose)", "Ardeidae", "Columbidae_(Columbid)", "Cuculidae", "Gaviidae", "Gruidae", "Laridae_(Gull)",
                "Gallus_gallus_or_Numida_meleagris_(Chicken_or_Guinea_fowl)",
                "Gallus_gallus_or_Numida_meleagris_or_Phasianus_colchicus_(Chicken_or_Guinea_fowl_or_Pheasant)", "Rallidae", "Crex_crex_(Corncrake)",
                "Alcidae", "Alle_alle_(Little_auk)", "Sulidae", "MAMMALIA", "Carnivora", "Mustela_(Mustelid)", "Meles_meles_(Badger)", "Cetacea",
                "Equidae_(Equid)", "Insectivora", "Medium_mammal_(vertebra_and_ribs)_(pig_and_fallow_deer-sized)", "Murinae_(small)" };
        for (String badMatch : badMatches) {
            assertFalse(badMatch + " should not be similar enough to " + target, generator.isSimilarEnough(target, generator.normalize(badMatch)));
        }
        String[] goodMatches = { "eel", "peel", "wheel", "Happy meel", "electric eel", "unhappy meel", "even keeled", "well heeled",
                "eelectric light orchestra" };
        for (String goodMatch : goodMatches) {
            assertTrue(goodMatch + " should be similar enough to " + target, generator.isSimilarEnough(target, generator.normalize(goodMatch)));
        }

        // added from TDAR-626 and TDAR-642
        assertFalse(generator.isSimilarEnough(generator.normalize("Ilium"), generator.normalize("Distal")));
        assertFalse(generator.isSimilarEnough(generator.normalize("Ischium"), generator.normalize("Shaft")));
        assertFalse(generator.isSimilarEnough(generator.normalize("Ischium"), generator.normalize("Distal")));
        assertTrue(generator.isSimilarEnough("cattle", generator.normalize("Cattle")));
    }

    @Test
    public void testInvalidCharactersRegex() {
        // FIXME: construct exhaustive list of invalid characters dynamically?
        String[] hasInvalidCharacters = { "@!@#", "!!", "^^\t", "*", "&", "^", "$$", "(ab)", "this is a % test" };
        for (String invalidCharacterString : hasInvalidCharacters) {
            Assert.assertNotSame(invalidCharacterString + " has invalid characters", invalidCharacterString,
                    OwlOntologyConverter.labelToFragmentId(invalidCharacterString));
        }
    }

    @Test
    public void testSanitizeOntologyLabel() {
        String[] ontologyLabels = { "   \t\t  Some string wit~h-.%A90F (parentheses in the mix)", "\tAnother (string (with (parentheses)))\t" };
        for (String label : ontologyLabels) {
            String sanitizedLabel = OwlOntologyConverter.labelToFragmentId(label);
            logger.info("sanitized label: " + sanitizedLabel);
            Assert.assertFalse(sanitizedLabel.matches(OwlOntologyConverter.IRI_INVALID_CHARACTERS_REGEX));
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
            Assert.assertEquals(i, OwlOntologyConverter.getNumberOfPrefixTabs(tabInput));
        }
    }

}
