package org.tdar.struts.data;

import java.io.Serializable;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

public class SvgMapWrapper implements Serializable {

    private static final long serialVersionUID = -724628710412117107L;
    private int minX;
    private int minY;
    private int width;
    private int height;
    private List<SQLXML> sqlXml = new ArrayList<SQLXML>();

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<SQLXML> getSqlXml() {
        return sqlXml;
    }

    public void setSqlXml(List<SQLXML> sqlXml) {
        this.sqlXml = sqlXml;
    }
}
