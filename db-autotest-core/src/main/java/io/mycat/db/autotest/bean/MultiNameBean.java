package io.mycat.db.autotest.bean;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by qiank on 2017/1/25.
 */
public interface MultiNameBean {


    /**
     * 具有该对象属性的 class 只解析当前一层
     * @return
     */
    List<Class<? extends AutoTestBaseBean>> getMultiNameBean();
}
