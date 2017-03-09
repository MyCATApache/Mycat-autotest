package io.mycat.db.autotest.utils;

import io.mycat.db.autotest.server.AutoTestServer;
import org.apache.commons.io.IOUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by zejian on 2015/7/15 0015.
 */
public class BeetlUtils  {



    /*public static String caeateTemplate(String templateid,Map obj){
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("template/");
        Configuration cfg = null;
        try {
            cfg = Configuration.defaultConfiguration();
        } catch (IOException e) {
            LogFrameFile.getInstance().error("",e);
        }
        GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
        Template t = gt.getTemplate(templateid);
        t.binding(obj);
        String str = t.render();
       // System.out.println(str);
        return str;
    }*/

    public static void caeateTemplate(String templateid,Map obj,File file)throws Exception{

            URL url = AutoTestServer.class.getClassLoader().getResource("");

            FileOutputStream file2 = null;
            try{
                String path = null;
                if(AutoTestServer.isJar){
                    path = System.getProperty("user.dir");
                }else{
                    path = url.getPath();
                }

                FileResourceLoader resourceLoader = new FileResourceLoader(URLDecoder.decode(path,"utf-8")+"/"+"template2/template","utf-8");
                //ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("template/");

                Configuration cfg = Configuration.defaultConfiguration();
                GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
                Template t = gt.getTemplate("/"+templateid);
                t.binding(obj);
                file2 = new FileOutputStream(file);
                t.renderTo(file2);
            }finally {
                IOUtils.closeQuietly(file2);
            }

    }

    public static void createpathTemplate(String outpath,String packages,String filename,String templateid,Map obj)throws Exception{

        packages = packages.replaceAll("[.]", "/");
         File parent = new File(outpath);
        File file =  new File(parent,packages);
        if(!file.exists()){
            file.mkdirs();
        }
        file =  new File(file,filename);
        caeateTemplate(templateid, obj,file);
    }


    public static void createpathTemplate(String path,String templateid,Map obj)throws Exception{

        //packages = packages.replaceAll("[.]", "/");
        File file = new File(path);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        caeateTemplate(templateid, obj,file);
    }

}
