package io.mycat.db.autotest.parsing;

import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.TypeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取配置文件数据核心类
 * xml 核心解析类
 */
public class DefaultXmlAnalysis {


    public static String analysis(String path,String tagName) throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, InvocationTargetException, NoSuchMethodException {
       return analysis(new File(path),tagName,new HashMap<>());
    }

    public static String analysis(File file,String tagName) throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, InvocationTargetException, NoSuchMethodException {
        return analysis(file,tagName,new HashMap<>());
    }

    public static String analysis(String path,String tagName,Map<String,String> attrs) throws ParserConfigurationException, IOException, SAXException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        return analysis(new File(path),tagName,attrs);
    }

    /**
     * 解析配置文件的标签，及标签的属性
     * @param f
     * @param tagName
     * @param attrs
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static String analysis(File f,String tagName,Map<String,String> attrs) throws ParserConfigurationException, IOException, SAXException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            NodeList nodelist = doc.getElementsByTagName(tagName);
            if (nodelist == null || nodelist.getLength() == 0) {
                throw new AutoTestException("未知的文件，无法找到"+tagName+"位置");
            }

            Node node = nodelist.item(0);
            Map<String, String> childTags = new HashMap<>();
            //List<Node> childNodes = new ArrayList<>();
            getNodeData(node, childTags,null);
            String projectConfigId = AutoTestBeanTagsEngine.analysis(node.getNodeName(), attrs,childTags);
            NodeList nodelistChildNodes = node.getChildNodes();
            if (nodelistChildNodes == null || nodelistChildNodes.getLength() == 0) {
                LogFrameFile.getInstance().warn(tagName+"中未配置任何内容");
                return null;
            }

            for (int i = 0; i < nodelistChildNodes.getLength(); i++) {
                Node nodeLevel1 = nodelistChildNodes.item(i);
                if (nodeLevel1.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName1 = nodeLevel1.getNodeName();
                    getObjSet(nodeName1,nodeLevel1,projectConfigId);
                }
            }

            return projectConfigId;
    }

    /**
     * 获取 当前节点的 属性 和 子标签类型的属性
     * @param node
     * @param childTags
     * @param childNodes
     */
    private static void getNodeData(Node node, Map<String, String> childTags,  List<Node> childNodes ) {
        NodeList nodelistChildNodes3 = node.getChildNodes();
        for (int m = 0; m < nodelistChildNodes3.getLength(); m++) {
            Node nodeLevel3 = nodelistChildNodes3.item(m);
            if (nodeLevel3.getNodeType() == Node.ELEMENT_NODE) {
                if (!AutoTestBeanTagsEngine.isTag(nodeLevel3.getNodeName())) {
                    boolean flag = true;
                    NodeList nodelistChildNodes4  = nodeLevel3.getChildNodes();
                    //如过子标签，下无元素标签，就表示当前标签可能是父表的属性
                    for (int n = 0; n < nodelistChildNodes4.getLength(); n++) {
                        if (nodelistChildNodes4.item(n).getNodeType() == Node.ELEMENT_NODE) {
                            flag = false;
                        }
                    }
                    if(flag && nodeLevel3.getFirstChild() != null){
                        childTags.put(nodeLevel3.getNodeName(), nodeLevel3.getFirstChild().getTextContent());
                    }
                } else {
                    if(childNodes != null){
                        childNodes.add(nodeLevel3);
                    }

                }
            }
        }
    }

    private static void getObjSet(String nodeName1, Node nodeLevel1, String id) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        // 解析集合类型属性
        if (TypeUtils.isTypeSet(nodeName1)) {
            NodeList nodelistChildNodes2 = nodeLevel1.getChildNodes();
            for (int j = 0; j < nodelistChildNodes2.getLength(); j++) {
                Node nodeLevel2 = nodelistChildNodes2.item(j);
                if (nodeLevel2.getNodeType() == Node.ELEMENT_NODE) {
                    simpleAnalyticObject(id, nodeLevel2);
                }
            }
        } else {
            simpleAnalyticObject(id, nodeLevel1);

        }
    }

    //递归解析标签
    private static void simpleAnalyticObject(String id,
                                             Node nodeLevel2) throws InstantiationException,
            IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        NamedNodeMap nnm = nodeLevel2.getAttributes();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("parentId", id);
        for (int k = 0; k < nnm.getLength(); k++) {
            attrs.put(nnm.item(k).getNodeName(), nnm.item(k).getNodeValue());
        }

        Map<String, String> childTags = new HashMap<>();
        List<Node> childNodes = new ArrayList<>();
        getNodeData(nodeLevel2,childTags,childNodes);

        String nid = AutoTestBeanTagsEngine.analysis(nodeLevel2.getNodeName(), attrs, childTags);
        if (!childNodes.isEmpty()) {
            for (Node childNode : childNodes) {
                getObjSet(childNode.getNodeName(), childNode, nid);
            }
        }
    }

}
