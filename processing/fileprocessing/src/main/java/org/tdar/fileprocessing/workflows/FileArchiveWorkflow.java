package org.tdar.fileprocessing.workflows;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;
import org.tdar.fileprocessing.tasks.IndexableTextExtractionTask;
import org.tdar.fileprocessing.tasks.ListArchiveTask;
import org.tdar.filestore.FileType;

/**
 * $Id$
 *
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class FileArchiveWorkflow extends BaseWorkflow {

    public static final Collection<String> ARCHIVE_EXTENSIONS_SUPPORTED = Arrays.asList(new String[] { "zip", "tar", "bz2", "tgz" });

    public FileArchiveWorkflow() {
        addRequired(FileArchiveWorkflow.class, Arrays.asList("zip", "tar", "bz2", "tgz"));
        addTask(ListArchiveTask.class, WorkflowPhase.PRE_PROCESS);
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.FILE_ARCHIVE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
