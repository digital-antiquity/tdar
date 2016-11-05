package org.tdar.utils.dropbox;

public interface DropboxConstants {
    public static final String CREATE_PDFA = "Create PDFA";
    final String CLIENT_DATA = "/Client Data/";
    final String INPUT = "input";
    final String OUTPUT = "output";
    final String UPLOAD_TO_TDAR = "Upload to tDAR";
    final String DONE_OCR = CREATE_PDFA + "/"+OUTPUT+"/";
    final String TO_PDFA = CREATE_PDFA + "/"+INPUT+"/";
    final String UPLOAD_PATH = CLIENT_DATA + UPLOAD_TO_TDAR;
    final String DONE_PDFA_PATH = CLIENT_DATA + DONE_OCR;
    final String TO_PDFA_PATH = CLIENT_DATA + TO_PDFA;
    final String COMBINE_PDF_DIR = "Combine Folder and PDFA";
}
