package org.tdar.web;

import org.junit.Test;

public class EntityEditingWebITCase extends AbstractEditorAuthenticatedWebTestCase {

    private static final String UNIVERSITY_OF_TEST = "University of TEST";
    private static final String ENTITY_VIEW = "/browse/creators/";
    private static final String ENTITY_PERSON_EDIT = "/entity/person/%s/edit";
    private static final String ENTITY_USER_EDIT = "/entity/user/%s/edit";
    private static final String ENTITY_INSTITUTION_EDIT = "/entity/institution/%s/edit";

    @Test
    public void testEntityEdit() {
        String url = String.format(ENTITY_PERSON_EDIT, 8424);
        gotoPage(url);
        assertTextPresent("Editing:");
        assertTextPresent("Shackel");
        String phone = "(123) 456-7890 ";
        setInput("person.phone", phone);
        submitForm();
        assertTextPresent("Shackel");
        assertTextPresent(phone);
        logout();
        assertTextNotPresent(phone);
    }

    @Test
    public void testUserEdit() {
        String url = String.format(ENTITY_USER_EDIT, 1);
        gotoPage(url);
        assertTextPresent("Editing:");
        assertTextPresent("Lee");
        String phone = "(123) 456-7890 ";
        setInput("person.phone", phone);
        submitForm();
        assertTextPresent("Lee");
        assertTextPresent(phone);
        logout();
        assertTextNotPresent(phone);
    }

    @Test
    public void testInstitutionEdit() {
        String url = String.format(ENTITY_INSTITUTION_EDIT, 12088);
        gotoPage(url);
        assertTextPresent(UNIVERSITY_OF_TEST);
        String newUrl = "http://www.test.com";
        setInput("institution.description", newUrl);
        submitForm();
        assertTextPresent(UNIVERSITY_OF_TEST);
        assertTextPresent(newUrl);
        logout();
        assertTextNotPresent(newUrl);
    }

    @Test
    public void testInstitutionEditKeyIssue() {
        String url = String.format(ENTITY_INSTITUTION_EDIT, 12517);
        gotoPage(url);
        setInput("name", UNIVERSITY_OF_TEST);
        submitFormWithoutErrorCheck();
        // assertTextNotPresent(UNIVERSITY_OF_TEST);
        assertTextPresent("Cannot rename institution");
    }

    @Test
    public void testInstitutionEditYourInstitution() {
        String url = String.format(ENTITY_INSTITUTION_EDIT, 12088);
        gotoPage(url);
        assertTextPresent(UNIVERSITY_OF_TEST);
        String newName = "University of anotehr test";
        setInput("name", newName);
        submitForm();
        assertTextNotPresent(UNIVERSITY_OF_TEST);
        assertTextPresent(newName);

        gotoPage(url);
        assertTextPresent(newName);
        setInput("name", UNIVERSITY_OF_TEST);
        submitForm();
        assertTextPresent(UNIVERSITY_OF_TEST);
        assertTextNotPresent(newName);
    }

    @Test
    public void testInstitutionEditChangeName() {
        String url = String.format(ENTITY_INSTITUTION_EDIT, 12517);
        gotoPage(url);
        assertTextPresent("Unknown");
        String newName = "anotheruniversity";
        setInput("name", newName);
        submitForm();
        assertTextPresent(newName);
    }

}
