package org.tdar.core.service.event;

import java.io.File;

import org.tdar.core.event.EventType;
import org.tdar.filestore.FilestoreObjectType;

/**
 * Default ObjectContainer tracking a file that needs to be logged in the filestore
 * @author abrin
 *
 */
public class LoggingObjectContainer extends ObjectContainer<File> {

	private FilestoreObjectType filestoreObjectType;
	private Long persistableId;

	public LoggingObjectContainer(File doc, String id, EventType type, FilestoreObjectType filestoreObjectType, Long long1) {
		super(doc, id, type);
		this.setPersistableId(long1);
		this.setFilestoreObjectType(filestoreObjectType);
	}

	@Override
	public String getType() {
		return "XML";
	}

	public FilestoreObjectType getFilestoreObjectType() {
		return filestoreObjectType;
	}

	public void setFilestoreObjectType(FilestoreObjectType filestoreObjectType) {
		this.filestoreObjectType = filestoreObjectType;
	}

	public Long getPersistableId() {
		return persistableId;
	}

	public void setPersistableId(Long persistableId) {
		this.persistableId = persistableId;
	}
}
