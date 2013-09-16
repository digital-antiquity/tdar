/*
 * Copyright (C) 2013  VeRSI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tdar.filestore.tasks;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.resource.Audio;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;

/**
 * A test to check that the preconditions work: although other classes are used, it's not an integration test, as no database or
 * file input/output is performed. It simply checks the preconditions...
 * 
 * @author Martin Paulo
 */
public class ExtracAudioInfoTaskPreconditionsTest {

    private ExtractAudioInfoTask task;
    private Audio audio;

    @Before
    public void setUp() throws Exception {
        task = new ExtractAudioInfoTask();
        audio = new Audio();
        WorkflowContext ctx = new WorkflowContext();
        ctx.setResourceType(ResourceType.AUDIO);
        ctx.setTransientResource(audio);
        task.setWorkflowContext(ctx);
    }

    @Test
    public void mustBeAudioResourceType() {
        task.getWorkflowContext().setResourceType(ResourceType.DOCUMENT);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("The Extract Audio Info Task has been called for a non audio resource!"));
        }
    }

    @Test
    public void mustHaveNonNullAudioFile() {
        task.getWorkflowContext().setTransientResource(null);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Transient copy of audio not available..."));
        }
    }

    @Test
    public void mustHaveAFileToWorkWith() {
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Must have an audio file to work with"));
        }
    }

}
