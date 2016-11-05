package org.tdar.struts.action.search;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.struts.action.AbstractIntegrationControllerTestCase;
import org.tdar.struts.action.api.lookup.CollectionLookupAction;
import org.tdar.struts.action.api.lookup.InstitutionLookupAction;
import org.tdar.struts.action.api.lookup.KeywordLookupAction;
import org.tdar.struts.action.api.lookup.PersonLookupAction;
import org.tdar.struts.action.api.lookup.ResourceAnnotationKeyLookupAction;
import org.tdar.struts.action.api.lookup.ResourceLookupAction;

public class LookupControllerITCase extends AbstractIntegrationControllerTestCase {

    private static final String L_BL_AW = "l[]bl aw\\";
    private ResourceLookupAction controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(ResourceLookupAction.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback
    // special characters need to be escaped or stripped prior to search
    public void testLookupWithSpecialCharactors() throws SolrServerException, IOException {
        controller.setTerm(L_BL_AW);
        controller.setMinLookupLength(0);
        controller.lookupResource();
        PersonLookupAction pcontroller = generateNewInitializedController(PersonLookupAction.class);
        pcontroller.setTerm(L_BL_AW);
        pcontroller.setFirstName(L_BL_AW);
        pcontroller.setLastName(L_BL_AW);
        pcontroller.setEmail(L_BL_AW);
        pcontroller.setInstitution(L_BL_AW);
        pcontroller.lookupPerson();
        InstitutionLookupAction icontroller = generateNewInitializedController(InstitutionLookupAction.class);
        icontroller.setInstitution(L_BL_AW);
        icontroller.lookupInstitution();
        KeywordLookupAction kcontroller = generateNewInitializedController(KeywordLookupAction.class);
        kcontroller.setKeywordType("TemporalKeyword");
        kcontroller.setTerm(L_BL_AW);
        kcontroller.lookupKeyword();
        CollectionLookupAction ccontroller = generateNewInitializedController(CollectionLookupAction.class);
        controller.setTerm(L_BL_AW);
        ccontroller.lookupResourceCollection();
        ResourceAnnotationKeyLookupAction rcontroller = generateNewController(ResourceAnnotationKeyLookupAction.class);
        rcontroller.setTerm(L_BL_AW);
        rcontroller.lookupAnnotationKey();
    }
}
