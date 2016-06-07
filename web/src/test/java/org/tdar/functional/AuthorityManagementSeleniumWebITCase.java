package org.tdar.functional;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.By;
import org.tdar.core.bean.DedupeableType;

public class AuthorityManagementSeleniumWebITCase extends AbstractAdminSeleniumWebITCase {

    @Test
    public void testInstitutionAuthorityManagement() {
        gotoPage("/admin/authority");
        find(By.id("selEntityType")).val(DedupeableType.PERSON.name());
        find(By.id("selEntityType")).val(DedupeableType.INSTITUTION.name());
        find(By.id("txtInstitution")).sendKeys("City of Alexandria");
        waitFor(By.id("cbEntityId_11153"));
        find(By.id("cbEntityId_11153")).click();
        waitFor(By.id("cbEntityId_11154"));
        find(By.id("cbEntityId_11154")).click();
        submitForm();

        find(By.id("rdoAuthorityId-11154")).click();
        submitForm();
        assertTrue("page contains 'congrats'", getText().contains("Congratulations"));

        gotoPage("/browse/creators/11153");
        assertTrue("page contains Duplicate", getText().contains("Duplicate"));
        find(By.linkText("City of Alexandria, Virginia")).click();
        find(By.linkText("City of Alexandria")).click();
    }
}
