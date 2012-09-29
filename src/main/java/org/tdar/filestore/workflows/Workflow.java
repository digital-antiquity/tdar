package org.tdar.filestore.workflows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.LoggingTask;
import org.tdar.filestore.tasks.Task;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public interface Workflow {

    public boolean run() throws Exception;

    public WorkflowContext getWorkflowContext();

    public void setWorkflowContext(WorkflowContext workflowContext);

    public Set<String> getValidExtensions();

    public void addTask(Task task, WorkflowPhase phase);

    public boolean canProcess(String ext);

    public FileType getInformationResourceFileType();

    public boolean isEnabled();

    public void registerFileExtension(String ext, ResourceType... types);

    public Set<String> getValidExtensionsForResourceType(ResourceType type);

    public abstract static class BaseWorkflow implements Workflow {
        private WorkflowContext workflowContext;
        private Map<WorkflowPhase, List<Task>> workflowPhaseToTasks = new HashMap<WorkflowPhase, List<Task>>();
        // this appears to be a folded version of resourceTypeToExtensions.values() ?
        private Set<String> extensions = new HashSet<String>();
        private Map<ResourceType, Set<String>> resourceTypeToExtensions = new HashMap<ResourceType, Set<String>>();
        // were we expecting that these Workflows would be serializable?    
        private final Logger logger = LoggerFactory.getLogger(getClass());
        
        public BaseWorkflow() {
            addTask(new LoggingTask(), WorkflowPhase.CLEANUP);
        }

        public boolean isEnabled() {
            return true;
        }

        public boolean run() throws Exception {
            boolean successful = true;
            // this may be more complex than it needs to be, but it may be useful in debugging later; or organizationally.
            // by default tasks are processed in the order they are added within the phase that they're part of.
            for (WorkflowPhase phase : WorkflowPhase.values()) {
                List<Task> phaseTasks = workflowPhaseToTasks.get(phase);
                if (CollectionUtils.isEmpty(phaseTasks)) {
                    continue;
                }
                for (Task task : phaseTasks) {
                    logger.info("{} - {}", phase.name(), task.getName());
                    StringBuilder message = new StringBuilder();
                    try {
                        task.setWorkflowContext(getWorkflowContext());
                        task.prepare();
                        task.run();
                    } catch (Throwable e) {
                        message.append("Task Failed.");
                        successful = false;
                        throw new TdarRecoverableRuntimeException(e.getMessage(), e);
                    } finally {
                        workflowContext.logTask(task, message);
                        task.cleanup();
                    }
                }
            }
            return successful;
        }

        public WorkflowContext getWorkflowContext() {
            return workflowContext;
        }

        public void addTask(Task task, WorkflowPhase phase) {
            List<Task> taskList = workflowPhaseToTasks.get(phase);
            if (taskList == null) {
                taskList = new ArrayList<Task>();
                workflowPhaseToTasks.put(phase, taskList);
            }
            taskList.add(task);
        }

        public void setWorkflowContext(WorkflowContext workflowContext) {
            this.workflowContext = workflowContext;
        }

        public Set<String> getValidExtensions() {
            return extensions;
        }

        public Set<String> getValidExtensionsForResourceType(ResourceType type) {
            Set<String> validExtensions = resourceTypeToExtensions.get(type);
            if (validExtensions == null) {
                return Collections.emptySet();
            }
            return validExtensions;
        }

        public boolean canProcess(String extension) {
            return !extensions.isEmpty() && extensions.contains(extension.toLowerCase());
        }

        public void registerFileExtension(String fileExtension, ResourceType... resourceTypes) {
            if (resourceTypes == null || resourceTypes.length == 0) {
                logger.warn("Trying to register a null resource type with file extension: {}", fileExtension);
                return;
            }
            for (ResourceType type : resourceTypes) {
                Set<String> validExtensions = resourceTypeToExtensions.get(type);
                if (validExtensions == null) {
                    validExtensions = new HashSet<String>();
                    resourceTypeToExtensions.put(type, validExtensions);
                }
                validExtensions.add(fileExtension);
            }
            extensions.add(fileExtension);
        }

        public void registerFileExtensions(String[] fileExtensions, ResourceType... resourceTypes) {
            if (resourceTypes == null || resourceTypes.length == 0 || fileExtensions == null || fileExtensions.length == 0) {
                logger.warn("invalid file extensions {} or resource types {}", Arrays.asList(fileExtensions), Arrays.asList(resourceTypes));
                return;
            }
            for (String fileExtension : fileExtensions) {
                registerFileExtension(fileExtension, resourceTypes);
            }
        }

        public Logger getLogger() {
            return logger;
        }

    }

}
