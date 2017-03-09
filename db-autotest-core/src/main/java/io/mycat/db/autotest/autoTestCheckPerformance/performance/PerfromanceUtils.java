package io.mycat.db.autotest.autoTestCheckPerformance.performance;

import io.mycat.db.autotest.autoTestCheckPerformance.check.VerifyUtils;
import io.mycat.db.autotest.autoTestCheckPerformance.check.vo.CheckMsg;
import io.mycat.db.autotest.autoTestCheckPerformance.performance.vo.PerfromanceMsg;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.usecase.Performance;
import io.mycat.db.autotest.bean.testgroup.usecase.UseCase;
import io.mycat.db.autotest.bean.testgroup.usecase.Verify;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.*;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by qiank on 2017/2/5.
 */
public class PerfromanceUtils {

    // 验证结果并返回验证消息情况
    public static long check(int count, Performance performance, UseCase useCase, Connection conn, String sqlStr) throws Exception {

        long time = 0;
        int length = count;
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            long start = System.currentTimeMillis();
            DataBaseUtils.execSql(conn,sqlStr);
            long end = System.currentTimeMillis();
            times.add(end - start);
            time = time + end - start;
        }
        List<Long> times2 = new ArrayList<>();
        times2.addAll(times);
        Collections.sort(times2);
        long average = time / count;

        PerfromanceMsg pm = new PerfromanceMsg(performance.getId(),performance.getName(),useCase.getId()+"/"+performance.getId()+".html",times,average,times2.get(0),times2.get(times2.size()-1));
        useCase.addPerfromance(pm);

        Map<String, Object> datas = new HashMap<>();
        datas.put("verifyCheckDatas", performance);
        AutoTestBaseBean stb = BeanFactory.getBeanById(useCase.getParentId());
        String path = "groupUseCase/" + stb.getId() + "/" + useCase.getId() + "/" + performance.getId() + ".html";
        datas.put("perfromanceMsg", pm);
        createHtml(datas, path);

        return time;
    }

    //List<Map<String,String>>
    private static boolean createHtml( Map<String, Object> datas, String path) throws Exception {
        ProjectConfig projectConfig = BeanFactory.getProjectConfig();
        String outPath = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
        String templateid = "perfromanceInfo.html";
        BeetlUtils.createpathTemplate(outPath + "/" + path, templateid, datas);
        return true;
    }

}
