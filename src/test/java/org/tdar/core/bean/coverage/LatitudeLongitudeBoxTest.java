package org.tdar.core.bean.coverage;

import static org.junit.Assert.*;

import org.junit.Test;

public class LatitudeLongitudeBoxTest {

    /**
     * Should always be true under current implementation.
     */
    @Test
    public void testIsActuallyObfuscated() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0));
        assertTrue(llb.isActuallyObfuscated());
    }

}
