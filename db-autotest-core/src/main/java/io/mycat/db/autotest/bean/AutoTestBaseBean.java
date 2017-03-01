package io.mycat.db.autotest.bean;

import java.io.Serializable;
import java.util.List;

public abstract class AutoTestBaseBean implements Serializable {

	/**
	 * 本标签id
	 */
	private String id;

	/**
	 * 父标签id
	 */
	private String parentId;

	/**
	 *
	 */
	private List<String> fields ;

	private String name;

	/**
	 * 标签名称
	 */
	private String tagName;

	private String ref;
	
	private List<Class<? extends AutoTestBaseBean>> loadChildFields;
	
	
	public AutoTestBaseBean(List<String> fields, String tagName,List<Class<? extends AutoTestBaseBean>> loadChildFields) {
		this.fields = fields;
		this.tagName = tagName;
		this.loadChildFields = loadChildFields;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}


	public List<Class<? extends AutoTestBaseBean>> getLoadChildFields() {
		return loadChildFields;
	}

	public void setLoadChildFields(List<Class<? extends AutoTestBaseBean>> loadChildFields) {
		this.loadChildFields = loadChildFields;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getTagName() {
		return tagName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
