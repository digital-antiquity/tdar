/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils.sensorydata;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.ScannerTechnologyType;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;
import org.tdar.utils.sensorydata.enums.ImageField;
import org.tdar.utils.sensorydata.enums.MeshField;
import org.tdar.utils.sensorydata.enums.ProjectField;
import org.tdar.utils.sensorydata.enums.RegistrationField;
import org.tdar.utils.sensorydata.enums.ScanField;

public class AdsTemplateProcessor {
    
    private DataFormatter formatter = new HSSFDataFormatter();
    private FormulaEvaluator evaluator;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    //TODO:  break out excel traversal into separate class(es).  Could be generally useful.
    interface CellListener {
        public void cellVisited(String label, Cell valueCell);
    }
    
    interface RowListener extends CellListener{
        public void rowVisited(Row row);
    }
    
    
    public SensoryData getSensoryDataFromAdsTemplate(File adsTemplate) {
        // put this in constructor
        SensoryData sensoryData = new SensoryData();
        try {
            
            logger.debug("Processing {}", adsTemplate);
            Workbook book = WorkbookFactory.create(new FileInputStream(adsTemplate));
            evaluator = book.getCreationHelper().createFormulaEvaluator();
            
            handleProjectInfo(sensoryData, book.getSheet("1-Project Description")); // 1-Project Description
            handleScans(sensoryData, book.getSheet("2-Scan Metadata")); // 2-Scan Metadata
            handleRegistration(sensoryData, book.getSheet("3-Registration Metadata")); // 3-Registration Metadata
            handleMesh(sensoryData, book.getSheet("4-Mesh Metadata")); // 4-Mesh Metadata
            handleImages(sensoryData, book.getSheet("5-Image Metadata")); // 5-Image Metadata
        } catch (InvalidFormatException ifx) {
            AdsImportException ex = new AdsImportException(ifx.getMessage());
            throw (ex);
        } catch (IOException iox) {
            AdsImportException ex = new AdsImportException(iox.getMessage());
            throw (ex);
        }

        return sensoryData;
    }
    
	private void handleProjectInfo(final SensoryData sensoryData, Sheet sheet) {
	    if(sheet==null) throw(new AdsImportException("required sheet missing"));
        visitNameValuePairs(sheet, 0, 0, new CellListener() {
            public void cellVisited(String label, Cell cell)  {
                if(StringUtils.isBlank(label)) return; //ignore blank labels, they are probably from blank rows
                ProjectField field = ProjectField.fromLabel(label);
                if(field==null) {
                    //TODO: throw something
                }
                String strVal = getCellValue(cell);
                switch(field) {
                    case PROJECT_NAME://ignored
                        //TODO: try to use assign documents to a project.
                        break;
                    case NAME_OF_MONUMENT_SURVEY_AREA_OR_OBJECT:
                        sensoryData.setTitle(strVal);
                        break;
                    case MONUMENT_OBJECT_NUMBER:
                        sensoryData.setMonumentNumber(strVal);
                        break;
                    case MONUMENT_OBJECT_DESCRIPTION:
                        sensoryData.setDescription(strVal);
                        break;
                    case SURVEY_LOCATION: //ignored
                        //TODO: they use park/city/state names... map to geo keywords, perhaps?
                        break;
                    case SURVEY_DATES:
                        setDateCreated(sensoryData, cell);
                        break;
                    case SURVEY_CONDITIONS:
                        sensoryData.setSurveyConditions(strVal);
                        break;
                    case SCANNER_DETAILS:
                        sensoryData.setScannerDetails(strVal);
                        break;
                    case COMPANY_OPERATOR_NAME:
                        sensoryData.setCompanyName(strVal);
                        break;
                    case CONTROL_DATA_COLLECTED: //ignored (we use filename, they use yes/no)
                        break;
                    case TURNTABLE_USED:
                        sensoryData.setTurntableUsed(toBool(strVal));
                        break;
                    case RGB_DATA_CAPTURE:
                        sensoryData.setRgbDataCaptureInfo(strVal);
                        break;
                    case ESTIMATED_DATA_RESOLUTION:
                        sensoryData.setEstimatedDataResolution(toDouble(cell));
                        break;
                    case TOTAL_NUMBER_OF_SCANS_IN_PROJECT:
                        sensoryData.setTotalScansInProject(toLong(cell));
                        break;
                    case DESCRIPTION_OF_FINAL_DATASETS_FOR_ARCHIVE:
                        sensoryData.setFinalDatasetDescription(strVal);
                        break;
                    case PLANIMETRIC_MAP_OF_SCAN_COVERAGE_AREAS: //ignored (we use filename, they use yes/no)
                        break;
                    case ADDITIONAL_PROJECT_NOTES:
                        sensoryData.setAdditionalProjectNotes(strVal);
                        break;
                    case IMAGES_FROM_SURVEY: //ignored: we'll grab this info from the 'image' tab 
                        break;
                }
            }
        });
    }
    
