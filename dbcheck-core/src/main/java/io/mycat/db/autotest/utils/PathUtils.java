package io.mycat.db.autotest.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by qiank on 2017/2/3.
 */
public class PathUtils {


    public static String getPath(String loadPath,String path) throws UnsupportedEncodingException {
        if(path.startsWith("./") || path.startsWith("../")){
            return URLDecoder.decode(new File(loadPath,path).getPath(),"utf-8");
        }else{
            return URLDecoder.decode(new File(path).getPath(),"utf-8");
        }
    }
}
