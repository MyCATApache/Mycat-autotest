package io.mycat.db.autotest.autoTestCheckPerformance.performance.exec;

import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.performance.PerformanceExec;

import java.util.List;

/**
 * Created by qiank on 2017/2/20.
 */
public class DefaultPerformanceExec implements PerformanceExec {

    private List<TestGroupBaseBean> testGroupBaseBeans;

    public DefaultPerformanceExec(List<TestGroupBaseBean> testGroupBaseBeans){
        this.testGroupBaseBeans = testGroupBaseBeans;
    }

    @Override
    public boolean exec() throws Exception {
        for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
            testGroupBaseBean.setType(2);
            testGroupBaseBean.exec();
        }
        return true;
    }
}
