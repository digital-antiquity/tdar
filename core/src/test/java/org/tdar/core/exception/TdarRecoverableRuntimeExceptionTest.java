package org.tdar.core.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TdarRecoverableRuntimeExceptionTest {

    @Test
    // assert that recoverable errors maintain 'friendliness'
    public void testMessage() {
        String msg = "This is a message";
        TdarRecoverableRuntimeException ex = new TdarRecoverableRuntimeException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    // ensure message isn't munged when wrapping in another exception
    public void testWrappedMessage() {
        String msg = "This is a message";
        RuntimeException rtx = new RuntimeException(msg);
        TdarRecoverableRuntimeException ex = new TdarRecoverableRuntimeException(rtx.getMessage(), rtx);
        assertEquals(msg, ex.getMessage());
    }

}
