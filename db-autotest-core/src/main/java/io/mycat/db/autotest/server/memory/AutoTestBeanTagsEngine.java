package io.mycat.db.autotest.server.memory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.MultiNameBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.bean.testgroup.usecase.Check;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.exception.MethodParmsConvertException;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.BeanUtils;
import io.mycat.db.autotest.utils.ClassUtils;
import io.mycat.db.autotest.utils.DateUtil;
import io.mycat.db.autotest.utils.LogFrameFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * 一种特殊的设计，使配置文件和对象达到最大的分离
 *
 * @author zjliu qiankunshe@126.com
 * @ClassName: AutoTestBeanTagsMemory
 * @Description: 缓存配置标签配置信息，并解析标签
 * @date 2017年1月22日 上午10:18:45
 */
public class AutoTestBeanTagsEngine {

    /**
     * 缓存所有的标签
     */
    private static final List<AutoTestBaseBean> objTags = new ArrayList<>();

    private static final ConcurrentLinkedDeque<AutoTestBaseBean> lazyBeans = new ConcurrentLinkedDeque<>();

	/*public static void addObjtags(List<AutoTestBaseBean> tags) {
        objTags.addAll(tags);
	}

	public static List<AutoTestBaseBean> getObjtags() {
		return objTags;
	}*/

    /**
     * 加载标签对应的对象
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static void loadTagClass() throws InstantiationException, IllegalAccessException, NoSuchMethodException {
        Set<Class<?>> cls = ClassUtils.getClasses("io.mycat.db.autotest.bean");
        List<AutoTestBaseBean> objTag = new ArrayList<>();
        Map<String, Map<String, BeanUtils.AutoTestBeanTagsBean>> autoTestBeanTagsBeanMap = new HashMap<>();
        for (Class<?> class1 : cls) {
            if (!class1.isAnnotation() && !Modifier.isAbstract(class1.getModifiers())
                    && AutoTestBaseBean.class.isAssignableFrom(class1)) {
                AutoTestBaseBean autoTestBaseBean = (AutoTestBaseBean) class1.newInstance();
                String tagName = autoTestBaseBean.getTagName();

                Map<String, BeanUtils.AutoTestBeanTagsBean> autoTestBeanTagsBeans = BeanUtils.getAutoTestBeanTagsBean(
                        class1);
                if ("multiName".equals(tagName)) {
                    List<Class<? extends AutoTestBaseBean>> classes = ((MultiNameBean) autoTestBaseBean).getMultiNameBean();

                    for (Class<? extends AutoTestBaseBean> aClass : classes) {
                        Field[] fields = aClass.getDeclaredFields();
                        for (Field cc : fields) {
                            if (autoTestBaseBean.getClass().getName().equals(cc.getType().getName())) {
                                AutoTestBaseBean autoTestBaseBean2 = autoTestBaseBean.getClass().newInstance();
                                autoTestBaseBean2.setTagName(cc.getName());
                                objTag.add(autoTestBaseBean2);
                                autoTestBeanTagsBeanMap.put(cc.getName(), autoTestBeanTagsBeans);
                            }
                        }
                    }
                }
                objTag.add(autoTestBaseBean);
                autoTestBeanTagsBeanMap.put(autoTestBaseBean.getTagName(), autoTestBeanTagsBeans);
            }
        }
        objTags.addAll(objTag);
        BeanUtils.getAutoTestBeanTagsBeanMaps().putAll(autoTestBeanTagsBeanMap);
    }


    /**
     * @param tagName
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @Title: getObj
     * @Description: 根据标签返回对应的实现类
     */
    public static AutoTestBaseBean getObj(String tagName) throws InstantiationException, IllegalAccessException {
        for (AutoTestBaseBean autoTestBaseBean : objTags) {
            if (tagName.equals(autoTestBaseBean.getTagName())) {
                AutoTestBaseBean autoTestBaseBean2 = autoTestBaseBean.getClass().newInstance();
                autoTestBaseBean2.setTagName(autoTestBaseBean.getTagName());
                return autoTestBaseBean2;
            }
        }
        return null;
    }

