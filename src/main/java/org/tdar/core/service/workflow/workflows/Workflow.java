package org.tdar.core.service.workflow.workflows;

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
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.LoggingTask;
import org.tdar.filestore.tasks.Task;
import org.tdar.struts.data.FileProxy;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public interface Workflow {

    public boolean run(WorkflowContext workflowContext) throws Exception;

    public Set<String> getValidExtensions();

    public void addTask(Class<? extends Task> task, WorkflowPhase phase);

    public boolean canProcess(String ext);

    public FileType getInformationResourceFileType();

    public boolean isEnabled();

    public void registerFileExtension(String ext, ResourceType... types);

    public Set<String> getValidExtensionsForResourceType(ResourceType type);

    public abstract static class BaseWorkflow implements Workflow {

        private Map<String,List<String>> requiredExtensions = new HashMap<>();
        private Map<String,List<String>> suggestedExtensions = new HashMap<>();

        private Map<WorkflowPhase, List<Class<? extends Task>>> workflowPhaseToTasks = new HashMap<WorkflowPhase, List<Class<? extends Task>>>();
        // this appears to be a folded version of resourceTypeToExtensions.values() ?
        private Set<String> extensions = new HashSet<String>();
        private Map<ResourceType, Set<String>> resourceTypeToExtensions = new HashMap<ResourceType, Set<String>>();
        // were we expecting that these Workflows would be serializable?
        private final Logger logger = LoggerFactory.getLogger(getClass());

        public BaseWorkflow() {
            addTask(LoggingTask.class, WorkflowPhase.CLEANUP);
        }

        public boolean isEnabled() {
            return true;
        }

        public boolean run(WorkflowContext workflowContext) throws Exception {
            boolean successful = true;
            // this may be more complex than it needs to be, but it may be useful in debugging later; or organizationally.
            // by default tasks are processed in the order they are added within the phase that they're part of.
            for (WorkflowPhase phase : WorkflowPhase.values()) {
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
                    }
                }
            }
            workflowContext.setProcessedSuccessfully(successful);
            return successful;
        }

        public void addTask(Class<? extends Task> task, WorkflowPhase phase) {
            List<Class<? extends Task>> taskList = workflowPhaseToTasks.get(phase);
            if (taskList == null) {
                taskList = new ArrayList<Class<? extends Task>>();
                workflowPhaseToTasks.put(phase, taskList);
            }
            taskList.add(task);
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

        public void initializeWorkflowContext(InformationResourceFileVersion version, WorkflowContext ctx) {
            return;
        }

        
        public boolean validateProxyCollection(FileProxy primary) {
            return true;
        }

        public Map<String,List<String>> getSuggestedExtensions() {
            return suggestedExtensions;
        }

        public void setSuggestedExtensions(Map<String,List<String>> suggestedExtensions) {
            this.suggestedExtensions = suggestedExtensions;
        }

        public Map<String,List<String>> getRequiredExtensions() {
            return requiredExtensions;
        }

        public void setRequiredExtensions(Map<String,List<String>> requiredExtensions) {
            this.requiredExtensions = requiredExtensions;
        }

    }

    public void initializeWorkflowContext(InformationResourceFileVersion version, WorkflowContext ctx);

    public boolean validateProxyCollection(FileProxy primary);

}
