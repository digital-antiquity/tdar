package org.tdar.fileprocessing.workflows;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.fileprocessing.tasks.LoggingTask;
import org.tdar.fileprocessing.tasks.Task;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FilestoreObjectType;

public abstract class BaseWorkflow implements Workflow {

    private Map<WorkflowPhase, List<Class<? extends Task>>> workflowPhaseToTasks = new HashMap<>();
    // this appears to be a folded version of resourceTypeToExtensions.values() ?
    // were we expecting that these Workflows would be serializable?
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String extension;
    private Set<RequiredOptionalPairs> requiredOptionalPairs = new HashSet<>();
    
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public BaseWorkflow() {
        addTask(LoggingTask.class, WorkflowPhase.CLEANUP);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    
    @Override
    public boolean canProcess(String extension) {
        if (getAnyRequiredExtensions().contains(extension)) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean run(WorkflowContext workflowContext) throws Exception {
        boolean successful = true;
        // this may be more complex than it needs to be, but it may be useful in debugging later; or organizationally.
        // by default tasks are processed in the order they are added within the phase that they're part of.

        try {
            for (FileStoreFile version : workflowContext.getOriginalFiles()) {
                version.setTransientFile(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            }
        } catch (Exception e) {
            workflowContext.addException(e);
            workflowContext.setErrorFatal(true);
            return false;
        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // ensuring proper sorting
        EnumSet<WorkflowPhase> phases = EnumSet.allOf(WorkflowPhase.class);
        for (WorkflowPhase phase : phases) {
            List<Class<? extends Task>> phaseTasks = workflowPhaseToTasks.get(phase);
            if (CollectionUtils.isEmpty(phaseTasks)) {
                continue;
            }
            for (Class<? extends Task> taskClass : phaseTasks) {
                Task task = taskClass.newInstance();
                logger.info("{} - {}", phase.name(), task.getName());
                StringBuilder message = new StringBuilder();
                try {
                    task.setWorkflowContext(workflowContext);
                    task.prepare();
                    task.run();
                } catch (Throwable e) {
                    message.append("Task Failed.");
                    workflowContext.addException(e);
                    successful = false;
                    continue;
                } finally {
                    workflowContext.logTask(task, message);
                    task.cleanup();
                    Thread.yield();
                }
            }
        }
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

        workflowContext.setProcessedSuccessfully(successful);
        return successful;
    }

    @Override
    public void addTask(Class<? extends Task> task, WorkflowPhase phase) {
        List<Class<? extends Task>> taskList = workflowPhaseToTasks.get(phase);
        if (taskList == null) {
            taskList = new ArrayList<>();
            workflowPhaseToTasks.put(phase, taskList);
        }
        taskList.add(task);
    }

    @Override
    public void initializeWorkflowContext(WorkflowContext ctx, FileStoreFileProxy[] version) {
        return;
    }
    
    protected void addRequired(Class<? extends Workflow> cls, List<String> asList) {
        for (String ext : asList) {
            RequiredOptionalPairs rop = new RequiredOptionalPairs(cls);
            rop.getRequired().add(ext);
            getRequiredOptionalPairs().add(rop);
        }
    }

    
    
    
    public Logger getLogger() {
        return logger;
    }

    public Set<RequiredOptionalPairs> getRequiredOptionalPairs() {
        return requiredOptionalPairs;
    }

    public List<String> getAnyRequiredExtensions() {
        List<String> extensions = new ArrayList<>();
        for (RequiredOptionalPairs pair : requiredOptionalPairs) {
            extensions.addAll(pair.getRequired());
        }
        return extensions;
    }
 
    public List<String> getAllValidExtensions() {
        List<String> extensions = new ArrayList<>();
        for (RequiredOptionalPairs pair : requiredOptionalPairs) {
            extensions.addAll(pair.getRequired());
            extensions.addAll(pair.getOptional());
        }
        return extensions;
    }
 
}