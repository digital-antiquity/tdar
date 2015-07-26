package org.tdar.web.resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.tdar.TestConstants;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class UploadITWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    @Test
    public void testSendFile() throws Exception {
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TestConstants.TEST_DOCUMENT);
    }

    @Test
    // Should return error json w/ error http code if no ticket in request
    public void sendFileWithNoTicket() {
        int status = uploadFileToPersonalFilestoreWithoutErrorCheck("", TestConstants.TEST_DOCUMENT);
        Assert.assertNotSame("expecting an error code because we sent bogus ticket", HttpStatus.OK.value(), status);
    }

    @Test
    // server should return an error if no file in request
    public void sendRequesWithNoFile() {
        String ticketId = getPersonalFilestoreTicketId();
        int status = uploadFileToPersonalFilestoreWithoutErrorCheck(ticketId, null);
        Assert.assertNotSame("expecting an error code because we sent bogus ticket", HttpStatus.OK.value(), status);

    }
}
