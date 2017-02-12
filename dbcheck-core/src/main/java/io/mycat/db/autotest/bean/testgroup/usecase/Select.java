package io.mycat.db.autotest.bean.testgroup.usecase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.server.ioc.BeanFactory;

public class Select extends AutoTestBaseBean  implements TagServerType {
	
	
	@FieldStep(name={"sql","verify"})
	private List<AutoTestBaseBean> fieldStep = new ArrayList<>();
	
	public Select() {
		super(Arrays.asList("fieldStep"),  "select", Arrays.asList(Sql.class,Verify.class));
	}

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
