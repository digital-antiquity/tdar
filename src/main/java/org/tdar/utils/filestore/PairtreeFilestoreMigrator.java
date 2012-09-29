package org.tdar.utils.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.WorkflowContextService;
import org.tdar.filestore.FilesystemFilestore;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.TaintedFileException;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ImageThumbnailTask;
import org.tdar.filestore.tasks.LoggingTask;
import org.tdar.filestore.tasks.PDFDerivativeTask;
import org.tdar.filestore.tasks.Task;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * $Id$
 * 
 * This is a utility meant to be run one time to move content from the old
 * file storage scheme to the new one. There is a lot of code duplication
 * between this and the services which deal with the filestore.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class PairtreeFilestoreMigrator {

    public static final Logger log = Logger.getLogger(PairtreeFilestoreMigrator.class);
    private final CSVWriter errors;
    private FilesystemFilestore oldStore;
    private PairtreeFilestore newStore;
    private ClassPathXmlApplicationContext context;
    private Session session;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings
    public PairtreeFilestoreMigrator(String originalFilestorePath, String newFilestorePath, Writer errorWriter) {
        this.errors = new CSVWriter(errorWriter);
        this.oldStore = new FilesystemFilestore(originalFilestorePath);
        this.newStore = new PairtreeFilestore(newFilestorePath);

        String springContextFile = "applicationContext.xml";
        File f = new File(springContextFile);
        if (!f.exists()) {
            f = new File("src/main/resources/" + springContextFile);
        }
        if (!f.exists()) {
            log.fatal("Could not find applicationContext.xml to configure the migrator");

            System.exit(0);
        }
        context = new ClassPathXmlApplicationContext(springContextFile);
        // SessionFactoryService sessionService = context.getBean(SessionFactoryService.class);
        // session = sessionService.getSessionFactory().openSession();
        initErrorWriter();
    }

    @SuppressWarnings("unchecked")
    public void migrate() throws IOException {
        try {
            List<InformationResource> resources = session.createCriteria(InformationResource.class).addOrder(Order.desc("id")).list();
            log.debug("Got some resources. There are " + resources.size() + " of them.");
            for (InformationResource resource : resources) {
                Transaction transaction = session.beginTransaction();
                moveFile(resource);
                transaction.commit();
            }
        } finally {
            cleanup();
        }
    }

    private void moveFile(InformationResource resource) {
        for (InformationResourceFile irFile : resource.getInformationResourceFiles()) {
            for (InformationResourceFileVersion version : irFile.getInformationResourceFileVersions()) {
                String oldPath = "";
                if (version.isDerivative() || StringUtils.isEmpty(version.getFilestoreId())) {
                    log.trace("Skipping: " + version);
                    continue;
                }
                log.info("Processing id#:" + version.getId());
                try {
                    oldStore.verifyFile(version);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (TaintedFileException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    File f = oldStore.retrieveFile(version);
                    oldPath = f.getAbsolutePath();
                    log.trace("MOVING: " + f.getAbsolutePath() + "");
                    File newFile = new File(newStore.store(f, version));
                    String newPath = newFile.getParent();
                    WorkflowContext ctx = new WorkflowContext();
                    ctx.setOriginalFile(version);
                    ctx.setFilestore(newStore);
                    ctx.setInformationResourceId(resource.getId());
                    ctx.setInformationResourceFileId(irFile.getId());
                    ctx.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
                    Task t = null;
                    log.info(irFile.getInformationResourceFileType());
                    switch (irFile.getInformationResourceFileType()) {
                        case DOCUMENT:
                            t = new PDFDerivativeTask();
                            break;
                        case IMAGE:
                            t = new ImageThumbnailTask();
                            break;
                    }
                    if (t != null) {
                        t.setWorkflowContext(ctx);
                        t.prepare();
                        t.run(newFile);
                        t.cleanup();
                        Task log = new LoggingTask();
                        log.setWorkflowContext(ctx);
                        log.prepare();
                        log.run(newFile);
                        log.cleanup();
                    }

                    WorkflowContextService wcs = context.getBean(WorkflowContextService.class);
                    wcs.processContext(ctx);

                    log.info("\tTO: " + newPath + "");
                } catch (FileNotFoundException fnf) {
                    logError(resource.getId().toString(), version.getId().toString(), version.getFilestoreId(), version.getFilename(), oldPath,
                            "source file not found");
                } catch (TaintedFileException fnf) {
                    logError(resource.getId().toString(), version.getId().toString(), version.getFilestoreId(), version.getFilename(), oldPath,
                            "source file does not have matching digest");
                } catch (Exception e) {
                    logError(resource.getId().toString(), version.getId().toString(), version.getFilestoreId(), version.getFilename(), oldPath, e.getMessage() +
                            e.toString());
                    log.debug(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void initErrorWriter() {
        String[] errorHeader = { "resource_id", "version_id", "filestore_id", "filename", "newpath", "error" };
        errors.writeNext(errorHeader);
    }

    private void logError(String resourceId, String versionId, String filestoreId, String filename, String path, String error) {
        String[] err = { resourceId, versionId, filestoreId, filename, path, error };
        errors.writeNext(err);
        try {
            errors.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void cleanup() throws IOException {
        errors.close();
    }

    public static void printUsage(String error) {
        String usage = "USAGE: \n$> FilestoreMigrator originalFilestorePath newFilestorePath errorFilePath";
        if (StringUtils.isNotBlank(error))
            usage += "\n ERROR: " + error;
        System.out.println(usage);
    }

    public static void main(String[] args) {

        if (args.length < 3) {
            printUsage("Please supply all required arguments.");
            return;
        }

        try {
            PairtreeFilestoreMigrator migrator = new PairtreeFilestoreMigrator(args[0], args[1], new FileWriter(args[2]));
            migrator.migrate();
        } catch (IOException e) {
            printUsage("Could not successfully migrate." + e.getMessage());
        }

    }

}
