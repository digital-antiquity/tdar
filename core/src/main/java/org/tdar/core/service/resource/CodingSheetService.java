package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.List;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.parser.CodingSheetParserException;
import org.tdar.workflows.WorkflowContext;

public interface CodingSheetService {

    List<CodingSheet> findSparseCodingSheetList();

    void ingestCodingSheet(CodingSheet codingSheet, WorkflowContext ctx);

    void reconcileOntologyReferencesOnRulesAndDataTableColumns(CodingSheet codingSheet, Ontology ontology_);

    List<CodingSheet> findAllUsingOntology(Ontology ontology);

    List<String> updateCodingSheetMappings(CodingSheet codingSheet, TdarUser authenticatedUser, List<CodingRule> incomingRules);

    boolean isOkToMapOntology(CodingSheet persistable);

    void parseUpload(CodingSheet sheet, InformationResourceFileVersion version) throws IOException, CodingSheetParserException;

    List<CodingSheet> findAll();

    List<CodingRule> addSpecialCodingRules(CodingSheet codingSheet, List<CodingRule> codingRules);

}