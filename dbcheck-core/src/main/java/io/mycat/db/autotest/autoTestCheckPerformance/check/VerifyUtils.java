package io.mycat.db.autotest.autoTestCheckPerformance.check;

import io.mycat.db.autotest.autoTestCheckPerformance.check.vo.CheckMsg;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.usecase.UseCase;
import io.mycat.db.autotest.bean.testgroup.usecase.Verify;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.*;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by qiank on 2017/2/3.
 */
public class VerifyUtils {

    // 验证结果并返回验证消息情况
    public static boolean check(Verify verify, UseCase useCase, Connection conn, String sqlStr) throws IOException {

        List<Map<String, String>> verifyCheckDatas = null;

        switch (verify.getVerifyCheckfileType()) {
            case "excel":// 以excel 模式获取比较数据
                ExcelReader excelReader = new ExcelReader();
                verifyCheckDatas = excelReader.readExcelContent(new File(PathUtils.getPath(useCase.getPath(), verify.getVerifyCheckfile())));

        }

        DBHelper dbHelper = null;
        List<Map<String, String>> verifyDatas = null;
        long time = 0;
        try {
            dbHelper = new DBHelper(conn);
            long start = System.currentTimeMillis();
            verifyDatas = dbHelper.queryForListMap(sqlStr);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (SQLException e) {
            LogFrameFile.getInstance().error("", e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
        AutoTestBaseBean stb = BeanFactory.getBeanById(useCase.getParentId());
        String path = "groupUseCase/" + stb.getId() + "/" + useCase.getId() + "/" + verify.getId() + ".html";
        if(verifyCheckDatas == null || verifyDatas == null){
            getCheckMsg(verify, useCase, time, new HashMap<>(), path, true);
            return false;
        }

        Map<String, Object> datas = new HashMap<>();
        List<Map<String, String>> verifyCheckDatas2 = new ArrayList<>();
        for (Map<String, String> verifyCheckData : verifyCheckDatas) {
            verifyCheckDatas2.add(new TreeMap<>(verifyCheckData));
        }
        datas.put("verifyCheckDatas", verifyCheckDatas2);
        List<Map<String, String>> verifyDatas2 = new ArrayList<>();
        for (Map<String, String> verifyData : verifyDatas) {
            verifyDatas2.add(new TreeMap<>(verifyData));
        }
        datas.put("verifyDatas", verifyDatas2);
        datas.put("verify", verify);

        if (verify.isVerifyOrder()) {
            //  HashMap
            if (equals(verifyCheckDatas,verifyDatas)) {

                getCheckMsg(verify, useCase, time, datas, path, true);
                return true;
            }
        } else {
            List<Map<String, String>> verifyCheckDatasTemp = BeanUtils.cloneTo(verifyCheckDatas);
            List<Map<String, String>> verifyDatasTemp = BeanUtils.cloneTo(verifyDatas);
            Collections.sort(verifyCheckDatasTemp, new MapComparator());
            Collections.sort(verifyDatasTemp, new MapComparator());
            if (equals(verifyCheckDatasTemp,verifyDatasTemp)) {
                getCheckMsg(verify, useCase, time, datas, path, true);
                return true;
            }

        }
        useCase.setStauts(false);
        getCheckMsg(verify, useCase, time, datas, path, false);
        return false;
    }

    private static boolean equals(List<Map<String, String>> verifyCheckDatas, List<Map<String, String>> verifyDatas){
        if(verifyCheckDatas == null || verifyDatas == null){
            return false;
        }
        for (int i = 0; i < verifyCheckDatas.size(); i++) {
            Map<String, String> verifyCheckData = verifyCheckDatas.get(i);
            Map<String, String> verifyData = verifyDatas.get(i);
            for (Map.Entry<String, String> stringStringEntry : verifyCheckData.entrySet()) {
                if(stringStringEntry.getValue() != null && verifyData.get(stringStringEntry.getKey()) != null){
                    if(!stringStringEntry.getValue().equals(verifyData.get(stringStringEntry.getKey()))){
                        return false;
                    }
                }else if(stringStringEntry.getValue() == null && verifyData.get(stringStringEntry.getKey()) == null){

                }else{
                    return false;
                }
            }
        }
        return true;
    }

    private static void getCheckMsg(Verify verify, UseCase useCase, long time, Map<String, Object> datas, String path, boolean stauts) {
        CheckMsg cm = new CheckMsg(verify.getId(), verify.getName(), "", path, time, stauts);
        useCase.addCheck(cm);
        try {
            datas.put("checkMsg", cm);
            createHtml(1, datas, path);
        } catch (UnsupportedEncodingException e) {
            LogFrameFile.getInstance().error("", e);
        }
    }

    //List<Map<String,String>>
    private static boolean createHtml(int type, Map<String, Object> datas, String path) throws UnsupportedEncodingException {
        ProjectConfig projectConfig = BeanFactory.getProjectConfig();
        String outPath = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
        String templateid = "verifyChekcInfo.html";
        if (2 == type) {
            templateid = "verifyExceptionInfo.html";
        }
        BeetlUtils.createpathTemplate(outPath + "/" + path, templateid, datas);
        return true;
    }

    static class MapComparator implements Comparator<Map<String, String>> {

        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            for (String s : o1.keySet()) {
                if (o1.get(s).compareTo(o2.get(s)) != 0) {
                    return o1.get(s).compareTo(o2.get(s));
                }
            }
            return 0;
        }
    }

}
