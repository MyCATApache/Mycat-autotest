package io.mycat.db.autotest.parsing;

import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by qiank on 2017/1/25.
 */
public class ParsingAnalysisMain {

    private static List<File> getFiles(String path) throws UnsupportedEncodingException {
        File file = new File(path);
        return getFiles(file);
    }

    private static List<File> getFiles(File file) throws UnsupportedEncodingException {
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if(files == null){
            return new ArrayList<>();
        }
        return  Arrays.asList(files);
    }


    /**
     * 解析配置文件，控制器
     * @param projectConfigPath
     * @throws Exception
     */
    public static void analysis(String projectConfigPath) throws Exception{

            ParsingProjectParsing.analysis(projectConfigPath);
            ProjectConfig projectConfig = BeanFactory.getBeanByClass(ProjectConfig.class);
            if(projectConfig == null){
                return;
            }
            // 解析全局配置文件
            String path = projectConfig.getPath();
            List<File> paths = ParsingAnalysisMain.getFiles(PathUtils.getPath(projectConfigPath,path));
            for (File s : paths) {
                File file = new File(s,"initTestGroup.xml");
                if(file.exists()){
                    //解析用例组，
                    String id = TestGroupParsing.analysis(file,s.getName(),s.getPath());
                    List<File> paths2 = ParsingAnalysisMain.getFiles(s);
                    for (File s1 : paths2) {
                        File file2 = new File(s1,"useCase.xml");
                        if(file2.exists()){
                            // 解析用例
                            UseCaseParsing.analysis(file2,id,s1.getName(),s1.getName());
                        }
                    }
                }
            }
            AutoTestBeanTagsEngine.loadRef();
            //System.out.println(BeanFactory.getBeans());
            LogFrameFile.getInstance().debug("解析xml结束");


    }
}
