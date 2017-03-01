package io.mycat.db.autotest.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by qiank on 2017/1/24.
 */
public class TypeUtils {

    private static final List<String> typeSets = Arrays.asList("connections");

    /**
     * 如果为true本标签为集合标签，需要继续向下解析
     * @param name
     * @return
     */
    public static boolean isTypeSet(String name){
        if(typeSets.contains(name)){
            return true;
        }
        return false;
    }
}
