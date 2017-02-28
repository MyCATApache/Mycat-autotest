package io.mycat.db.autotest.autoTestCheckPerformance.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.utils.LogFrameFile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by qiank on 2017/2/28.
 */
public class XmlUtils {

    public static List<Map<String, String>> readXmlContent(File file) {
        InputStream in = null;
        try {
            // SAXReader reader = new SAXReader();
            //InputStream in = new FileInputStream(file);
            //Document doc = reader.read(in);
            //List<DefaultElement> list = (List<DefaultElement>)doc.selectNodes(xpath);


            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            in = new FileInputStream(file);
            Document doc = db.parse(in);

            // 创建XPath对象
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpathF = factory.newXPath();

            Node node = (Node) xpathF.evaluate("/data/fields", doc, XPathConstants.NODE);

            if (node == null) {
                throw new AutoTestException("缺少字段描述");
            }
            NodeList nodelistChildNodes = node.getChildNodes();
            if (nodelistChildNodes == null || nodelistChildNodes.getLength() == 0) {
                throw new AutoTestException("缺少字段描述");
            }

            List<String> fields = new ArrayList<>();
            for (int i = 0; i < nodelistChildNodes.getLength(); i++) {
                Node nodeLevel1 = nodelistChildNodes.item(i);
                if (nodeLevel1.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName1 = nodeLevel1.getNodeName();
                    fields.add(nodeName1);
                }
            }

            NodeList nodeList = (NodeList) xpathF.evaluate("/data/list/vo", doc,
                    XPathConstants.NODESET);
            List<Map<String, String>> datas = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nodeLevel1 = nodeList.item(i);
                NodeList nodeList2 = nodeLevel1.getChildNodes();
                Map<String, String> data = new HashMap<>();
                int index = 0;
                for (int j = 0; j < nodeList2.getLength(); j++) {
                    Node nodeLevel2 = nodeList2.item(j);
                    if (nodeLevel2.getNodeType() == Node.ELEMENT_NODE) {
                        data.put(fields.get(index), nodeLevel2.getFirstChild().getTextContent());
                        index++;
                    }
                }
                datas.add(data);
            }
            return datas;
        } catch (Exception e) {
            throw new AutoTestException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LogFrameFile.getInstance().error("", e);
                }
            }
        }
    }

}
