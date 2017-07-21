package org.tdar.core.bean;

import org.apache.commons.lang.StringUtils;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;

public interface TestEntityHelper {

    default TdarUser createAndSaveNewPerson() {
        return createAndSaveNewPerson(null, "");
    }

    default TdarUser createAndSaveNewPerson(String email_, String suffix) {
        String email = email_;
        if (StringUtils.isBlank(email)) {
            email = TestConstants.DEFAULT_EMAIL;
        }
        TdarUser testPerson = new TdarUser();
        testPerson.setEmail(email);
        testPerson.setFirstName(TestConstants.DEFAULT_FIRST_NAME + suffix);
        testPerson.setLastName(TestConstants.DEFAULT_LAST_NAME + suffix);
        testPerson.setUsername(email);
        Institution institution = getEntityService().findInstitutionByName(TestConstants.INSTITUTION_NAME);
        if (institution == null) {
            institution = new Institution();
            institution.setName(TestConstants.INSTITUTION_NAME);
            getGenericService().saveOrUpdate(institution);
        }
        testPerson.setInstitution(institution);
        testPerson.setContributor(false);
        getGenericService().save(testPerson);
        return testPerson;
    }
    
    GenericService getGenericService();
    
    EntityService getEntityService();

}
