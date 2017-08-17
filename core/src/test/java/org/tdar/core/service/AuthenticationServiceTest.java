package org.tdar.core.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthenticationServiceImpl;

/**
 * Created by jim on 8/19/15.
 *
 * email address tests courtesy of:
 *  - https://en.wikipedia.org/wiki/Email_address#Valid_email_addresses
 *  - http://codefool.tumblr.com/post/15288874550/list-of-valid-and-invalid-email-addresses
 */
public class AuthenticationServiceTest {

    AuthenticationService service = new AuthenticationServiceImpl();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> readLines(String filename) {
        Path path = new File("src/test/resources/validation", filename).toPath();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(path, Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("bad filename", e);
        }
        return lines;
    }

    @Test
    public void testUsernameLengths() {
        assertThat(service.isValidUsername("b"),  is (false));
        assertThat(service.isValidUsername("bo"),  is (false));
        assertThat(service.isValidUsername("bob"),  is (false));
        assertThat(service.isValidUsername("bobb"),  is (false));
        assertThat(service.isValidUsername("bobby"),  is (true));
    }

    @Test
    public void testPossibleUsernameLengths() {
        assertThat(service.isPossibleValidUsername("b"),  is (false));
        assertThat(service.isPossibleValidUsername("bo"),  is (true));
        assertThat(service.isPossibleValidUsername("bob"),  is (true));
        assertThat(service.isPossibleValidUsername("bobb"),  is (true));
        assertThat(service.isPossibleValidUsername("bobby"),  is (true));
        assertThat(service.isPossibleValidUsername("jim"), is(true));
    }

    @Test
    public void testUsernameCharacters() {
        assertThat(service.isValidUsername("bôbby"), is(false));
        assertThat(service.isValidUsername(" bobby"), is(false));
        assertThat(service.isValidUsername("bobby "), is(false));
        assertThat(service.isValidUsername("bobby tables"), is(false));
    }

    @Test
    public void testPossibleUsernameCharacters() {
        assertThat(service.isPossibleValidUsername("bôbby"), is(false));
        assertThat(service.isPossibleValidUsername(" bobby"), is(false));
        assertThat(service.isPossibleValidUsername("bobby "), is(false));
        assertThat(service.isPossibleValidUsername("bobby tables"), is(true));
    }

    @Ignore @Test
    public void testValidEmailAddresses() {
        for(String email : readLines("email-addresses-valid.txt")) {
            String message = String.format("email address should be considered valid: %s", email);
            assertThat(message, service.isValidEmail(email), is(true));
        }
    }

    @Ignore @Test
    public void testUnusualEmailAddresses() {
        for(String email : readLines("email-addresses-valid-unusual.txt")) {
            String message = String.format("email address should be valid: %s", email);
            assertThat(message, service.isValidEmail(email), is(true));
        }
    }

    @Ignore @Test
    public void testInvalidEmailAddresses() {
        for(String email : readLines("email-addresses-invalid.txt")) {
            String message = String.format("email address should be invalid: %s", email);
            assertThat(message, service.isValidEmail(email), is(false));
        }
    }

}
