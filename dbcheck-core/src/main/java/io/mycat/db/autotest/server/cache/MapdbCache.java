package io.mycat.db.autotest.server.cache;


import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by qiank on 2017/3/1.
 */
public class MapdbCache {

    private static org.mapdb.DB db = null;

    static {
       String path = System.getProperty("user.dir");
       db = DBMaker.fileDB(new File(path+"/"+"mapdbCache.db")).closeOnJvmShutdown().checksumHeaderBypass().make();
       db.hashMap("useCaseConfigStatusCache").createOrOpen();
       db.hashMap("useCaseConfigCache").createOrOpen();

    }

    public static void setCaseConfigStatusCache(String uid,long time){
        ConcurrentMap primary = db.hashMap("useCaseConfigStatusCache").open();
        primary.put(uid,time);
    }

    public static boolean isCaseConfigStatusCache(String uid,long timenow){
        ConcurrentMap primary = db.hashMap("useCaseConfigStatusCache").open();
        Object obj = primary.get(uid);
        if(obj == null){
            return false;
        }
        long time = (Long)obj;
        if(timenow == time){
            return true;
        }
        return  false;
    }

    public static <T extends Serializable> T getUseCaseConfigCache(String key){
        ConcurrentMap primary = db.hashMap("useCaseConfigCache").open();
        return (T)primary.get(key);
    }

    public static <T extends Serializable> void setUseCaseConfigCache(String key,T data){
        ConcurrentMap primary = db.hashMap("useCaseConfigCache").open();
        primary.put(key,data);
    }

    public static void clearFile(){
        db.close();
        String path = System.getProperty("user.dir");
        System.out.println(new File(path+"/"+"mapdbCache.db").delete());
    }
}
