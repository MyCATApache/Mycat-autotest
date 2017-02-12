package io.mycat.db.autotest.parsing;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class TestGroupParsing {

    //projectConfig
    public static String analysis(String path,String name,String pathDir) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, SAXException, InvocationTargetException {
        return analysis(new File(path),name,pathDir);
    }

    //projectConfig
    public static String analysis(File file,String name,String pathDir) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, SAXException, InvocationTargetException {
        Map<String,String> attrs = new HashMap<>();
        attrs.put("name",name);
        attrs.put("path",pathDir);
        return DefaultXmlAnalysis.analysis(file, "testGroup",attrs);
    }

}
