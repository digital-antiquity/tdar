package org.tdar.core.service.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelWorkbookWriter;

/**
 * A sheet proxy holds context needed to construct one or more sheets; it's goal is to attempt to hide Excel / POI from whatever is writing to it
 * 
 * @author jimdevos
 * @param <T>
 * @param <T>
 * 
 */
public class SheetProxy implements Serializable {

    private static final long serialVersionUID = -8358849369052680733L;
    private String name;
    private transient Workbook workbook;
    private List<String> headerLabels = new ArrayList<String>();
    private int startRow = ExcelWorkbookWriter.FIRST_ROW;
    private int startCol = ExcelWorkbookWriter.FIRST_COLUMN;
    private String noteRow;
    private Iterator<Object[]> data;

    // private static Callback NO_OP = new Callback() {public void apply(Workbook ignored){}};

    private Callback preCallback = null;
    private Callback postCallback = null;
    private int freezeRow;

    // cleanup needed after rendering a sheet?
    private boolean isCleanupNeeded = true;
    private boolean autosizeCols;

    // optional work that a caller can perform before/after exporter processes its data.
    public static abstract class Callback {
        public abstract void apply(Workbook workbook);
    }

    public SheetProxy() {
        this(ExcelWorkbookWriter.DEFAULT_EXCEL_VERSION);
    }

    public SheetProxy(SpreadsheetVersion version) {
        if (version == SpreadsheetVersion.EXCEL97) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
    }

    public SheetProxy(String name, List<String> headerLabels, Iterator<Object[]> data) {
        this(SpreadsheetVersion.EXCEL97, name, headerLabels, data);
    }

    public SheetProxy(SpreadsheetVersion version, String name, List<String> headerLabels, Iterator<Object[]> data) {
        this((Workbook) null, name, headerLabels, data);
        if (version == SpreadsheetVersion.EXCEL97) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
    }

    public SheetProxy(Workbook workbook, String name, List<String> headerLabels, Iterator<Object[]> data) {
        this.name = name;
        this.data = data;
        this.headerLabels.addAll(headerLabels);
        this.workbook = workbook;
    }

    public SheetProxy(Workbook workbook, String name) {
        this.name = name;
        this.workbook = workbook;
    }

    public String getSheetName(int sheetNum) {
        if (sheetNum == 0) {
            return name;
        }
        return String.format("%s (%s)", name, sheetNum);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if ((workbook != null) && (workbook.getSheet(name) != null)) {
            throw new TdarRecoverableRuntimeException("sheetProxy.workbook_name_already_exists", Arrays.asList(name));
        }
        this.name = name;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public List<String> getHeaderLabels() {
        return headerLabels;
    }

    public void setHeaderLabels(List<String> headerLabels) {
        this.headerLabels = headerLabels;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public SpreadsheetVersion getVersion() {
        if (workbook instanceof HSSFWorkbook) {
            return SpreadsheetVersion.EXCEL97;
        }
        return SpreadsheetVersion.EXCEL2007;
    }

    public Iterator<Object[]> getData() {
        return data;
    }

    public void setData(Iterator<Object[]> data) {
        this.data = data;
    }

    public Callback getPreCallback() {
        return preCallback;
    }

    public void setPreCallback(Callback preCallback) {
        this.preCallback = preCallback;
    }

    public Callback getPostCallback() {
        return postCallback;
    }

    public void setPostCallback(Callback postCallback) {
        this.postCallback = postCallback;
    }

    public void setFreezeRow(int i) {
        freezeRow = 0;
    }

    public int getFreezeRow() {
        return freezeRow;
    }

    public boolean hasFreezeRow() {
        return freezeRow == 0;
    }

    public void preProcess() {
        if (preCallback != null) {
            preCallback.apply(workbook);
        }
    }

    public void postProcess() {
        if (postCallback != null) {
            postCallback.apply(workbook);
        }
    }

    public String getExtension() {
        if (getVersion() == SpreadsheetVersion.EXCEL2007) {
            return "xlsx";
        } else {
            return "xls";
        }
    }

    public boolean isCleanupNeeded() {
        return isCleanupNeeded;
    }

    public void setCleanupNeeded(boolean bool) {
        isCleanupNeeded = bool;
    }

    public String getNoteRow() {
        return noteRow;
    }

    public void setNoteRow(String noteRow) {
        this.noteRow = noteRow;
    }

    public boolean isAutosizeCols() {
        return autosizeCols;
    }

    public void setAutosizeCols(boolean autosizeCols) {
        this.autosizeCols = autosizeCols;
    }

}
