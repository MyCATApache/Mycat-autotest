package io.mycat.db.autotest.bean.testgroup.usecase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;

public class Transaction extends AutoTestBaseBean implements TagServerType {
	
	private boolean autoCommit = true;
	
	@FieldStep(name={"sql","verify","commit","rollback"})
	private List<AutoTestBaseBean> fieldStep = new ArrayList<>();

	private transient Connection connectionBase;
	
	public Transaction() {
		super(Arrays.asList("autoCommit","fieldStep"),  "transaction", Arrays.asList(Sql.class,Verify.class));
	}

	public Connection getConnectionBase() {
		return connectionBase;
	}

	public void setConnectionBase(Connection connectionBase) {
		this.connectionBase = connectionBase;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public List<AutoTestBaseBean> getFieldStep() {
		return fieldStep;
	}

	public void setFieldStep(List<AutoTestBaseBean> fieldStep) {
		this.fieldStep = fieldStep;
	}


	@Override
	public boolean exec() throws Exception {
		try {
			for (AutoTestBaseBean autoTestBaseBean : fieldStep) {
				if (autoTestBaseBean instanceof TagServerType) {
						((TagServerType) autoTestBaseBean).exec();
					}
				}

		}finally{
			if(connectionBase != null){
				connectionBase.close();
				connectionBase = null;
			}
		}

		return true;
	}
}