    private void handleScans(final SensoryData sensoryData, Sheet sheet) {
        if(sheet==null) return;
    	visitTable(sheet, 0, 0, false, new RowListener() {
    	    int sequence;
    		SensoryDataScan currentScan = null;
    		public void rowVisited(Row row) {
    			currentScan = new SensoryDataScan();
    			currentScan.setSequenceNumber(sequence++);
    			sensoryData.getSensoryDataScans().add(currentScan);
    		}

			public void cellVisited(String label, Cell cell) {
				ScanField field = ScanField.fromLabel(label);
				if(field==null) {
					//throw somthing
				}
				String strVal = getCellValue(cell);
                if(StringUtils.length(strVal)> 255) {
                    logger.warn("scan field {} exceeds 255 characters - make sure this column is at least varchar({})", label, strVal.length());
                }
				
				switch(field) {
			    case SCAN_FILENAME:
			        currentScan.setFilename(strVal);
			    	break;
			    case SCAN_TRANSFORMATION_MATRIX:
			        currentScan.setTransformationMatrix(strVal);
			    	break;
			    case MATRIX_APPLIED_TO_SCANS: 
			        currentScan.setMatrixApplied(toBool(strVal));
			    	break;
			    case NAME_OF_MONUMENT:
			        currentScan.setMonumentName(strVal);
			    	break;
			    case SURVEY_DATE:
			        currentScan.setScanDate(toDate(label, cell));
			    	break;
			    case NUMBER_OF_POINTS_IN_SCAN:
			        currentScan.setPointsInScan(toLong(cell));
			    	break;
			    case ADDITIONAL_SCAN_NOTES:
			        currentScan.setScanNotes(strVal);
			    	break;
			    case SCANNER_TECHNOLOGY:
			        currentScan.setScannerTechnology(toScanType(cell));
			    	break;
			    case DATA_RESOLUTION:
			        currentScan.setResolution(toDouble(cell));
			    	break;
			    case LENSE_OR_FOV_DETAILS:
			        currentScan.setTriangulationDetails(strVal);
			    	break;
				}
			}
    	});
    	
        //iterate over scans and remove all invalid items
        Iterator<SensoryDataScan> iter = sensoryData.getSensoryDataScans().iterator();
        while(iter.hasNext()) {
            SensoryDataScan image = iter.next();
            if(!image.isValid()) {
                logger.warn("encountered invalid scan record - removing");
                iter.remove();
                
            }
        }
        
    	
    }
    
    private void handleRegistration(final SensoryData sensoryData, Sheet sheet) {
        if(sheet==null) {
            logger.warn("sheet has no registration page. perhaps sheet name contains typo?");
            return;
        }
    	visitTable(sheet, 0, 0, false, new RowListener() {
    	    int rowCount;
			public void cellVisited(String label, Cell cell) {
		    	RegistrationField field = RegistrationField.fromLabel(label);
		    	switch(field) {
		    	case GLOBAL_REGISTRATION_ERROR:
		    	    sensoryData.setRegistrationErrorUnits(toDouble(cell));
		    		break;
		    	case NAME_OF_REGISTERED_DATASET:
		    	    sensoryData.setRegisteredDatasetName(getCellValue(cell));
		    		break;
		    	case TOTAL_NUMBER_OF_POINTS:
		    	    sensoryData.setFinalRegistrationPoints(toLong(cell));
		    		break;
		    	}
			}

			//This sheet is laid out in rowformat but always has one row (we think)
			public void rowVisited(Row row) {
			    if(++rowCount > 1) {
			        throw(new AdsImportException("sensorydata document has more than one registration row:" + sensoryData.getTitle()));
			    }
			}
    	});
    }
    
