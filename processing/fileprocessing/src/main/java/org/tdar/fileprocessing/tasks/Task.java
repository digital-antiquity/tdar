/**
 * 
 */
package org.tdar.fileprocessing.tasks;

import java.io.Serializable;

import org.tdar.fileprocessing.workflows.Workflow;
import org.tdar.fileprocessing.workflows.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
public interface Task extends Serializable {

    /*
     * Pass in the generic context
     */
    void setWorkflowContext(WorkflowContext ctx);

    WorkflowContext getWorkflowContext();

    /**
     * Run method called by the task work flow process. The implicit contract is that if the task exits abnormally, it will throw an exception during
     * the execution of this method.
     * Also, the exceptions are not necessarily logged: so if you want a record of the problem, log error before throwing the
     * exception.
     * 
     * @see WorkflowContext#setErrorFatal(boolean)
     * @see Workflow#run(WorkflowContext)
     */
    void run() throws Exception;

    /*
     * setup method
     */
    void prepare();

    /*
     * shutdown method
     */
    void cleanup();

    /*
     * get the Name of the task (used for logging) etc.
     */
    String getName();
}
