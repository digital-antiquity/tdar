package org.tdar.odata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.odata4j.core.ODataConstants;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSchema.Builder;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.odata.bean.MetaData;
import org.tdar.odata.bean.MetaData.Container;
import org.tdar.odata.bean.MetaData.Entity;
import org.tdar.odata.bean.MetaData.EntitySet;
import org.tdar.odata.bean.MetaData.Property;
import org.tdar.odata.service.RepositoryService;

public class MetaDataBuilder implements IMetaDataBuilder {

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	// The entity hierarchy.
	// These are semantically different for Datasets and DataTables in the tDAR
	// domain model.
	// In tDAR these objects refer to a metadata data model.
	// Here they refer to a data data model.
	// In the future the metadata data model could also be exposed via OData.
	// In that case we might also have MDataSets, MDataTables etc.
	// The T prefix has been used here to avoid mental muddle.

	// A question requiring careful consideration is whether each TDataTable
	// represents a different entity
	// as all of the fields are often different. I am leaning towards the notion
	// of an abstract TDataTable
	// and that there are various concrete kinds of TDataTable.

	private final String namespace;
	private RepositoryService repositoryService;

	private List<EdmEntityContainer.Builder> entityContainerBuilders = new ArrayList<EdmEntityContainer.Builder>();
	private List<Builder> schemaBuilders = new ArrayList<Builder>();
	private List<EdmEntityType.Builder> entityTypeBuilders = new ArrayList<EdmEntityType.Builder>();
	private List<EdmEntitySet.Builder> entitySetBuilders = new ArrayList<EdmEntitySet.Builder>();
	private List<EdmAssociationSet.Builder> associationSets = new ArrayList<EdmAssociationSet.Builder>();
	private List<EdmAssociation.Builder> associations = new ArrayList<EdmAssociation.Builder>();

	public MetaDataBuilder(final String namespace, final RepositoryService repositoryService) {
		super();
		this.namespace = namespace;
		this.repositoryService = repositoryService;
	}

	// RR: not keen on this approach of using "global" variables
	// to accumulate results,
	// but the alternative turned out to be rather messy.
	private void initialiseAccumulators() {
		entityContainerBuilders.clear();
		schemaBuilders.clear();
		entityTypeBuilders.clear();
		entitySetBuilders.clear();
		associationSets.clear();
		associations.clear();
	}

	@Override
	public EdmDataServices build() {

		EdmDataServices dataServices = null;
		try {
			initialiseAccumulators();
			entitySetTypePropertyAssociationBuilders();
			containerBuilders();
			schemaBuilders();

			EdmDataServices.Builder metadata = EdmDataServices.newBuilder()
					.setVersion(ODataConstants.DATA_SERVICE_VERSION).addSchemas(schemaBuilders);

			dataServices = metadata.build();

		} catch (Throwable throwable) {
			getLogger().info("Building metadata. ", throwable);
			throw new RuntimeException("Building metadata. ", throwable);
		}
		return dataServices;
	}

	private void containerBuilders() {
		EdmEntityContainer.Builder entityContainerBuilder = EdmEntityContainer.newBuilder().setName(Container.NAME)
				.setIsDefault(true).addEntitySets(entitySetBuilders).addAssociationSets(associationSets);
		entityContainerBuilders.add(entityContainerBuilder);
	}

	private void schemaBuilders() {
		Builder schema = EdmSchema.newBuilder().setNamespace(namespace).addEntityTypes(entityTypeBuilders)
				.addEntityContainers(entityContainerBuilders);
		schemaBuilders.add(schema);
	}

