package org.tdar.search.service.index;

import java.io.File;
import java.io.Serializable;

import org.tdar.core.event.EventType;
import org.tdar.core.service.event.ObjectContainer;

public class SolrDocumentContainer extends ObjectContainer<File> implements Serializable  {

	private static final String INDEX = "INDEX";
	private static final long serialVersionUID = 5991246263392007178L;
	private String core;

	public SolrDocumentContainer(File doc, String generateId, EventType eventType, String core) {
		super(doc, generateId, eventType);
		this.setCore(core);
	}

	public String getCore() {
		return core;
	}

	public void setCore(String core) {
		this.core = core;
	}
	
	public String getType() {
		return INDEX;
	};
}
