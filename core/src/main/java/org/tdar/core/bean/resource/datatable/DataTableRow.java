package org.tdar.core.bean.resource.datatable;

import java.util.Collections;
import java.util.List;

import org.tdar.core.bean.Indexable;

/**
 * Stand-in class for handling rows. Mostly used for Solr/search
 * 
 * @author abrin
 *
 */
public class DataTableRow implements Indexable {

	private static final long serialVersionUID = 1890861649805565275L;
	private Long id;

	@Override
	public void setId(Long number) {
		this.id = number;
	}

	@Override
	public List<?> getEqualityFields() {
		return Collections.emptyList();
	}

	@Override
	public Long getId() {
		return id;
	}

}
