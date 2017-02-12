package io.mycat.db.autotest.parsing;

import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.PathUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiank on 2017/1/25.
 */
public class ParsingAnalysisMain {

    private static List<String> getPaths(String path) throws UnsupportedEncodingException {
        File file = new File(path);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        List<String> ds = new ArrayList<>();
        if(files == null){
            return ds;
        }
        for (File file1 : files) {
            ds.add(URLDecoder.decode(file1.getPath(),"utf-8"));
        }
        return ds;
    }




    public static void analysis(String projectConfigPath) throws Exception{

            ParsingProjectParsing.analysis(projectConfigPath);
            ProjectConfig projectConfig = BeanFactory.getBeanByClass(ProjectConfig.class);

            if(projectConfig == null){
                return;
            }
            // 解析全局配置文件
            String path = projectConfig.getPath();
            List<String> paths = ParsingAnalysisMain.getPaths(PathUtils.getPath(projectConfigPath,path));
            for (String s : paths) {
                File file = new File(s,"initTestGroup.xml");
                if(file.exists()){
                    //解析用例组，
                    String id = TestGroupParsing.analysis(file,s.substring(s.lastIndexOf(File.separatorChar)+1),s);
                    List<String> paths2 = ParsingAnalysisMain.getPaths(s);
                    for (String s1 : paths2) {
                        File file2 = new File(s1,"useCase.xml");
                        if(file2.exists()){
                            // 解析用例
                            UseCaseParsing.analysis(file2,id,s1.substring(s1.lastIndexOf(File.separatorChar)+1),s1);
                        }
                    }
                }
            }

            //System.out.println(BeanFactory.getBeans());
            LogFrameFile.getInstance().debug("解析xml结束");


    }
}
