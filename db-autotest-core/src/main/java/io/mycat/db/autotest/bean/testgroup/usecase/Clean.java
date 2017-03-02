package io.mycat.db.autotest.bean.testgroup.usecase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;

public class Clean extends AutoTestBaseBean  implements TagServerType {

	public Clean() {
		super(Arrays.asList(),  "clean", Arrays.asList(Sql.class));
	}
	
	@FieldStep(name={"sql","transaction"})
	private List<AutoTestBaseBean> fieldStep = new ArrayList<>();

	public List<AutoTestBaseBean> getFieldStep() {
		return fieldStep;
	}

	public void setFieldStep(List<AutoTestBaseBean> fieldStep) {
		this.fieldStep = fieldStep;
	}

	@Override
	public boolean exec() throws Exception {
		for (AutoTestBaseBean autoTestBaseBean : fieldStep) {
			if(autoTestBaseBean instanceof TagServerType){
				((TagServerType)autoTestBaseBean).exec();
			}
		}

		return true;
	}
}
