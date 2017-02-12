package io.mycat.db.autotest.autoTestCheckPerformance;

import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.server.ioc.BeanFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by qiank on 2017/1/27.
 */
public class AutoTestRun {

    //已完成的性能测试用列
    private static final Collection<String> preformanceFinishs = new java.util.concurrent.ConcurrentLinkedQueue<String>();



    //所有需完成的性能测试列表
    private static final List<String> preformanceList = new ArrayList<>();

    //已完成的用列
    private static final Collection<String> checkFinishs = new java.util.concurrent.ConcurrentLinkedQueue<String>();

    //所有需完成的列表
    private static final List<String> checkList = new ArrayList<>();


    public static void addCheckFinishs(List<String> checks){
        checkFinishs.addAll(checks);
    }

    public static void addCheckFinish(String check){
        checkFinishs.add(check);
    }

    public static void addCheckLists(List<String> checks){
        checkList.addAll(checks);
    }

    public static void addcheckList(String check){
        checkList.add(check);
    }

    public static void checkRun(String names){
        try {
            ProjectConfig pc =  BeanFactory.getProjectConfig();
            if(names != null){
                String[] namestrs = names.split(",");
            }else{
                Set<TestGroupBaseBean> groupBases = BeanFactory.getGroupBases();
                for (TestGroupBaseBean groupBasis : groupBases) {
                    groupBasis.exec();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
