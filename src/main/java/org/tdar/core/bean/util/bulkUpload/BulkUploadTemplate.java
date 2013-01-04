package org.tdar.core.bean.util.bulkUpload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.excel.CellFormat;

public class BulkUploadTemplate {

    private static final String EXCEL_MAX_NUM = "99999999999999";
    private static final String EXCEL_MIN_NUM = "-99999999999999";
    private ExcelService excelService;
    public static final String BULK_TEMPLATE_TITLE = "BULK_TEMPLATE_TITLE";
    public static final String EXAMPLE_TIFF = "TDAR_EXAMPLE.TIFF";
    public static final String EXAMPLE_PDF = "TDAR_EXAMPLE.PDF";
    public static final String FILENAME = "filename";

    public BulkUploadTemplate(ExcelService excelService) {
        this.setExcelService(excelService);
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public HSSFWorkbook getTemplate(LinkedHashSet<CellMetadata> fieldnameSet) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("template");
        CreationHelper factory = workbook.getCreationHelper();

        HSSFRow row = sheet.createRow(0);
        // When the comment box is visible, have it show in a 1x3 space

        CellStyle defaultStyle = getExcelService().createSummaryStyle(workbook);
        // CellStyle resourceCreatorRoleStyle = CellFormat.NORMAL.setBorderRight((short) 2).createStyle(workbook);
        CellStyle headerStyle2 = CellFormat.BOLD.setColor(new HSSFColor.GREY_25_PERCENT()).setWrapping(true).setFontSize((short) 10).createStyle(workbook);
        CellStyle requiredStyle = CellFormat.BOLD.setWrapping(false).createStyle(workbook);
        requiredStyle.setFillForegroundColor(new HSSFColor.ROSE().getIndex());

        HashMap<String, String> exampleDoc = new HashMap<String, String>();
        HashMap<String, String> exampleImage = new HashMap<String, String>();

        exampleDoc.put(FILENAME, EXAMPLE_PDF);
        exampleImage.put(FILENAME, EXAMPLE_TIFF);
        exampleDoc.put("title", "EXAMPLE TITLE");
        exampleImage.put("title", "EXAMPLE TITLE");

        exampleDoc.put("title", "EXAMPLE TITLE");
        exampleDoc.put("bookTitle", "Book Title");
        exampleDoc.put("startPage", "20");
        exampleDoc.put("endPage", "40");
        exampleDoc.put("issn", "1111-1111");
        exampleDoc.put("documentType", "BOOK_SECTION");
        exampleImage.put("title", "EXAMPLE TITLE");

        exampleImage.put("ResourceCreatorPerson.role", "CREATOR");
        exampleImage.put("ResourceCreatorPerson.Person.email", "test@test.com");
        exampleImage.put("ResourceCreatorPerson.Person.firstName", "First Name");
        exampleImage.put("ResourceCreatorPerson.Person.lastName", "Last Name");

        exampleDoc.put("ResourceCreatorPerson.Person.email", "test@test.com");
        exampleDoc.put("ResourceCreatorPerson.Person.firstName", "First Name");
        exampleDoc.put("ResourceCreatorPerson.Person.lastName", "Last Name");
        exampleDoc.put("ResourceCreatorPerson.role", "AUTHOR");

        exampleDoc.put("ResourceCreatorInstitution.role", "AUTHOR");
        exampleDoc.put("ResourceCreatorInstitution.Institution.name", "Institutional Author");

        Drawing drawing = sheet.createDrawingPatriarch();

        int i = 0;
        HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(sheet);
        Set<CellMetadata> enumFields = new HashSet<CellMetadata>();
        for (CellMetadata field : fieldnameSet) {

            if (CollectionUtils.isNotEmpty(field.getEnumList())) {
                getExcelService().addColumnValidation(sheet, i, validationHelper, field.getEnumList().toArray(new Enum[0]));
                enumFields.add(field);
            }

            if (field.isNumeric()) {
                if (field.isFloatNumber()) {
                    getExcelService().addNumericColumnValidation(sheet, i, validationHelper, EXCEL_MIN_NUM, EXCEL_MAX_NUM);
                } else {
                    getExcelService().addIntegerColumnValidation(sheet, i, validationHelper, EXCEL_MIN_NUM, EXCEL_MAX_NUM);
                }
            }

            row.createCell(i).setCellValue(field.getOutputName());
            CellStyle style = defaultStyle;
            if (field.getMappedClass() != null && field.getMappedClass().equals(Document.class)) {
                style = headerStyle2;
            } else if (field.isRequired()) {
                style = requiredStyle;
            } else if (CollectionUtils.isNotEmpty(field.getEnumList()) && ArrayUtils.contains(ResourceCreatorRole.values(), field.getEnumList().get(0))) {
                // style = resourceCreatorRoleStyle;
            } else {
                style = defaultStyle;
            }
            row.getCell(i).setCellStyle(style);

            getExcelService().addComment(factory, drawing, row.getCell(i), field.getComment());

            i++;
        }

        HSSFRow rowDoc = sheet.createRow(1);
        HSSFRow rowImg = sheet.createRow(2);
        i = 0;
        for (CellMetadata field : fieldnameSet) {
            String imgFld = exampleImage.remove(field.getName());
            String docFld = exampleDoc.remove(field.getName());
            if (imgFld == null) {
                imgFld = "";
            }
            if (docFld == null) {
                docFld = "";
            }

            rowImg.createCell(i).setCellValue(imgFld);
            rowDoc.createCell(i).setCellValue(docFld);
            i++;
        }

        HSSFSheet referenceSheet = workbook.createSheet("REFERENCE");

        CellStyle summaryStyle = getExcelService().createSummaryStyle(workbook);

        i = 0;
        for (CellMetadata field : enumFields) {
            if (field.getName().equals("ResourceCreatorInstitution.role"))
                continue;
            addReferenceColumn(referenceSheet, field.getEnumList().toArray(new Enum[0]), field.getDisplayName() + " Values:", summaryStyle, i);
            i++;
        }

        // autosize
        for (int c = 0; c < 4; c++) {
            referenceSheet.autoSizeColumn(i);
        }

        for (int c = 0; c < fieldnameSet.size(); c++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    public <T extends Enum<T>> void addReferenceColumn(Sheet wb, T[] labels,
            String header, CellStyle summaryStyle, int col) {
        int rowNum = 0;
        Row row = getExcelService().createRow(wb, rowNum);
        row.createCell(col).setCellValue(header);
        row.getCell(col).setCellStyle(summaryStyle);
        for (T type : labels) {
            rowNum++;
            getExcelService().createRow(wb, rowNum).createCell(col).setCellValue(type.name());
        }
    }

    public ExcelService getExcelService() {
        return excelService;
    }

    public void setExcelService(ExcelService excelService) {
        this.excelService = excelService;
    }

}
