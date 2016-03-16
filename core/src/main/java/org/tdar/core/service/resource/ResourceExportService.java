package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.SerializationService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.PersistableUtils;

import com.google.common.base.Objects;

@Service
public class ResourceExportService {

	private static final String ZIP = ".zip";
	private static final String EXPORT = "export";
	private static final String RESOURCE_XML = "resource.xml";
	private static final String UPLOADED = "files/";
	private static final String ARCHIVAL = "archival/";
	private static final String PROJECT_XML = "project.xml";
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();
	@Autowired
	private GenericDao genericDao;

	@Autowired
	private SerializationService serializationService;

	@Transactional(readOnly = true)
	public File export(final Resource... resources) throws Exception {
		String edir = EXPORT + System.currentTimeMillis();
		File dir = new File(FileUtils.getTempDirectory(), edir);
		dir.mkdir();
		File zipFile = File.createTempFile(EXPORT, ZIP);
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
		File schema = serializationService.generateSchema();
		writeToZip(zout, schema, "tdar.xsd");
		for (Resource r : resources) {
			String base = String.format("%s/%s/", r.getResourceType(), r.getId());
			if (r instanceof InformationResource) {
				InformationResource ir = ((InformationResource) r);
				if (((InformationResource) r).getProject() != Project.NULL) {
					Project p = setupResourceForExport(ir.getProject());
					File file = writeToFile(dir, p, PROJECT_XML);
					writeToZip(zout, file, base + PROJECT_XML);
				}
				for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
					InformationResourceFileVersion uploaded = irf.getLatestUploadedVersion();
					InformationResourceFileVersion archival = irf.getLatestUploadedVersion();
					File retrieveFile = FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, uploaded);
					writeToZip(zout, retrieveFile, base + UPLOADED + retrieveFile.getName());
					if (archival != null && Objects.equal(archival, uploaded)) {
						File archicalFile = FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, archival);
						writeToZip(zout, archicalFile, base + ARCHIVAL + retrieveFile.getName());
					}
				}
			}
			Resource r_ = setupResourceForExport(r);
			File file = writeToFile(dir, r_, RESOURCE_XML);
			writeToZip(zout, file, base + RESOURCE_XML);
		}
		IOUtils.closeQuietly(zout);
		return zipFile;
	}

	private void writeToZip(ZipOutputStream zout, File file, String name) throws IOException, FileNotFoundException {
		ZipEntry zentry = new ZipEntry(name);
		zout.putNextEntry(zentry);
		logger.debug("adding to archive: {}", name);
		FileInputStream fin = new FileInputStream(file);
		IOUtils.copy(fin, zout);
		IOUtils.closeQuietly(fin);
		zout.closeEntry();
	}

	@Transactional(readOnly = true)
	private File writeToFile(File dir, Resource resource, String filename) throws Exception {
		String convertToXML = serializationService.convertToXML(resource);
		File type = new File("target/export/" + resource.getResourceType().name());
		File file = new File(dir, filename);

		FileUtils.writeStringToFile(file, convertToXML);
		return file;
	}

	@Transactional(readOnly = true)
	protected <R extends Resource> R setupResourceForExport(final R resource) {
		genericDao.markReadOnly(resource);

		Long id = resource.getId();
		if (id == null) {
			logger.debug("ID NULL: {}", resource);
		}

		for (Keyword kwd : resource.getAllActiveKeywords()) {
			clearId(kwd);
			if (kwd instanceof HierarchicalKeyword) {
				((HierarchicalKeyword) kwd).setParent(null);
			}
		}

		for (ResourceCreator rc : resource.getResourceCreators()) {
			clearId(rc);
			nullifyCreator(rc.getCreator());
		}

		// remove internal
		resource.getResourceCollections().removeIf(rc -> rc.isInternal());
		resource.getLatitudeLongitudeBoxes().forEach(llb -> clearId(llb));
		resource.getResourceCollections().forEach(rc -> {
			clearId(rc);
			rc.setResourceIds(null);
			rc.getResources().clear();
		});

		resource.getActiveRelatedComparativeCollections().forEach(cc -> clearId(cc));
		resource.getActiveSourceCollections().forEach(cc -> clearId(cc));
		resource.getActiveCoverageDates().forEach(cd -> clearId(cd));
		resource.getResourceNotes().forEach(rn -> clearId(rn));

		resource.getResourceAnnotations().forEach(ra -> {
			clearId(ra);
			clearId(ra.getResourceAnnotationKey());
		});

		resource.getResourceAnnotations()
				.add(new ResourceAnnotation(new ResourceAnnotationKey("TDAR ID"), id.toString()));

		if (resource instanceof InformationResource) {
			InformationResource ir = (InformationResource) resource;
			nullifyCreator(ir.getPublisher());
			nullifyCreator(ir.getResourceProviderInstitution());
			nullifyCreator(ir.getCopyrightHolder());

			List<FileProxy> proxies = new ArrayList<>();
			for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
				if (irf.isDeleted()) {
					continue;
				}
				proxies.add(new FileProxy(irf));
			}
			ir.getInformationResourceFiles().clear();
			ir.setFileProxies(proxies);

			if (PersistableUtils.isNotNullOrTransient(ir.getProjectId())) {
				ir.setProject(new Project(ir.getProjectId(), null));
				ir.setMappedDataKeyColumn(null);
			}

			if (resource instanceof Dataset) {
				Dataset dataset = (Dataset) resource;
				dataset.setDataTables(null);
				dataset.setRelationships(null);
			}

			if (resource instanceof CodingSheet) {
				CodingSheet codingSheet = (CodingSheet) resource;
				codingSheet.setCodingRules(null);
				codingSheet.setAssociatedDataTableColumns(null);
				codingSheet.setDefaultOntology(null);
			}

			if (resource instanceof Ontology) {
				((Ontology) resource).setOntologyNodes(null);
			}

		}

		resource.setId(null);
		return resource;
	}

	public void clearId(Persistable p) {
		genericDao.markReadOnly(p);
		p.setId(null);

	}

	private void nullifyCreator(Creator creator) {
		if (creator == null) {
			return;
		}
		clearId(creator);
		if (creator instanceof Person) {
			Person person = (Person) creator;
			if (person.getInstitution() != null) {
				clearId(person.getInstitution());
			}
		}
	}

}
