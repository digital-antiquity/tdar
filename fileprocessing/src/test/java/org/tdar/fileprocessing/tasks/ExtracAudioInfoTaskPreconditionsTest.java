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
package org.tdar.fileprocessing.tasks;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.fileprocessing.tasks.ExtractAudioInfoTask;
import org.tdar.fileprocessing.workflows.WorkflowContext;

/**
 * A test to check that the preconditions work: although other classes are used, it's not an integration test, as no database or
 * file input/output is performed. It simply checks the preconditions...
 * 
 * @author Martin Paulo
 */
public class ExtracAudioInfoTaskPreconditionsTest {

    private ExtractAudioInfoTask task;

    @Before
    public void setUp() throws Exception {
        task = new ExtractAudioInfoTask();
        WorkflowContext ctx = new WorkflowContext();
        task.setWorkflowContext(ctx);
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
