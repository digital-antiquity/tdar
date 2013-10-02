package org.tdar.utils.bulkUpload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class BulkManifestProxy implements Serializable {

	private static final long serialVersionUID = -3716153931002809635L;

	private List<String> filenames = new ArrayList<String>();
	private List<String> filenamesInsensitive = new ArrayList<String>();
	private boolean caseSensitive = false;
	private Map<Row, List<String>> rowFilenameMap = new HashMap<Row, List<String>>();
	private Map<String, CellMetadata> cellLookupMap = new HashMap<String, CellMetadata>();
	private List<String> columnNames = new ArrayList<String>();
	private LinkedHashSet<CellMetadata> allValidFields = new LinkedHashSet<CellMetadata>();
	private Sheet sheet;

	private Row columnNamesRow;

	public void addFilename(String filename) {
		filenames.add(filename);
		filenamesInsensitive.add(filename.toLowerCase());
	}
	
	// don't want to expose the list directly for manipulation because we're managing two variants (case sensitive and insensitive)
	public String listFilenames() {
		return String.format("[%s]", StringUtils.join(filenames,","));
	}

	public boolean containsFilename(String filename) {
		if (!caseSensitive) {
			return filenamesInsensitive.contains(filename.toLowerCase());
		}
		return filenames.contains(filename);
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}

	public Map<Row, List<String>> getRowFilenameMap() {
		return rowFilenameMap;
	}

	public void setRowFilenameMap(Map<Row, List<String>> rowFilenameMap) {
		this.rowFilenameMap = rowFilenameMap;
	}

	public Map<String, CellMetadata> getCellLookupMap() {
		return cellLookupMap;
	}

	public void setCellLookupMap(Map<String, CellMetadata> cellLookupMap) {
		this.cellLookupMap = cellLookupMap;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public LinkedHashSet<CellMetadata> getAllValidFields() {
		return allValidFields;
	}

	public void setAllValidFields(LinkedHashSet<CellMetadata> allValidFields) {
		this.allValidFields = allValidFields;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	public int getLastCellNum() {
		return columnNamesRow.getLastCellNum();
	}

	public int getFirstCellNum() {
		return columnNamesRow.getFirstCellNum();
	}

	public void setColumnNamesRow(Row columnNamesRow) {
		this.columnNamesRow = columnNamesRow;

	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

}
