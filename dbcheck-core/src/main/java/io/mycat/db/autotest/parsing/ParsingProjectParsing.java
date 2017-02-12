package io.mycat.db.autotest.parsing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.TypeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParsingProjectParsing {

    //projectConfig

    public static String analysis(String path) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, SAXException, InvocationTargetException {
        Map<String,String> datas = new HashMap<>();
        datas.put("path",path);
       return DefaultXmlAnalysis.analysis(path+"/config.xml","projectConfig",datas);
    }

    //projectConfig
    public static String analysis(File file) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, SAXException, InvocationTargetException {
        return DefaultXmlAnalysis.analysis(file,"projectConfig");
    }

}