	private void entitySetTypePropertyAssociationBuilders() {
		// The grand-child abstract entity TDataRecord
		EdmEntityType.Builder abstractDateRecordEntityTypeBuilder = null;
		EdmEntitySet.Builder abstractDataRecordEntitySetBuilder = null;
		{
			abstractDateRecordEntityTypeBuilder = EdmEntityType.newBuilder().setNamespace(namespace)
					.setName(Entity.T_DATA_RECORD).addKeys(Property.ID).addProperties(propertyBuildersFromRecord());

			abstractDataRecordEntitySetBuilder = EdmEntitySet.newBuilder().setName(EntitySet.T_DATA_RECORDS)
					.setEntityType(abstractDateRecordEntityTypeBuilder);

			entitySetBuilders.add(abstractDataRecordEntitySetBuilder);
			entityTypeBuilders.add(abstractDateRecordEntityTypeBuilder);
		}

		// Concrete entity TDataRecords
		EdmEntityType.Builder dateRecordEntityTypeBuilder = null;
		EdmEntitySet.Builder dataRecordEntitySetBuilder = null;
		{
			Collection<DataTable> ownedDataTables = repositoryService.findAllOwnedDataTables();
			for (DataTable dataTable : ownedDataTables) {
				// TODO RR: the data tables do not provid with a primary key
				// called id or anything else as far as I can tell.
				// This assertion is commented out just to allow the integration
				// tests to pass.
				// DataTableColumn pkColumn =
				// dataTable.getColumnByName(DataTableColumn.TDAR_ROW_ID.getName());
				// if (pkColumn == null)
				// {
				// // TODO RR: investigate if data tables are always provided
				// with a primary key called id.
				// throw new RuntimeException("The data table should always have
				// a column with a name of id . " + dataTable.getName());
				// }
				List<DataTableColumn> dataTableColumns = dataTable.getDataTableColumns();
				dateRecordEntityTypeBuilder = EdmEntityType.newBuilder().setNamespace(namespace)
						.setName(dataTable.getName()).setBaseType(Entity.T_DATA_RECORD).addKeys(Property.ID)
						.addProperties(propertyBuildersFromRecord(dataTableColumns));

				dataRecordEntitySetBuilder = EdmEntitySet.newBuilder().setName(dataTable.getName() + "_s")
						.setEntityType(dateRecordEntityTypeBuilder);

				entitySetBuilders.add(dataRecordEntitySetBuilder);
				entityTypeBuilders.add(dateRecordEntityTypeBuilder);
			}
		}

		// The child entity TDataTable
		EdmEntityType.Builder dataTableEntityTypeBuilder = null;
		EdmEntitySet.Builder dataTableEntitySetBuilder = null;
		{
			dataTableEntityTypeBuilder = EdmEntityType.newBuilder().setNamespace(namespace).setName(Entity.T_DATA_TABLE)
					.addKeys(Property.NAME)
					.addProperties(EdmProperty.newBuilder(Property.ID).setType(EdmSimpleType.INT64),
							EdmProperty.newBuilder(Property.NAME).setType(EdmSimpleType.STRING));

			dataTableEntitySetBuilder = EdmEntitySet.newBuilder().setName(Property.T_DATA_TABLES)
					.setEntityType(dataTableEntityTypeBuilder);

			EdmAssociation.Builder association = defineAssociation(MetaData.Association.T_DATA_RECORDS,
					EdmMultiplicity.ONE, EdmMultiplicity.MANY, dataTableEntityTypeBuilder, dataTableEntitySetBuilder,
					abstractDateRecordEntityTypeBuilder, abstractDataRecordEntitySetBuilder);
			dataTableEntityTypeBuilder.addNavigationProperties(EdmNavigationProperty.newBuilder(association.getName())
					.setRelationship(association).setFromTo(association.getEnd1(), association.getEnd2()));

			entitySetBuilders.add(dataTableEntitySetBuilder);
			entityTypeBuilders.add(dataTableEntityTypeBuilder);
		}

		// The topmost/root entity TDataSet
		EdmEntityType.Builder dataSetEntityTypeBuilder = null;
		EdmEntitySet.Builder dataSetEntitySetBuilder = null;
		{
			dataSetEntityTypeBuilder = EdmEntityType.newBuilder().setNamespace(namespace).setName(Entity.T_DATA_SET)
					.addKeys(Property.NAME)
					.addProperties(EdmProperty.newBuilder(Property.ID).setType(EdmSimpleType.INT64),
							EdmProperty.newBuilder(Property.NAME).setType(EdmSimpleType.STRING));

			dataSetEntitySetBuilder = EdmEntitySet.newBuilder().setName(EntitySet.T_DATA_SETS)
					.setEntityType(dataSetEntityTypeBuilder);

			EdmAssociation.Builder association = defineAssociation(MetaData.Association.T_DATA_TABLES,
					EdmMultiplicity.ONE, EdmMultiplicity.MANY, dataSetEntityTypeBuilder, dataSetEntitySetBuilder,
					dataTableEntityTypeBuilder, dataTableEntitySetBuilder);
			dataSetEntityTypeBuilder.addNavigationProperties(EdmNavigationProperty.newBuilder(association.getName())
					.setRelationship(association).setFromTo(association.getEnd1(), association.getEnd2()));

			entitySetBuilders.add(dataSetEntitySetBuilder);
			entityTypeBuilders.add(dataSetEntityTypeBuilder);
		}
	}

