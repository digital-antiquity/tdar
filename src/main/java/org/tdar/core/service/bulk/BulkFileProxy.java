package org.tdar.core.service.bulk;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import org.tdar.utils.activity.Activity;

/**
 * Helper class for Bulk Upload to track both the File and its stream
 * 
 * @author abrin
 * 
 */
public class BulkFileProxy implements Serializable {

    private static final long serialVersionUID = 3145958895116193973L;

    private File file;

    private InputStream stream;

    private Activity activity;

    public BulkFileProxy(File manfiest, Activity activity) {
        this.file = manfiest;
        this.setActivity(activity);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
