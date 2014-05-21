/**
 * 
 */
package org.tdar.web;

import org.junit.Before;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractAnonymousWebTestCase extends AbstractWebTestCase {

    @Before
    public void setUp() {
        gotoPage("/");
    }

}
