//package org.tdar.core.service.processes;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.ObjectUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.tdar.core.bean.resource.CodingRule;
//import org.tdar.core.bean.resource.CodingSheet;
//import org.tdar.core.bean.resource.Dataset;
//import org.tdar.core.bean.resource.dataintegration.DataValueOntologyNodeMapping;
//import org.tdar.core.bean.resource.datatable.DataTable;
//import org.tdar.core.bean.resource.datatable.DataTableColumn;
//import org.tdar.core.bean.util.ScheduledBatchProcess;
//import org.tdar.core.service.DataIntegrationService;
//import org.tdar.utils.Pair;
//
///**
// * $Id$
// * 
// * One-time migration task to convert DataValueOntologyNodeMappings into their CodingSheet/CodingRule equivalent.
// * 
// * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
// * @version $Rev$
// */
//@Component
//public class UpdateDataValueMappingProcess extends ScheduledBatchProcess<Dataset> {
//
//    private static final long serialVersionUID = -995331174444056954L;
//
//    @Autowired
//    private DataIntegrationService dataIntegrationService;
//
//    public boolean isSingleRunProcess() {
//        return true;
//    }
//
//    public String getDisplayName() {
//        return "Convert DataValueOntologyNodeMappings to CodingRules";
//    }
//
//    public Class<Dataset> getPersistentClass() {
//        return Dataset.class;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return false;
//    }
//
//    @Override
//    public void process(Dataset dataset) {
//        List<DataTableColumn> mappedColumns = new ArrayList<DataTableColumn>();
//        // find all "mapped" columns
//        for (DataTable table : dataset.getDataTables()) {
//            for (DataTableColumn column : table.getDataTableColumns()) {
//                if (column.getDefaultOntology() != null) {
//                    mappedColumns.add(column);
//                }
//            }
//        }
//        if (mappedColumns.isEmpty()) {
//            return;
//        }
//        logger.debug("Processing mapped columns {} for dataset {}", mappedColumns, dataset);
//        for (DataTableColumn ontologyMappedColumn : mappedColumns) {
//            logger.debug("Processing column {}", ontologyMappedColumn);
//            // if this column doesn't have a coding sheet, create an identity coding sheet
//            CodingSheet defaultCodingSheet = ontologyMappedColumn.getDefaultCodingSheet();
//            if (defaultCodingSheet == null) {
//                defaultCodingSheet = dataIntegrationService.createGeneratedCodingSheet(ontologyMappedColumn, dataset.getSubmitter());
//                logger.debug("Created new generated coding sheet: {}", defaultCodingSheet);
//                ontologyMappedColumn.setDefaultCodingSheet(defaultCodingSheet);
//                genericDao.saveOrUpdate(ontologyMappedColumn);
//            }
//            defaultCodingSheet.setDefaultOntology(ontologyMappedColumn.getDefaultOntology());
//            // not strictly necessary
//            genericDao.saveOrUpdate(defaultCodingSheet);
//            // iterate through all column mappings and find their equivalent coding rule
//            Map<String, List<CodingRule>> termToCodingRuleMap = defaultCodingSheet.getTermToCodingRuleMap();
//            logger.debug("term to coding rule map: {}", termToCodingRuleMap);
//            logger.debug("values to ontology node mappings: {}", ontologyMappedColumn.getValueToOntologyNodeMapping());
//            for (DataValueOntologyNodeMapping mapping : ontologyMappedColumn.getValueToOntologyNodeMapping()) {
//                List<CodingRule> rules = termToCodingRuleMap.get(mapping.getDataValue());
//                if (CollectionUtils.isEmpty(rules)) {
//                    String errorMessage = "No coding rule found for value " + mapping.getDataValue();
//                    errors.add(Pair.create(dataset, new Throwable(errorMessage)));
//                    logger.debug(errorMessage);
//                    continue;
//                }
//                for (CodingRule rule : rules) {
//                    if (rule == null) {
//                        String errorMessage = "No coding rule found for value " + mapping.getDataValue();
//                        errors.add(Pair.create(dataset, new Throwable(errorMessage)));
//                        logger.debug(errorMessage);
//                        continue;
//                    }
//                    if (rule.getOntologyNode() != null && ObjectUtils.notEqual(rule.getOntologyNode(), mapping.getOntologyNode())) {
//                        String errorMessage = String.format("rule %s already has an ontology node different from %s - ignoring", rule,
//                                mapping.getOntologyNode());
//                        errors.add(Pair.create(dataset, new Throwable(errorMessage)));
//                        logger.debug(errorMessage);
//                        continue;
//                    }
//                    logger.debug("Setting ontology node on {} to {}", rule, mapping.getOntologyNode());
//                    rule.setOntologyNode(mapping.getOntologyNode());
//                }
//            }
//            genericDao.saveOrUpdate(defaultCodingSheet.getCodingRules());
//        }
//    }
//
//    @Override
//    public int getBatchSize() {
//        return 30;
//    }
//}
