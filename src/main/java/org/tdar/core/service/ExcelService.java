package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.stereotype.Service;

@Service
public class ExcelService {

    public static final int MAX_ROWS_PER_SHEET = 30000;

    public <T extends Enum<?>> void validateColumn(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, T[] enums) {
        CellRangeAddressList addressList = new CellRangeAddressList();
        addressList.addCellRangeAddress(1, i, 10000, i);
        DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(getEnumNames(enums).toArray(new String[] {}));
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, validationConstraint);
        dataValidation.setEmptyCellAllowed(true);
        dataValidation.setShowPromptBox(true);
        dataValidation.setSuppressDropDownArrow(false);
        sheet.addValidationData(dataValidation);
    }

    private <T extends Enum<?>> List<String> getEnumNames(T[] enums) {
        List<String> toReturn = new ArrayList<String>();
        for (T value : enums) {
            toReturn.add(value.name());
        }
        return toReturn;
    }

    public HSSFRow createRow(HSSFSheet wb, int rowNum) {
        HSSFRow row = wb.getRow(rowNum);
        if (row == null) {
            row = wb.createRow(rowNum);
        }
        return row;
    }

    public HSSFCellStyle createSummaryStyle(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        HSSFFont summaryFont = workbook.createFont();
        summaryFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setWrapText(true);
        return summaryStyle;
    }

    public HSSFCellStyle createSummaryStyleGray(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = createSummaryStyle(workbook);
        summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        summaryStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
        return summaryStyle;
    }

    public HSSFCellStyle createSummaryStyleGreen(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = createSummaryStyle(workbook);
        summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        summaryStyle.setFillForegroundColor(new HSSFColor.LIGHT_GREEN().getIndex());
        return summaryStyle;
    }

    
    public HSSFCellStyle createTableNameStyle(HSSFWorkbook workbook) {
        HSSFCellStyle dataTableNameStyle = workbook.createCellStyle(); // DATA_TABLE_NAME_STYLE
        dataTableNameStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        dataTableNameStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
        return dataTableNameStyle;
    }

    public HSSFCellStyle createHeaderStyle(HSSFWorkbook workbook, HSSFFont font) {
        HSSFCellStyle headerStyle = workbook.createCellStyle(); // HEADER_STYLE
        headerStyle.setFont(font);
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setFillForegroundColor(new HSSFColor.LIGHT_CORNFLOWER_BLUE().getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return headerStyle;
    }

    public HSSFCell createCell(HSSFRow row, int position, String value) {
        return createCell(row, position, value, null);
    }

    public HSSFCell createCell(HSSFRow row, int position, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(position);

        if (!StringUtils.isEmpty(value)) {
            if (StringUtils.isNumeric(value)) {
                cell.setCellValue(Double.valueOf(value));
            } else {
                cell.setCellValue(value);
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        return cell;
    }

    public void createHeaderCell(HSSFCellStyle summaryStyle, HSSFRow row, int position, String text) {
        HSSFCell headerCell = row.createCell(position);
        headerCell.setCellValue(text);
        headerCell.setCellStyle(summaryStyle);
    }


    public void addComment(CreationHelper factory, Drawing drawing, HSSFCell cell, String commentText) {
        if (StringUtils.isEmpty(commentText))
            return;
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex()+3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex()+3);
        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(commentText);
        comment.setString(str);
        comment.setRow(cell.getRowIndex());
        comment.setColumn(cell.getColumnIndex());
        comment.setAuthor("tDAR");
    }

}
