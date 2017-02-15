package io.mycat.db.autotest.parsing;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class UseCaseParsing {

    //projectConfig
    public static String analysis(String path,String parentId,String name,String pathDir) throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, InvocationTargetException, NoSuchMethodException {
        return analysis(new File(path),parentId,name,pathDir);
    }

    //projectConfig
    public static String analysis(File file,String parentId,String name,String pathDir) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, SAXException, InvocationTargetException, NoSuchMethodException {
        Map<String,String> attrs = new HashMap<>();
        attrs.put("parentId",parentId);
        attrs.put("name",name);
        attrs.put("path",pathDir);// 使用相对路径
        return DefaultXmlAnalysis.analysis(file,"useCase",attrs);
    }

}
