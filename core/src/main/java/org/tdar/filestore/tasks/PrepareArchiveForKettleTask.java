package org.tdar.filestore.tasks;

import static java.lang.System.lineSeparator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.Task.AbstractTask;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This file will check to see if the parent archive has been marked for extraction, and if so, make a copy for an external ETL tool (Kettle) to
 * work with, then write a control file that will give Kettle the information it needs to perform its run.
 * 
 * The archive is then marked as having been extracted (to stop future attempts to extract the file).
 * 
 * At some later date Kettle will pick up the control file, use it to locate the copy, then unzip and re-import the contents as part of the parent
 * project.
 * 
 * Brittle? Yes. But it does allow the contents of the tar ball to be varied independently of the tDAR source code, and also for transformations
 * to be created by people who aren't familiar with Java. Swings and round-a-bouts, I guess.
 * 
 * @author Martin Paulo
 */
public class PrepareArchiveForKettleTask extends AbstractTask {

    private static final long serialVersionUID = 4807507811943064504L;

    private static final String FILE_NAME = "file_name";
    private static final String PROJECT_ID = "project_id";
    private static final String UPDATED_BY_EMAIL = "updated_by_email";

    private static final String NEW_TARBALL_TEMPLATE_KEY = "new_tarball";
    // in time the template/templates can be moved to a file
    private static final String XML_TEMPLATE_NEW_TARBALL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() +
            "<run_settings>" + lineSeparator() +
            "    <file_name>${" + FILE_NAME + "}</file_name>" + lineSeparator() +
            "    <project_id>${" + PROJECT_ID + "?c}</project_id>" + lineSeparator() +
            "    <updated_by>${" + UPDATED_BY_EMAIL + "}</updated_by>" + lineSeparator() +
            "</run_settings>";

    private String kettleInputPath = TdarConfiguration.getInstance().getKettleInputPath();
    private File controlFileOuputDir;
    private File archiveCopiesDir;

    /**
     * @param kettleInputPath
     *            the control file output directory: allows us to override the one read from the property file.
     */
    protected void setKettleInputPathOverride(String kettleInputPath) {
        this.kettleInputPath = kettleInputPath;
    }

    private static boolean isDirectoryWritable(File file) {
        return file.exists() && file.isDirectory() && file.canWrite();
    }

    private static String getLogMessage(String message, Archive archive) {
        return String.format(message + " (Title: %s Id: %s) ", archive.getTitle(), archive.getId());
    }

    private static Template loadFreemarkerTemplate() throws IOException {
        StringTemplateLoader loader = new StringTemplateLoader();
        loader.putTemplate(NEW_TARBALL_TEMPLATE_KEY, XML_TEMPLATE_NEW_TARBALL);
        @SuppressWarnings("deprecation")
        Configuration configuration = new Configuration();
        configuration.setTemplateLoader(loader);
        return configuration.getTemplate(NEW_TARBALL_TEMPLATE_KEY);
    }

    @Override
    public String getName() {
        return "extract faims tarball task";
    }