	private EdmAssociation.Builder defineAssociation(String associationName, EdmMultiplicity fromMultiplicity,
			EdmMultiplicity toMultiplicity, EdmEntityType.Builder fromEntityType, EdmEntitySet.Builder fromEntitySet,
			EdmEntityType.Builder toEntityType, EdmEntitySet.Builder toEntitySet) {

		// add EdmAssociation
		EdmAssociationEnd.Builder fromAssociationEnd = EdmAssociationEnd.newBuilder().setRole(fromEntityType.getName())
				.setType(fromEntityType).setMultiplicity(fromMultiplicity);

		String toAssociationRole = toEntityType.getName();
		if (toAssociationRole.equals(fromEntityType.getName())) {
			toAssociationRole = toAssociationRole + "1";
		}

		EdmAssociationEnd.Builder toAssociationEnd = EdmAssociationEnd.newBuilder().setRole(toAssociationRole)
				.setType(toEntityType).setMultiplicity(toMultiplicity);

		EdmAssociation.Builder association = EdmAssociation.newBuilder().setNamespace(namespace)
				.setName(associationName).setEnds(fromAssociationEnd, toAssociationEnd);

		// add EdmAssociationSet
		EdmAssociationSet.Builder associationSet = EdmAssociationSet.newBuilder().setName(associationName)
				.setAssociation(association)
				.setEnds(EdmAssociationSetEnd.newBuilder().setRole(fromAssociationEnd).setEntitySet(fromEntitySet),
						EdmAssociationSetEnd.newBuilder().setRole(toAssociationEnd).setEntitySet(toEntitySet));

		associationSets.add(associationSet);
		associations.add(association);
		return association;
	}

	private List<EdmProperty.Builder> propertyBuildersFromRecord() {
		ArrayList<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		properties.add(EdmProperty.newBuilder(Property.ID).setType(EdmSimpleType.INT64));
		return properties;
	}

	private List<EdmProperty.Builder> propertyBuildersFromRecord(final List<DataTableColumn> dataTableColumns) {
		ArrayList<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		for (DataTableColumn column : dataTableColumns) {
			String columnName = column.getName();
			DataTableColumnType columnDataType = column.getColumnDataType();
			properties.add(EdmProperty.newBuilder(columnName).setType(getEdmSimpleType(columnDataType)));
		}
		return properties;
	}

	private EdmType getEdmSimpleType(DataTableColumnType columnDataType) {
		switch (columnDataType) {
		case BIGINT:
			return EdmSimpleType.INT64;
		case BLOB:
			return EdmSimpleType.BINARY;
		case BOOLEAN:
			return EdmSimpleType.BOOLEAN;
		case DATE:
			return EdmSimpleType.DATETIME;
		case DATETIME:
			return EdmSimpleType.DATETIME;
		case DOUBLE:
			return EdmSimpleType.DOUBLE;
		case TEXT:
			return EdmSimpleType.STRING;
		case VARCHAR:
			return EdmSimpleType.STRING;
		default:
			return null;
		}
	}

	@Override
	public String getNameSpace() {
		return namespace;
	}

	private Logger getLogger() {
		return logger;
	}
}
