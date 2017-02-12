package io.mycat.db.autotest.bean.testgroup;

import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.Connection;
import io.mycat.db.autotest.bean.annotation.FieldType;

public class Config extends AutoTestBaseBean {

	private String name;
	
	private boolean sync = false;
	
	@FieldType(childName="connection",childType=Connection.class)
	private List<Connection> connections;
	
	private String defualutConnection;

	public Config() {
		super(Arrays.asList("name","sync","defualutConnection","connections"),  "config", Arrays.asList(Connection.class));
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public String getDefualutConnection() {
		return defualutConnection;
	}

	public void setDefualutConnection(String defualutConnection) {
		this.defualutConnection = defualutConnection;
	}

	public boolean isSync() {
		return sync;
	}
}