    /**
     * (non-Javadoc)
     * <p>
     * Preconditions:
     * <ul>
     * <li>We need a project id, an email address, and a file.
     * <li>We need to know the directory that we are writing control files to.
     * <li>That directory needs to be able to writable
     * <li>We need to be able to read the original Archive resource.
     * <li>That original archive resource should tell us if this task is to be run, or already has been run.
     * <li>We need to be able to access the temp directory to make copies of files.
     * </ul>
     * <p>
     * Postconditions:
     * <ul>
     * <li>There will be a control file written to disk for Kettle to pick up
     * <li>There will be a copy made of the archive file for Kettle to work with
     * </ul>
     * 
     * @see org.tdar.filestore.tasks.Task#run()
     */
    @Override
    public void run() throws Exception {
        final WorkflowContext ctx = getWorkflowContext();
        // it's quite hard to pick a start of this run out of the log files, so we'll log a title for the time being
        getLogger().warn("<============= Starting run of task to extract tarball =============>");

        // first off, a whole raft of preconditions that we need to pass before we write the control file:
        // reality check: do we have an archive?
        if (!ctx.getResourceType().isArchive()) {
            return;
        }

        // if we can't get the archive, we don't have enough information to run...
        Archive archive = (Archive) ctx.getTransientResource();
        if (archive == null) {
            recordErrorAndExit("Transient copy of archive not available...");
        }

        // are we to import the archive's content?
        if (!archive.isDoImportContent()) {
            getLogger().info(getLogMessage("Archive is set to ignore import.", archive));
            return;
        }

        // we don't want to import the tar ball twice!
        if (archive.isImportDone()) {
            getLogger().info(getLogMessage("Archive has already been imported.", archive));
            return;
        }

        // has the archive been assigned to a project?
        if ((archive.getProjectId() == null) || (archive.getProjectId() <= 0)) {
            recordErrorAndExit("Cannot unpack an archive that has not yet been assigned to a project!");
        }

        controlFileOuputDir = new File(kettleInputPath);
        if (!isDirectoryWritable(controlFileOuputDir)) {
            recordErrorAndExit("Can not write to kettle input directory: " + controlFileOuputDir.getCanonicalPath());
        }

        // do we have a directory to write our copies to?
        archiveCopiesDir = ctx.getWorkingDirectory();
        if (!isDirectoryWritable(archiveCopiesDir)) {
            recordErrorAndExit("Can not write to directory for file output: " + archiveCopiesDir);
        }

        // are there actual files to copy?
        final List<InformationResourceFileVersion> archiveFiles = ctx.getOriginalFiles();
        if (archiveFiles.size() <= 0) {
            recordErrorAndExit("Must have an archive file to work with");
        }

        // Preconditions have been checked, now to write the control file and set up the copy of the archive to work with.
        // at the moment there should be only one of these files: however, that should only be an artifact of the user interface.
        for (InformationResourceFileVersion version : archiveFiles) {
            File copyOfTarball = makeCopyOfSourceFile(version);
            if (!copyOfTarball.exists()) {
                recordErrorAndExit("Copy of file for archive extract not found! Expected: " + copyOfTarball.getAbsolutePath());
            }
            writeKettleControlFileToDisk(archive, copyOfTarball);
        }
        // We hope that the save of the changes to the archive will happen on the other side of the work flow...
        archive.setImportDone(true);
        archive.setDoImportContent(false);
    }

    private void writeKettleControlFileToDisk(Archive archive, File copy) throws IOException, TemplateException {
        Template template = loadFreemarkerTemplate();
        Map<String, Object> values = new HashMap<>();
        values.put(FILE_NAME, StringEscapeUtils.escapeXml11(copy.getAbsolutePath()));
        values.put(PROJECT_ID, archive.getProjectId());
        values.put(UPDATED_BY_EMAIL, StringEscapeUtils.escapeXml11(getEmailToNotify(archive)));
        try (Writer output = new FileWriter(getNewRunControlFile())) {
            template.process(values, output);
        }
    }

    /**
     * @param archive
     *            that is being extracted
     * @return The email address to notify about the extraction of the archive. If it is null or empty, then the administrator is notified.
     */
    protected String getEmailToNotify(Archive archive) {
        String result = null;

        // First try the person who submitted the archive
        if (archive.getSubmitter() != null) {
            result = archive.getSubmitter().getEmail();
        }

        // Then the person who updated it
        if (StringUtils.isEmpty(result)) {
            if (archive.getUpdatedBy() != null) {
                result = archive.getUpdatedBy().getEmail();
            }
        }

        // Then the person who uploaded it
        if (StringUtils.isEmpty(result)) {
            if (archive.getUploader() != null) {
                result = archive.getUploader().getEmail();
            }
        }

        // Finally the administrator.
        if (StringUtils.isEmpty(result)) {
            // this should never be null, hopefully...
            result = TdarConfiguration.getInstance().getSystemAdminEmail();
        }
        if (StringUtils.isEmpty(result)) {
            recordErrorAndExit("Could not find an email address to notify of archive import!");
        }
        return result;
    }

    private File getNewRunControlFile() {
        int i = 1;
        File result;
        do {
            result = generateControlFileName(i++);
        } while (result.exists());
        getLogger().warn("=> writing control file to: " + result.getAbsolutePath());
        return result;
    }

    private File generateControlFileName(int i) {
        return new File(controlFileOuputDir, String.format("run_%d.xml", i));
    }

    private File makeCopyOfSourceFile(InformationResourceFileVersion version) throws IOException {
        File originalFile = version.getTransientFile();
        File workingDir = new File(archiveCopiesDir, "kettle_input");
        getLogger().warn("=> about to extract the contents of: " + originalFile.getName() + " to: " + workingDir.getAbsolutePath());
        FileUtils.copyFileToDirectory(originalFile, workingDir);
        return new File(workingDir, originalFile.getName());
    }

}
