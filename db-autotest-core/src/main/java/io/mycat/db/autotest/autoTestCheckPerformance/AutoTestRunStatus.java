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
public class AutoTestRunStatus {


    //所有用例的列表
    private static final List<String> useCaseAlls = new ArrayList<String>();

    //所有需完成的列表
    private static final List<String> useCaseList = new ArrayList<>();

    public static void addUseCaseAlls(String useCaseFinishId) {
        useCaseAlls.add(useCaseFinishId);
    }

    public static void addUseCaseList(String useCaseId) {
        useCaseList.add(useCaseId);
    }

    public static boolean isUseCaseAlls(String useCaseFinishId) {
        return useCaseAlls.contains(useCaseFinishId);
    }

    public static boolean isUseCaseList(String useCaseId) {
        return useCaseList.contains(useCaseId);
    }

    public static List<String> getUseCaseAlls() {
        return useCaseAlls;
    }

    public static List<String> getUseCaseList() {
        return useCaseList;
    }


    public static void clearUseCaseAlls() {
        useCaseAlls.clear();
    }

    public static void clearUseCaseList() {
        useCaseList.clear();
    }
}