	private void handleMesh(final SensoryData sd, Sheet sheet) {
        if(sheet==null) {
            logger.warn("sheet has no mesh info page. perhaps sheet name contains typo?");
            return;
        }
		visitNameValuePairs(sheet, 1, 0, new CellListener(){
			public void cellVisited(String label, Cell cell) {
			    String strVal = getCellValue(cell);
			    if(StringUtils.length(strVal)> 255) {
			        logger.warn("sensoryData field {} exceeds 255 characters - make sure this column is at least varchar({})", label, strVal.length());
			    }
			    boolean boolVal = toBool(strVal); 
				MeshField field = MeshField.fromLabel(label);
				if (field==null) {
				    throw(new AdsImportException("field not recognized:" + label));
				}
				switch(field) {
				case PREMESH_NAME_OF_PREMESH_DATASET:
				    sd.setPreMeshDatasetName(strVal);
					break;
				case PREMESH_NUMBER_OF_POINTS_IN_FILE:
				    sd.setPreMeshPoints(toLong(cell));
					break;
				case PREMESH_OVERLAP_REDUCTION:
				    sd.setPremeshOverlapReduction(boolVal);
					break;
				case PREMESH_SMOOTHING:
				    sd.setPremeshSmoothing(boolVal);
					break;
				case PREMESH_SUBSAMPLING:
				    sd.setPremeshSubsampling(boolVal);
					break;
				case PREMESH_COLOR_EDITIONS:
				    sd.setPremeshColorEditions(boolVal);
					break;
				case PREMESH_POINT_DELETION_SUMMARY:
				    sd.setPointDeletionSummary(strVal);
					break;
				case MESH_NAME_OF_MESH_DATASET:
				    sd.setMeshDatasetName(strVal);
					break;
				case MESH_HOLES_FILLED:
				    sd.setMeshHolesFilled(boolVal);
					break;
				case MESH_SMOOTHING:
				    sd.setMeshSmoothing(boolVal);
					break;
				case MESH_COLOR_EDITIONS:
				    sd.setMeshColorEditions(boolVal);
					break;
				case MESH_HEALING_DESPIKING:
				    sd.setMeshHealingDespiking(boolVal);
					break;
				case MESH_TOTAL_TRIANGLE_COUNT:
				    sd.setMeshTriangleCount(toLong(cell));
					break;
				case MESH_RGB_COLOR_INCLUDED:
				    sd.setMeshRgbIncluded(boolVal);
					break;
				case MESH_DATA_REDUCTION:
				    sd.setMeshdataReduction(boolVal);
					break;
				case MESH_COORDINATE_SYSTEM_ADJUSTMENT: //ignored, we use presence of cs matrix as implicit true
					break;
				case MESH_CS_ADJUSTMENT_MATRIX:
				    sd.setMeshAdjustmentMatrix(strVal);
					break;
				case MESH_ADDITIONAL_PROCESSING_NOTES:
				    sd.setMeshProcessingNotes(strVal);
					break;
				case DECIMATED_NAME_OF_DECIMATED_MESH_DATASET:
				    sd.setDecimatedMeshDataset(strVal);
					break;
				case DECIMATED_TOTAL_ORIGINAL_TRIANGLE_COUNT:
				    sd.setDecimatedMeshOriginalTriangleCount(toLong(cell));
					break;
				case DECIMATED_DECIMATED_TRIANGLE_COUNT:
				    sd.setDecimatedMeshTriangleCount(toLong(cell));
					break;
				case DECIMATED_RGB_COLOR_PRESERVED_FROM_ORIGINAL_DATASET:
				    sd.setRgbPreservedFromOriginal(boolVal);
					break;
				}
			}});
	}

