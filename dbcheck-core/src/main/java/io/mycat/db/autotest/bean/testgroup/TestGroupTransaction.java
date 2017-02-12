package io.mycat.db.autotest.bean.testgroup;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.MultiNameBean;
import io.mycat.db.autotest.bean.testgroup.usecase.Transaction;

public class TestGroupTransaction extends AutoTestBaseBean implements MultiNameBean,TagServerType {

	public TestGroupTransaction() {
		super(Arrays.asList("transaction"),  "multiName", null);//如果为multiName取父方法的成员变量名
	}
	
	private Transaction transaction;

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}


	@Override
	public List<Class<? extends AutoTestBaseBean>> getMultiNameBean() {
		return Arrays.asList(TestGroupBaseBean.class);
	}

	@Override
	public boolean exec() throws Exception {

		if(transaction != null){
			transaction.exec();
		}

		return true;
	}
}
