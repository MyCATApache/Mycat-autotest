package io.mycat.db.autotest.utils;

import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.exception.MethodParmsConvertException;
import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by qiank on 2017/1/24.
 */
public class BeanUtils {

    /**
     * 缓存所有的tag class解析
     */
    private static final Map<String, Map<String,AutoTestBeanTagsBean>> autoTestBeanTagsBeanMaps = new HashMap<>();

    public static Map<String, Map<String,AutoTestBeanTagsBean>> getAutoTestBeanTagsBeanMaps() {
        return autoTestBeanTagsBeanMaps;
    }

   /* public static void addAutoTestBeanTagsBeanMaps(Map<String, List<AutoTestBeanTagsBean>> autoTestBeanTagsBeanMaps) {
        autoTestBeanTagsBeanMaps.putAll(autoTestBeanTagsBeanMaps);
    }*/

    public static class AutoTestBeanTagsBean{
        private Field field;

        private String fieldName;

        private Method getMethod;

        private Method setMethod;

        private List<String> steps;

        private String childName;

        private Class<?> type;

        private Class<?> childType;

        public AutoTestBeanTagsBean() {
            super();
        }

        public AutoTestBeanTagsBean(Field field, Method getMethod, Method setMethod, List<String> steps,
                                    String childName, Class<?> type, Class<?> childType) {
            super();
            this.field = field;
            this.getMethod = getMethod;
            this.setMethod = setMethod;
            this.steps = steps;
            this.childName = childName;
            this.type = type;
            this.childType = childType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public Method getGetMethod() {
            return getMethod;
        }

        public void setGetMethod(Method getMethod) {
            this.getMethod = getMethod;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public void setSetMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        public List<String> getSteps() {
            return steps;
        }

        public void setSteps(List<String> steps) {
            this.steps = steps;
        }

        public String getChildName() {
            return childName;
        }

        public void setChildName(String childName) {
            this.childName = childName;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public Class<?> getChildType() {
            return childType;
        }

        public void setChildType(Class<?> childType) {
            this.childType = childType;
        }

    }

    /**
     * 获得类解析
     * @param clazz
     * @return
     * @throws NoSuchMethodException
     */
    public static Map<String,AutoTestBeanTagsBean> getAutoTestBeanTagsBean(Class<?> clazz) throws NoSuchMethodException {
        Map<String,AutoTestBeanTagsBean> autoTestBeanTagsBeans = new HashMap<>();
        getAutoTestBeanTagsBean(clazz,autoTestBeanTagsBeans);
        return autoTestBeanTagsBeans;
    }

    private  static void getAutoTestBeanTagsBean(Class<?> clazz,Map<String,AutoTestBeanTagsBean> autoTestBeanTagsBeans) throws NoSuchMethodException {
        if(Object.class.equals(clazz)){
           return;
        }

        Field[] fld = clazz.getDeclaredFields();
        for (Field field : fld) {
            AutoTestBeanTagsBean autoTestBeanTagsBean = new AutoTestBeanTagsBean();
            FieldStep fieldStep = field.getAnnotation(FieldStep.class);
            FieldType fieldType = field.getAnnotation(FieldType.class);
            if (fieldStep != null) {
                autoTestBeanTagsBean.setSteps(Arrays.asList(fieldStep.name()));
            }
            if (fieldType != null) {
                autoTestBeanTagsBean.setChildName(fieldType.childName());
                autoTestBeanTagsBean.setChildType(fieldType.childType());
                autoTestBeanTagsBean.setType(fieldType.type());
            }
            Class<?> cls = field.getType();
            autoTestBeanTagsBean.setType(cls);
            String getMethod = null;
            String setMethod = null;
            if(cls.getName().equals(Boolean.class.getName()) || cls.getName().equals(boolean.class.getName())){
                getMethod = getGetOrSetMethodName("is",field.getName());
            }else{
                getMethod = getGetOrSetMethodName("get",field.getName());
            }
            setMethod = getGetOrSetMethodName("set",field.getName());
            autoTestBeanTagsBean.setGetMethod( clazz.getMethod(getMethod));
            autoTestBeanTagsBean.setSetMethod( clazz.getMethod(setMethod,cls));
            autoTestBeanTagsBean.setFieldName(field.getName());
            autoTestBeanTagsBean.setField(field);
            autoTestBeanTagsBeans.put(field.getName(),autoTestBeanTagsBean);
        }
        getAutoTestBeanTagsBean(clazz.getSuperclass(),autoTestBeanTagsBeans);
    }

    /**
     *
     * @param fmn 前缀
     * @param name 成员变量名
     * @return
     */
    private static String getGetOrSetMethodName(String fmn,String name){
        StringBuilder sb = new StringBuilder(fmn);
        sb.append(Character.toUpperCase(name.charAt(0))).append(name.substring(1));
        return sb.toString();
    }


    /**
     * 常用数据类型-》转换
     *
     * @param value
     * @param pramsClass
     * @param pattern
     * @return
     */
    public static Object valueOf(String value, String pramsClass, String pattern) {

        switch (pramsClass) {
            case "byte":
                if (value == null || "".equals(value)) {
                    byte by = 0;
                    return by;
                }
            case "java.lang.Byte":
                if (value == null || "".equals(value)) {
                    Byte byt = null;
                    return byt;
                }
                return Byte.valueOf(value);
            case "short":
                if (value == null || "".equals(value)) {
                    short sh = 0;
                    return sh;
                }
            case "java.lang.Short":
                if (value == null || "".equals(value)) {
                    Short sh1 = null;
                    return sh1;
                }
                return Short.valueOf(value);
            case "int":
                if (value == null || "".equals(value)) {
                    int in = 0;
                    return in;
                }
            case "java.lang.Integer":
                if (value == null || "".equals(value)) {
                    Integer in1 = null;
                    return in1;
                }
                return Integer.valueOf(value);
            case "long":
                if (value == null || "".equals(value)) {
                    long lo = 0L;
                    return lo;
                }
            case "java.lang.Long":
                if (value == null || "".equals(value)) {
                    Long lo1 = null;
                    return lo1;
                }
                return Long.valueOf(value);
            case "float":
                if (value == null || "".equals(value)) {
                    float fl = 0.0F;
                    return fl;
                }
            case "java.lang.Float":
                if (value == null || "".equals(value)) {
                    Float fl1 = null;
                    return fl1;
                }
                return Float.valueOf(value);
            case "double":
                if (value == null || "".equals(value)) {
                    double dou = 0.0D;
                    return dou;
                }
            case "java.lang.Double":
                if (value == null || "".equals(value)) {
                    Double dou1 = null;
                    return dou1;
                }
                return Double.valueOf(value);
            case "java.lang.String":
                if ("".equals(value)) {
                    String strs = null;
                    return strs;
                }
                return value;
            case "char":
                if (value == null) {
                    char ch = ' ';
                    return ch;
                }
            case "java.lang.Character":
                if (value == null) {
                    Character ch1 = null;
                    return ch1;
                }
                return value.charAt(0);
            case "boolean":
                if (value == null || "".equals(value)) {
                    return false;
                }
            case "java.lang.Boolean":
                if (value == null || "".equals(value)) {
                    Boolean bo1 = null;
                    return bo1;
                }
                return Boolean.valueOf(value);
            case "java.util.Date":
                Date dat = null;
                if (value == null || "".equals(value)) {
                    return dat;
                }

                if (StringUtils.isNotBlank(pattern)) {
                    try {
                        dat = DateUtil.parseStrToDate(value, pattern);
                    } catch (Exception e) {
                        LogFrameFile.getInstance().error("", e);
                        throw new MethodParmsConvertException("日期类型转换错误", e);
                    }
                }
                if (dat == null) {
                    String valueNew = value.trim();
                    int length = valueNew.length();
                    if (length == 10 || length == 19) {
                        if (length == 10) {
                            dat = DateUtil.parseStrToDate(valueNew, DateUtil.YEAR_MONTH_DAY_ZH);
                        } else {
                            dat = DateUtil.parseStrToDate(valueNew, DateUtil.YEAR_MONTH_DAY_HOUR_MIN_SEC_ZH);
                        }
                    } else if (length == 13 && NumberUtils.isNumber(valueNew)) {
                        dat = new Date(Long.valueOf(valueNew));
                    }
                }
                if (dat == null) {
                    LogFrameFile.getInstance().error("日期类型转换错误");
                }
                return dat;
            default:
                LogFrameFile.getInstance().error("错误的参数类型");
                return "";
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneTo(T src) throws RuntimeException {
        ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        T dist = null;
        try {
            out = new ObjectOutputStream(memoryBuffer);
            out.writeObject(src);
            out.flush();
            in = new ObjectInputStream(new ByteArrayInputStream(memoryBuffer.toByteArray()));
            dist = (T) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null)
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            if (in != null)
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        return dist;
    }
}