	private void handleImages(final SensoryData sensoryData, final Sheet sheet) {
        if(sheet==null) {
            logger.warn("sheet has no image tab. perhaps sheet name contains typo?");
            return;
        }
        logger.trace("row count: {}", sheet.getLastRowNum());
        
		visitTable(sheet, 0, 0, false, new RowListener(){
		    int sequence;
			SensoryDataImage currentImage;
			
			public void rowVisited(Row row) {
		        //TODO: tell angie@uark: there is a typo in the image tab for the ads template where R10C4 contains '`' unless the scan contains 9 or more images
		        currentImage = new SensoryDataImage();
		        currentImage.setSequenceNumber(sequence++);
		        sensoryData.getSensoryDataImages().add(currentImage);
			}

			public void cellVisited(String label, Cell cell) {
                String strVal = getCellValue(cell);
                if(StringUtils.length(strVal)> 255) {
                    logger.warn("sensoryDataImage field {} exceeds 255 characters - make sure this column is at least varchar({})", label, strVal.length());
                }
			    
				ImageField field = ImageField.fromLabel(label);
				switch(field) {
				case IDENTIFIER_IMAGE_FILE_NAME:
//				    if(StringUtils.isBlank(strVal)) {
//				        logger.warn("null filename on row {}", cell.getRow().getRowNum());
//				    }
				    currentImage.setFilename(strVal);
					break;
				case TITLE__CAPTION:  //ignored - i would prefer to use this instead of a filename but it is usually blank or "NA".
					break;
				case DESCRIPTION_OF_IMAGE:
				    currentImage.setDescription(getCellValue(cell));
					break;
				case CREATOR: //ignored, but check for typo
					break;
				case DATE: //ignored
					break;
				case RIGHTS: //ignored
					break;
				case KEYWORDS:
				    setOtherKeywords(sensoryData, cell); //this field appears to have potentially helpful terms in it
					break;
				case LOCATION: //ignored
					break;
				}
			}
		});
		
		//iterate over images and remove all invalid items
		Iterator<SensoryDataImage> iter = sensoryData.getSensoryDataImages().iterator();
		while(iter.hasNext()) {
		    SensoryDataImage image = iter.next();
		    if(!image.isValid()) {
		        logger.trace("encountered invalid image record - removing");
		        iter.remove();
		        
		    }
		}
		
	}
    
    private void setDateCreated(InformationResource r, Cell cell) {
        if(cell.getCellType() != Cell.CELL_TYPE_NUMERIC && cell.getCellType() != Cell.CELL_TYPE_FORMULA) return; //let's not  mess w/ string values for now
        Date date = null;
        try {
            date = cell.getDateCellValue();
        } catch (NumberFormatException nfx) {
            logger.error("invalid number when extracting dateCreated for sensoryDocument: {}.  value:", r.getTitle(), getCellValue(cell));
        } catch (IllegalStateException isx) {
            logger.error("encountered string when extracting dateCreated for sensoryDocument: {}.  value:", r.getTitle(), getCellValue(cell));
        }
        //TODO: this can't be the easiest (non-deprecated) way to get a year.  It just can't be.   
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Integer year = new Integer(cal.get(Calendar.YEAR)); 
        r.setDate(year);
    }
    
    private void setOtherKeywords(InformationResource resource, Cell cell) {
        if(cell== null) return;
        if(isBlank(getCellValue(cell))) return;
        
        String[] keywords = StringUtils.split(getCellValue(cell),',');
        for(String keyword: keywords) {
            OtherKeyword ok = new OtherKeyword();
            ok.setLabel(keyword.trim());
            resource.getOtherKeywords().add(ok);
            logger.trace("adding otherkeyword {} to sensory document {}", ok, resource);
        }
    }
    
