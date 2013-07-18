package org.tdar.filestore.personal;

import java.io.File;

public class PersonalFilestoreFile {

    private File file;
    private String md5;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String toString() {
        return "[PersonalFilestoreFile file:" + file + " md5:" + md5 + "]";
    }

}
