package org.tdar.utils.pdf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionRemoteGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;

public class PDFCombiner {
    private Logger logger = Logger.getLogger(getClass());
    private Integer pagePointer = 1;
    private HashMap<String, Integer> documentPageMap = new HashMap<String, Integer>();
    private PDFMergerUtility merger = new PDFMergerUtility();

    private String[] includePath = { "VOLUME1" };

    public boolean contains(String[] paths, String key) {
        for (String path : paths) {
            if (key.contains(path))
                return true;
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String f = "/Users/Adam Brin/Desktop/VERDECD/VOLUME1/1-01.PDF";
        PDFCombiner combiner = new PDFCombiner();
        System.setProperty("java.awt.headless", "true");
        try {
            combiner.scanAndCombine(f, true,true);
            combiner.combine(FilenameUtils.getBaseName(f) + "NEWPDF.PDF");
            combiner.scanAndCombine(FilenameUtils.getBaseName(f) + "NEWPDF.PDF", false,true);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void combine(String newFileName) throws COSVisitorException, IOException {
        logger.info("calling combine on " + newFileName);
        merger.setDestinationFileName(newFileName);
        merger.mergeDocuments();
    }

    @SuppressWarnings("unchecked")
    public void scanAndCombine(String file, boolean combine,boolean first) throws IOException, COSVisitorException {
        PDDocument doc = PDDocument.load(file);
        String filePath = FilenameUtils.getFullPath(file);
        File currentPath = new File(filePath);
        try {
            if (combine && first) {
                merger.addSource(FilenameUtils.concat(currentPath.getAbsolutePath(), file.toUpperCase()));
            }
            List<PDPage> allPages = doc.getDocumentCatalog().getAllPages();
            this.pagePointer = allPages.size() + pagePointer;

            // for every page
            for (int i = 0; i < allPages.size(); i++) {
                PDPage page = (PDPage) allPages.get(i);
                List<PDAnnotation> annotations = page.getAnnotations();
                logger.info("page:" + i + " annotations: " + annotations.size());

                // grab all of the annotations
                for (int j = 0; j < annotations.size(); j++) {
                    PDAnnotation annot = (PDAnnotation) annotations.get(j);
                    logger.trace(annot.getContents() + " " + annot.getSubtype());

                    // if the annotation is a link & the link is a "remote" link
                    if (annot.getSubtype().equalsIgnoreCase("Link")) {
                        PDAnnotationLink link = (PDAnnotationLink) annot;
                        PDAction action = link.getAction();
                        logger.trace(action);
                        if (action instanceof PDActionRemoteGoTo) {
                            PDActionRemoteGoTo fileLink = (PDActionRemoteGoTo) action;
                            PDFileSpecification remoteFileInfo = fileLink.getFile();
                            // grab the actual path
                            String remoteFileName = FilenameUtils.concat(currentPath.getAbsolutePath(), remoteFileInfo.getFile().toUpperCase());
                            File remoteFile = new File(remoteFileName);

                            // if we've not seen it before then append the file to the end of the PDF
                            if (!documentPageMap.containsKey(remoteFileName)) {
                                if (remoteFile.exists() && contains(includePath, remoteFileName)) {
                                    logger.info("adding pointer to: " + remoteFileName + " (" + pagePointer + ")");
                                    if (combine) {
                                        merger.addSource(remoteFileName);
                                    }
                                    documentPageMap.put(remoteFileName, new Integer(pagePointer));
                                    scanAndCombine(remoteFileName, true,false);
                                } else {
                                    logger.warn(remoteFileName + " DOES NOT EXIST");
                                }
                            } else {
                                // if we're not combining, then re-write the link to be at the right page
                                if (combine) {
                                    logger.debug("already seen: " + remoteFileName);
                                } else {
                                    PDActionGoTo newAction = new PDActionGoTo();
                                    PDPageFitDestination destination = new PDPageFitDestination();
                                    destination.setPageNumber(documentPageMap.get(remoteFileName));
                                    destination.setPage(allPages.get(documentPageMap.get(remoteFileName)));
                                    newAction.setDestination(destination);
                                    link.setAction(newAction);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (doc != null) {
                if (!combine) {
                    doc.save(file);
                }
                doc.close();
            }
        }
    }

}