    public static boolean isTag(String tagName) {
        for (AutoTestBaseBean autoTestBaseBean : objTags) {
            if (tagName.equals(autoTestBaseBean.getTagName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param tagName
     * @param attributes 除属性外，还包含父id
     * @param childTags  子标签，如果属性在 attributes和childTags 都存在只生效 attributes
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static String analysis(String tagName, Map<String, String> attributes, Map<String, String> childTags) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // 非tag时返回null
        if (!isTag(tagName)) {
            return null;
        }

        AutoTestBaseBean autoTestBaseBean = getObj(tagName);
        Map<String, BeanUtils.AutoTestBeanTagsBean> atbtbs = BeanUtils.getAutoTestBeanTagsBeanMaps().get(tagName);
        if (autoTestBaseBean == null) {
            return null;
        }

        //解析当前标签
        for (Map.Entry<String, BeanUtils.AutoTestBeanTagsBean> stringAutoTestBeanTagsBeanEntry : atbtbs.entrySet()) {
            BeanUtils.AutoTestBeanTagsBean autoTestBeanTagsBean = stringAutoTestBeanTagsBeanEntry.getValue();
            String str = attributes.get(autoTestBeanTagsBean.getFieldName());
            if (StringUtils.isNotEmpty(str)) {
                try {
                    autoTestBeanTagsBean.getSetMethod().invoke(autoTestBaseBean, BeanUtils.valueOf(str, autoTestBeanTagsBean.getType().getName(), null));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    LogFrameFile.getInstance().error("", e);
                }

            } else {
                str = childTags.get(autoTestBeanTagsBean.getFieldName());
                if (StringUtils.isNotEmpty(str)) {
                    try {
                        autoTestBeanTagsBean.getSetMethod().invoke(autoTestBaseBean, BeanUtils.valueOf(str, autoTestBeanTagsBean.getType().getName(), null));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        LogFrameFile.getInstance().error("", e);
                    }
                }
            }
        }

        String id = autoTestBaseBean.getId();

        if (StringUtils.isBlank(id)) {
            id = UUID.randomUUID().toString();
            autoTestBaseBean.setId(id);
        }

        //解析父标签上的，子标签属性
        List<Class<? extends AutoTestBaseBean>> lc = autoTestBaseBean.getLoadChildFields();
        if (lc != null) {
            for (Class<? extends AutoTestBaseBean> aClass : lc) {
                AutoTestBaseBean autoTestBaseBeanChild = aClass.newInstance();
                List<String> lscs = autoTestBaseBeanChild.getFields();
                Map<String, BeanUtils.AutoTestBeanTagsBean> atbtbscs = BeanUtils.getAutoTestBeanTagsBeanMaps().get(autoTestBaseBeanChild.getTagName());
                boolean cLoad = false;
                for (String lsc : lscs) {
                    String str = attributes.get(lsc);
                    if (StringUtils.isNotEmpty(str)) {
                        cLoad = true;
                        BeanUtils.AutoTestBeanTagsBean auto = atbtbscs.get(lsc);
                        auto.getSetMethod().invoke(autoTestBaseBeanChild, BeanUtils.valueOf(str, auto.getType().getName(), null));
                    }
                }
                BeanUtils.AutoTestBeanTagsBean auto = atbtbscs.get("parentId");
                auto.getSetMethod().invoke(autoTestBaseBeanChild, id);
                if (cLoad) {
                    String childId = autoTestBaseBeanChild.getId();
                    if (StringUtils.isBlank(childId)) {
                        childId = UUID.randomUUID().toString();
                        autoTestBaseBeanChild.setId(childId);
                    }

                    AutoTestBaseBean autoTestBaseBeanOld = BeanFactory.getBeanById(childId);
                    if (autoTestBaseBeanOld != null) {
                        throw new AutoTestException("id标签重复，路径：");
                    } else {
                        BeanFactory.put(childId, autoTestBaseBean);
                    }
                    setAutoTestBeanTagsBeanByClass(atbtbs, aClass, autoTestBaseBeanChild, autoTestBaseBean);
                }
            }
        }

        AutoTestBaseBean autoTestBaseBeanOld = BeanFactory.getBeanById(id);
        if (autoTestBaseBeanOld != null) {
            throw new AutoTestException("id标签重复，路径：");
        } else {
            BeanFactory.put(id, autoTestBaseBean);
        }
        String refId = autoTestBaseBean.getRef();
        if (refId != null) {
            AutoTestBaseBean autoTestBaseBeanRef = BeanFactory.getBeanById(refId);
            if (autoTestBaseBeanRef == null) {
                lazyBeans.add(autoTestBaseBean);
            } else {
                refUtil(autoTestBaseBeanRef, autoTestBaseBean);
            }
        }
        String parentId = autoTestBaseBean.getParentId();

        if (StringUtils.isNotEmpty(parentId)) {
            AutoTestBaseBean autoTestBaseBeanP = BeanFactory.getBeanById(parentId);
            setAutoTestBeanTagsBeanByClass(BeanUtils.getAutoTestBeanTagsBeanMaps().get(autoTestBaseBeanP.getTagName()),
                    autoTestBaseBean.getClass(), autoTestBaseBean, autoTestBaseBeanP);
        }
        return id;
    }

    public static boolean loadRef() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if(lazyBeans.size() == 0){
            return true;
        }
        return loadRef(new ArrayList<AutoTestBaseBean>(lazyBeans),0);
    }

    private static boolean loadRef(List<AutoTestBaseBean> atbs,int length) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int nowLength = atbs.size();
        if(length == nowLength){
            List<String> ids = new ArrayList<>();
            for (AutoTestBaseBean atb : atbs) {
                ids.add(atb.getId());
            }
            throw new AutoTestException("以下id可能存在循环引用："+StringUtils.join(ids,","));
        }
        for (Iterator<AutoTestBaseBean> iterator = atbs.iterator(); iterator.hasNext(); ) {
            AutoTestBaseBean autoTestBaseBean =  iterator.next();

            AutoTestBaseBean autoTestBaseBeanRef = BeanFactory.getBeanById( autoTestBaseBean.getParentId());
            if (autoTestBaseBeanRef == null) {
                lazyBeans.add(autoTestBaseBean);
            } else {
                refUtil(autoTestBaseBeanRef, autoTestBaseBean);
                iterator.remove();
            }
        }
        if(atbs.size() == 0){
            return true;
        }
        return loadRef(atbs,nowLength);
    }

    /**
     * 将更新数据
     */
    private static void refUtil(AutoTestBaseBean autoTestBaseBeanSrc, AutoTestBaseBean autoTestBaseBeanDest) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!autoTestBaseBeanSrc.getClass().getName().equals(autoTestBaseBeanDest.getClass().getName())) {
            throw new AutoTestException("id为" + autoTestBaseBeanDest.getId() + "和id为" + autoTestBaseBeanDest.getId() + "不是同一类型的对象");
        }
        autoTestBaseBeanSrc = BeanUtils.cloneTo(autoTestBaseBeanSrc);
        autoTestBaseBeanSrc.setParentId(autoTestBaseBeanDest.getParentId());
        if (autoTestBaseBeanSrc instanceof TestGroupBaseBean) {
            ((TestGroupBaseBean) autoTestBaseBeanSrc).setPath(((TestGroupBaseBean) autoTestBaseBeanDest).getPath());
        }
        Map<String, BeanUtils.AutoTestBeanTagsBean> beanD = BeanUtils.getAutoTestBeanTagsBeanMap(autoTestBaseBeanDest.getClass().getName());
        for (BeanUtils.AutoTestBeanTagsBean autoTestBeanTagsBean : beanD.values()) {
            Method methodGet = autoTestBeanTagsBean.getGetMethod();
            if (methodGet != null) {
                Object obj = methodGet.invoke(autoTestBaseBeanSrc);
                Method methodSet = autoTestBeanTagsBean.getSetMethod();
                if (methodSet != null) {
                    methodSet.invoke(autoTestBaseBeanDest, obj);
                    if (obj instanceof AutoTestBaseBean) {
                        AutoTestBaseBean atb = ((AutoTestBaseBean) obj);
                        atb.setParentId(autoTestBaseBeanDest.getId());
                        String id = UUID.randomUUID().toString();
                        atb.setId(id);
                        updateParentId(atb, id);
                    }
                }
            }
        }
    }

    //更新子属性的parentId
    private static void updateParentId(AutoTestBaseBean autoTestBaseBean, String parentId) throws InvocationTargetException, IllegalAccessException {
        Map<String, BeanUtils.AutoTestBeanTagsBean> beanD = BeanUtils.getAutoTestBeanTagsBeanMap(autoTestBaseBean.getClass().getName());
        for (BeanUtils.AutoTestBeanTagsBean autoTestBeanTagsBean : beanD.values()) {
            Method methodGet = autoTestBeanTagsBean.getGetMethod();
            if (methodGet != null) {
                Object obj = methodGet.invoke(autoTestBaseBean);
                if (obj instanceof AutoTestBaseBean) {
                    AutoTestBaseBean atb = ((AutoTestBaseBean) obj);
                    atb.setParentId(parentId);
                    String id = UUID.randomUUID().toString();
                    atb.setId(id);
                    updateParentId(atb, id);
                }
            }
        }
    }

    /**
     * @param atbtbs                父对象的类解析配置
     * @param aClass                当前对象的类型
     * @param autoTestBaseBeanChild 当前对象的实列
     * @param autoTestBaseBean      父对象
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static void setAutoTestBeanTagsBeanByClass(Map<String, BeanUtils.AutoTestBeanTagsBean> atbtbs,
                                                       Class<? extends AutoTestBaseBean> aClass,
                                                       AutoTestBaseBean autoTestBaseBeanChild,
                                                       AutoTestBaseBean autoTestBaseBean) throws InvocationTargetException, IllegalAccessException, InstantiationException {

        Collection<BeanUtils.AutoTestBeanTagsBean> ls = atbtbs.values();
        boolean flag = true;
        // 解析非集合，成员变量参数
        for (BeanUtils.AutoTestBeanTagsBean l : ls) {

            if (aClass.equals(l.getType())) {
                if (autoTestBaseBeanChild instanceof MultiNameBean) {
                    if (l.getFieldName().equals(autoTestBaseBeanChild.getTagName())) {
                        flag = false;
                        l.getSetMethod().invoke(autoTestBaseBean, autoTestBaseBeanChild);
                    }
                } else {
                    flag = false;
                    l.getSetMethod().invoke(autoTestBaseBean, autoTestBaseBeanChild);
                }
            }
        }

        if (flag) {
            // 解析集合类型数据
            int index = 0;
            for (BeanUtils.AutoTestBeanTagsBean l : ls) {
                //解析Transaction 中 fieldStep 类型标签
                //System.out.println(l.getSteps().size());
                if (l.getSteps() != null && !l.getSteps().isEmpty()) {
                    if (l.getSteps().contains(aClass.newInstance().getTagName())) {
                        //System.out.println(autoTestBaseBeanChild.getId());
                        List<AutoTestBaseBean> tempLs = (List<AutoTestBaseBean>) l.getGetMethod().invoke(autoTestBaseBean);
                        if (tempLs == null) {
                            tempLs = new ArrayList<>();
                            l.getSetMethod().invoke(autoTestBaseBean, tempLs);
                        }
                        tempLs.add(autoTestBaseBeanChild);
                        break;
                    }
                } else if (l.getType().isAssignableFrom(List.class)) {
                    //System.out.println(l.getType());

                    Type fc = l.getField().getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型
                    if (fc == null) continue;
                    if (fc instanceof ParameterizedType) // 【3】如果是泛型参数的类型
                    {
                        ParameterizedType pt = (ParameterizedType) fc;
                        Type genericClazz = pt.getActualTypeArguments()[0]; //【4】 得到泛型里的class类型对象。

                        //
                        if (Class.class.isInstance(genericClazz)) {
                            if (aClass.equals((Class) genericClazz)) {
                                List<AutoTestBaseBean> tempLs = (List<AutoTestBaseBean>) l.getGetMethod().invoke(autoTestBaseBean);
                                if (tempLs == null) {
                                    tempLs = new ArrayList<>();
                                    l.getSetMethod().invoke(autoTestBaseBean, tempLs);
                                }
                                tempLs.add(autoTestBaseBeanChild);
                                break;
                            }
                        } else {
                            //System.out.println(((ParameterizedTypeImpl)genericClazz).getRawType());
                        }


                    }
                }
            }
        }


    }


}