    private void setGeographicKeywords(InformationResource resource, Cell cell) {
        if(cell== null) return;
        if(isBlank(getCellValue(cell))) return;
        
        String[] keywords = StringUtils.split(getCellValue(cell), ";");  //from my cursory look it seems these are semicolon-separated
        for(String keyword: keywords) {
            GeographicKeyword geoKeyword = new GeographicKeyword();
            geoKeyword.setLabel(keyword.trim());
        }
        
    }

    private Date toDate(String label, Cell cell) {
        Date date = null;
        try {
            if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                date = cell.getDateCellValue();
            }
        } catch (NumberFormatException nfx) {
            logger.warn("Unable to convert numeric value to date. resource:{}, field:{},  value:{}", label, cell );
        } catch (IllegalStateException isx) {
            logger.warn("tried to convert a date value and encountered a string");
        }

        logger.trace("extracting date from '{}'.  Converting {} to {} (cell type: {} ) ", new Object[] {label, getCellValue(cell), date, cell.getCellType()});
        return date;
    }
    
    private ScannerTechnologyType toScanType(Cell cell) {
        if(cell == null) return null;
        ScannerTechnologyType type = ScannerTechnologyType.fromLabel(getCellValue(cell).trim());
        if(type==null) {
            logger.warn("Couln't find technology type for label '{}'. Consider adding alias in ScannerTechnologyType.labelsToEnum");
        }
        return type;
    }
    
    private boolean toBool(String str) {
        if(isBlank(str)) return false;
        return str.toLowerCase().startsWith("y") || str.toLowerCase().startsWith("t");
    }
    
    private Double toDouble(Cell cell) {
        if(cell == null) return null;
        return new Double(getCellValue(cell));
    }

    private Long toLong(Cell cell) {
        if(cell == null) return null;
        return new Long(getCellValue(cell));
    }
    
    private void visitNameValuePairs(Sheet sheet, int startRownum, int startColnum, CellListener listener) {
        for(int i = startRownum; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell labelCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
            Cell valueCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
            String label= null;
            if(labelCell != null && valueCell != null) {
                label = labelCell.getStringCellValue();
                listener.cellVisited(label, valueCell);
            }
        }
    }
    
    private void visitTable(Sheet sheet, int headerRownum, int startColnum, boolean includeBlankRows, RowListener listener) {
        //first get the field names from the header row.
        Row headerRow = sheet.getRow(headerRownum);
        int colCount = headerRow.getLastCellNum();
        String cols[] = new String[colCount];
        for(int j = startColnum; j < colCount; j++) {
            cols[j - startColnum] = getCellValue(headerRow, j);
        }
        
        //troll through the table, telling the listener whenever we encounter a row or cell
        //now compile our list of maps.
        for(int i = headerRownum + 1; i <= sheet.getLastRowNum(); i++ ) {
            Row currentRow = sheet.getRow(i);
            if(currentRow==null) continue;
            if(includeBlankRows || !isAllBlank(currentRow, startColnum) ) {
                listener.rowVisited(currentRow);
                for(int j = startColnum; j < colCount; j++) {
                    String label = cols[j - startColnum];
                    Cell valueCell = currentRow.getCell(j, Row.RETURN_BLANK_AS_NULL);
                    listener.cellVisited(label, valueCell);
                }
            }
            
        }
    }
    
    //TODO: this is inefficient. We can/should determine all-blank status in one pass in visitTable()
    private boolean isAllBlank(Row row, int startColnum) {
        for(int i = startColnum; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if(cell != null) {
                if(isNotBlank(getCellValue(cell))) return false;
            }
        }
        return true;
    }
    
    public String getCellValue(Row row, int columnIndex) {
        return getCellValue(row.getCell(columnIndex));
    }
    
    public String getCellValue(Cell cell) {
        String val = formatter.formatCellValue(cell, evaluator);
        //FIXME: I shouldn't need check for blanks, as it's supposedly done by the row.getCell(i) that  precedes the call to this function. 
        //       I must be calling it wrong.
        if(isBlank(val)) return null;  
        return val; 
    }

}
