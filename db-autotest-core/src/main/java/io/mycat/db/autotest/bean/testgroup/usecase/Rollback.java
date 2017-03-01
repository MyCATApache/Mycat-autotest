package io.mycat.db.autotest.bean.testgroup.usecase;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.server.ioc.BeanFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rollback extends AutoTestBaseBean  implements TagServerType {

	public Rollback() {
		super(Arrays.asList(),  "rollback",null);
	}


	@Override
	public boolean exec() throws SQLException {
		AutoTestBaseBean atbb = getPerformanceOrTransaction(this);
		if(atbb instanceof Transaction){
			Transaction transaction = (Transaction)atbb;
			transaction.getConnectionBase().rollback();
		}
		return false;
	}

	private AutoTestBaseBean getPerformanceOrTransaction(AutoTestBaseBean autoTestBaseBean){
		if(autoTestBaseBean instanceof Performance || autoTestBaseBean instanceof Transaction ){
			return autoTestBaseBean;
		}else{
			return getPerformanceOrTransaction(BeanFactory.getBeanById(autoTestBaseBean.getParentId()));
		}
	}
}
