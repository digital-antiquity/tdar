package org.tdar.core.service.workflow.workflows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.LoggingTask;
import org.tdar.filestore.tasks.Task;

public abstract class BaseWorkflow implements Workflow {

    private Map<String, List<String>> requiredExtensions = new HashMap<>();
    private Map<String, List<String>> suggestedExtensions = new HashMap<>();

    private Map<WorkflowPhase, List<Class<? extends Task>>> workflowPhaseToTasks = new HashMap<>();
    // this appears to be a folded version of resourceTypeToExtensions.values() ?
    private Set<String> extensions = new HashSet<>();
    private Map<ResourceType, Set<String>> resourceTypeToExtensions = new HashMap<>();
    // were we expecting that these Workflows would be serializable?
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String extension;
    
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
    public Set<String> getValidExtensions() {
        return extensions;
    }

    @Override
    public Set<String> getValidExtensionsForResourceType(ResourceType type) {
        Set<String> validExtensions = resourceTypeToExtensions.get(type);
        if (validExtensions == null) {
            return Collections.emptySet();
        }
        return validExtensions;
    }

    @Override
    public boolean canProcess(String extension) {
        return !extensions.isEmpty() && extensions.contains(extension.toLowerCase());
    }

    @Override
    public void registerFileExtension(String fileExtension, ResourceType... resourceTypes) {
        if ((resourceTypes == null) || (resourceTypes.length == 0)) {
            logger.warn("Trying to register a null resource type with file extension: {}", fileExtension);
            return;
        }
        for (ResourceType type : resourceTypes) {
            Set<String> validExtensions = resourceTypeToExtensions.get(type);
            if (validExtensions == null) {
                validExtensions = new HashSet<>();
                resourceTypeToExtensions.put(type, validExtensions);
            }
            validExtensions.add(fileExtension);
        }
        extensions.add(fileExtension);
    }

    public void registerFileExtensions(String[] fileExtensions, ResourceType... resourceTypes) {
        if ((resourceTypes == null) || (resourceTypes.length == 0) || (fileExtensions == null) || (fileExtensions.length == 0)) {
            logger.warn("invalid file extensions {} or resource types {}", getAsList(fileExtensions), getAsList(resourceTypes));
            return;
        }
        for (String fileExtension : fileExtensions) {
            registerFileExtension(fileExtension, resourceTypes);
        }
    }

    /**
     * A utility method to return the argument in a list in a NPE safe way
     * 
     * @param targetArray
     * @return
     */
    @SuppressWarnings("static-method")
    private <T> List<T> getAsList(T[] targetArray) {
        if (targetArray == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(targetArray);
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void initializeWorkflowContext(WorkflowContext ctx, FileStoreFileProxy[] version) {
        return;
    }

    @Override
    public Map<String, List<String>> getSuggestedExtensions() {
        return suggestedExtensions;
    }

    public void setSuggestedExtensions(Map<String, List<String>> suggestedExtensions) {
        this.suggestedExtensions = suggestedExtensions;
    }

    @Override
    public Map<String, List<String>> getRequiredExtensions() {
        return requiredExtensions;
    }

    public void setRequiredExtensions(Map<String, List<String>> requiredExtensions) {
        this.requiredExtensions = requiredExtensions;
    }

}