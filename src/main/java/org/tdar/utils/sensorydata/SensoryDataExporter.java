package org.tdar.utils.sensorydata;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.tdar.core.bean.resource.SensoryData;

import com.thoughtworks.xstream.XStream;

public class SensoryDataExporter {

    XStream xstream = new XStream();
    public SensoryDataExporter() {
        xstream.processAnnotations(SensoryData.class);
    }
    
    public void export(SensoryData sensoryData, PrintWriter pw) {
        String xml = xstream.toXML(sensoryData);
        pw.println(xml);
    }
    
    
}
