package io.mycat.db.autotest.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by qiank on 2017/2/1.
 */
public class FileSqlUtils {


    public static List<String> getSqls(File file) throws IOException {
        List<String> sqls = FileUtils.readLines(file,"utf-8");
        //String[] sts =sqls.split("31;");
        return getSqls(sqls);
    }

    public static List<String> getSqls(String sql) {
        return getSqls(Arrays.asList(sql.split("\n")));
    }

    public static List<String> getSqls(List<String> sqls) {
        List<String> ls = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String arg : sqls) {
            String temparg = arg;
            temparg = temparg.trim();
            if(temparg.endsWith(";")){
                sb.append(arg.substring(0,arg.lastIndexOf(";")));
                ls.add(sb.toString());
                sb = new StringBuilder();
            }else{
                sb.append(arg);
            }
        }
        if(sb.length() != 0){
            ls.add(sb.toString());
            sb = new StringBuilder();
        }
        return ls;
    }

}
