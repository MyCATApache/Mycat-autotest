package io.mycat.db.autotest.bean.testgroup.usecase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.server.ioc.BeanFactory;

public class Performance extends AutoTestBaseBean  implements TagServerType {
	
	private String type = "datebaseDefualt";

	@FieldStep(name={"transaction","select"})
	private List<AutoTestBaseBean> fieldStep = new ArrayList<>();
	
	public Performance() {
		super(Arrays.asList("fieldStep"),  "performance", null);
	}

	public List<AutoTestBaseBean> getFieldStep() {
		return fieldStep;
	}

	public void setFieldStep(List<AutoTestBaseBean> fieldStep) {
		this.fieldStep = fieldStep;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
