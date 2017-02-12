package io.mycat.db.autotest.server.memory;

import java.lang.reflect.*;
import java.util.*;

import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.MultiNameBean;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.bean.annotation.FieldType;
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
 * 
* @ClassName: AutoTestBeanTagsMemory 
* @Description: 缓存配置标签配置信息，并解析标签
* @author zjliu qiankunshe@126.com
* @date 2017年1月22日 上午10:18:45 
*
 */
public class AutoTestBeanTagsEngine {

	/**
	 * 缓存所有的标签
	 */
	private static final List<AutoTestBaseBean> objTags = new ArrayList<>();

	/*public static void addObjtags(List<AutoTestBaseBean> tags) {
		objTags.addAll(tags);
	}

	public static List<AutoTestBaseBean> getObjtags() {
		return objTags;
	}*/

	/**
	 * 加载标签对应的对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void loadTagClass() throws InstantiationException, IllegalAccessException, NoSuchMethodException {
		Set<Class<?>> cls = ClassUtils.getClasses("io.mycat.db.autotest.bean");
		List<AutoTestBaseBean> objTag = new ArrayList<>();
		Map<String, Map<String,BeanUtils.AutoTestBeanTagsBean>> autoTestBeanTagsBeanMap = new HashMap<>();
		for (Class<?> class1 : cls) {
			if (!class1.isAnnotation() && !Modifier.isAbstract(class1.getModifiers())
					&& AutoTestBaseBean.class.isAssignableFrom(class1)) {
				AutoTestBaseBean autoTestBaseBean = (AutoTestBaseBean) class1.newInstance();
                String tagName = autoTestBaseBean.getTagName();

                Map<String,BeanUtils.AutoTestBeanTagsBean> autoTestBeanTagsBeans = BeanUtils.getAutoTestBeanTagsBean(
                        class1);
                if("multiName".equals(tagName)){
                    List<Class<? extends AutoTestBaseBean>> classes = ((MultiNameBean)autoTestBaseBean).getMultiNameBean();
                    //boolean flag = false;

                    for (Class<? extends AutoTestBaseBean> aClass : classes) {
                        Field[] fields = aClass.getDeclaredFields();
                        for (Field cc : fields) {
                           if(autoTestBaseBean.getClass().getName().equals(cc.getType().getName())){
                               AutoTestBaseBean autoTestBaseBean2 = autoTestBaseBean.getClass().newInstance();
                               autoTestBaseBean2.setTagName(cc.getName());
                               objTag.add(autoTestBaseBean2);
                               autoTestBeanTagsBeanMap.put(cc.getName(), autoTestBeanTagsBeans);
                               //flag = true;
                           }
                        }
                    }
                    /*if(flag){
                        continue;
                    }*/
                }
				objTag.add(autoTestBaseBean);
				autoTestBeanTagsBeanMap.put(autoTestBaseBean.getTagName(), autoTestBeanTagsBeans);
			}
		}
		objTags.addAll(objTag);
		BeanUtils.getAutoTestBeanTagsBeanMaps().putAll(autoTestBeanTagsBeanMap);
	}
	
	/**
	 * @Title: getObj 
	 * @Description: 根据标签返回对应的实现类
	 * @param tagName
	 * @return 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static AutoTestBaseBean getObj(String tagName) throws InstantiationException, IllegalAccessException{
		for (AutoTestBaseBean autoTestBaseBean : objTags) {
			if(tagName.equals(autoTestBaseBean.getTagName())){
                AutoTestBaseBean autoTestBaseBean2 = autoTestBaseBean.getClass().newInstance();
                autoTestBaseBean2.setTagName(autoTestBaseBean.getTagName());
				return autoTestBaseBean2;
			}
		}
		return null;
	}
	
	public static boolean isTag(String tagName) {
		for (AutoTestBaseBean autoTestBaseBean : objTags) {
			if(tagName.equals(autoTestBaseBean.getTagName())){
				return true;
			}
		}

		return false;
	}

	/**
	 *
	 * @param tagName
	 * @param attributes 除属性外，还包含父id
	 * @param childTags 子标签，如果属性在 attributes和childTags 都存在只生效 attributes
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static String analysis(String tagName,Map<String,String> attributes,Map<String,String> childTags) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		// 非tag时返回null
		if(!isTag(tagName)){
			return null;
		}

		//System.out.println("tagName:"+tagName);
		AutoTestBaseBean autoTestBaseBean = getObj(tagName);
        Map<String,BeanUtils.AutoTestBeanTagsBean> atbtbs = BeanUtils.getAutoTestBeanTagsBeanMaps().get(tagName);
        if(autoTestBaseBean == null){
        	return null;
		}

		//解析当前标签
        for (Map.Entry<String,BeanUtils.AutoTestBeanTagsBean> stringAutoTestBeanTagsBeanEntry : atbtbs.entrySet()) {
            BeanUtils.AutoTestBeanTagsBean  autoTestBeanTagsBean = stringAutoTestBeanTagsBeanEntry.getValue();
            String str = attributes.get(autoTestBeanTagsBean.getFieldName());
            if(StringUtils.isNotEmpty(str)){
               // BeanUtils.AutoTestBeanTagsBean auto  = atbtbs.get(field);
				try{
					autoTestBeanTagsBean.getSetMethod().invoke(autoTestBaseBean, BeanUtils.valueOf(str,autoTestBeanTagsBean.getType().getName(),null));
				}catch (IllegalArgumentException e){
					e.printStackTrace();
					LogFrameFile.getInstance().error("",e);
				}

            }else{
                str = childTags.get(autoTestBeanTagsBean.getFieldName());
                if(StringUtils.isNotEmpty(str)){
					try{
                    	autoTestBeanTagsBean.getSetMethod().invoke(autoTestBaseBean,BeanUtils.valueOf(str,autoTestBeanTagsBean.getType().getName(),null));
					}catch (IllegalArgumentException e){
						e.printStackTrace();
						LogFrameFile.getInstance().error("",e);
					}
                }
            }
        }

		String id = autoTestBaseBean.getId();

		if(StringUtils.isBlank(id)){
			id = UUID.randomUUID().toString();
			autoTestBaseBean.setId(id);
		}

        //解析父标签上的，子标签属性
        List<Class<? extends AutoTestBaseBean>> lc = autoTestBaseBean.getLoadChildFields();
        if(lc != null){
            for (Class<? extends AutoTestBaseBean> aClass : lc) {
				AutoTestBaseBean autoTestBaseBeanChild = aClass.newInstance();
				//attributes.put("parentId",id);
                List<String> lscs = autoTestBaseBeanChild.getFields();
                Map<String,BeanUtils.AutoTestBeanTagsBean> atbtbscs = BeanUtils.getAutoTestBeanTagsBeanMaps().get(autoTestBaseBeanChild.getTagName());
				//atbtbscs.put("parentId",id);
                boolean cLoad = false;
				//System.out.print(autoTestBaseBeanChild+": ");System.out.println(atbtbscs);
                for (String lsc : lscs) {
                   String str = attributes.get(lsc);
                    if(StringUtils.isNotEmpty(str)){
                        cLoad = true;
                        BeanUtils.AutoTestBeanTagsBean auto  =  atbtbscs.get(lsc);
                        auto.getSetMethod().invoke(autoTestBaseBeanChild, BeanUtils.valueOf(str,auto.getType().getName(),null));
                    }
                }
				BeanUtils.AutoTestBeanTagsBean auto = atbtbscs.get("parentId");
				auto.getSetMethod().invoke(autoTestBaseBeanChild, id);
                if(cLoad){
					String childId = autoTestBaseBeanChild.getId();

					if(StringUtils.isBlank(childId)){
						childId = UUID.randomUUID().toString();
						autoTestBaseBeanChild.setId(childId);
					}

					AutoTestBaseBean autoTestBaseBeanOld = BeanFactory.getBeanById(childId);
					if(autoTestBaseBeanOld != null){
						throw new AutoTestException("id标签重复，路径：");
					}else{
						BeanFactory.put(childId,autoTestBaseBean);
					}
                    setAutoTestBeanTagsBeanByClass(atbtbs,aClass,autoTestBaseBeanChild,autoTestBaseBean);
                }
            }
        }

		AutoTestBaseBean autoTestBaseBeanOld = BeanFactory.getBeanById(id);
		if(autoTestBaseBeanOld != null){
			throw new AutoTestException("id标签重复，路径：");
		}else{
			BeanFactory.put(id,autoTestBaseBean);
		}
		String parentId = autoTestBaseBean.getParentId();
        if(StringUtils.isNotEmpty(parentId)){
			AutoTestBaseBean autoTestBaseBeanP = BeanFactory.getBeanById(parentId);
			setAutoTestBeanTagsBeanByClass( BeanUtils.getAutoTestBeanTagsBeanMaps().get(autoTestBaseBeanP.getTagName()),
                    autoTestBaseBean.getClass(),autoTestBaseBean,autoTestBaseBeanP);
		}

		return id;
	}

	/**
	 *
	 * @param atbtbs 父对象的类解析配置
	 * @param aClass 当前对象的类型
	 * @param autoTestBaseBeanChild 当前对象的实列
	 * @param autoTestBaseBean 父对象
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private static void  setAutoTestBeanTagsBeanByClass(Map<String,BeanUtils.AutoTestBeanTagsBean> atbtbs,
                                                        Class<? extends AutoTestBaseBean> aClass,
                                                        AutoTestBaseBean autoTestBaseBeanChild,
                                                        AutoTestBaseBean autoTestBaseBean) throws InvocationTargetException, IllegalAccessException, InstantiationException {

		Collection<BeanUtils.AutoTestBeanTagsBean> ls = atbtbs.values();
		boolean flag = true;
		// 解析非集合，成员变量参数
		for (BeanUtils.AutoTestBeanTagsBean l : ls) {

			if(aClass.equals(l.getType())){
                if(autoTestBaseBeanChild  instanceof MultiNameBean ){
                    if(l.getFieldName().equals(autoTestBaseBeanChild.getTagName())){
                        flag = false;
                        l.getSetMethod().invoke(autoTestBaseBean,autoTestBaseBeanChild);
                    }

                }else{
                    flag = false;
                    l.getSetMethod().invoke(autoTestBaseBean,autoTestBaseBeanChild);
                }
			}
		}

        if(flag){
			// 解析集合类型数据
            int index = 0;
			for (BeanUtils.AutoTestBeanTagsBean l : ls) {
				//解析Transaction 中 fieldStep 类型标签
                //System.out.println(l.getSteps().size());
                if(l.getSteps() != null && !l.getSteps().isEmpty()){
					if(l.getSteps().contains(aClass.newInstance().getTagName())){
                        //System.out.println(autoTestBaseBeanChild.getId());
                        List<AutoTestBaseBean> tempLs = (List<AutoTestBaseBean>)l.getGetMethod().invoke(autoTestBaseBean);
						if(tempLs == null){
							tempLs = new ArrayList<>();
							l.getSetMethod().invoke(autoTestBaseBean,tempLs);
						}
                        tempLs.add(autoTestBaseBeanChild);
						break;
					}
				}else if(l.getType().isAssignableFrom(List.class)){
                    //System.out.println(l.getType());
                    Type fc = l.getField().getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型
					if(fc == null) continue;
					if(fc instanceof ParameterizedType) // 【3】如果是泛型参数的类型
					{
						ParameterizedType pt = (ParameterizedType) fc;
                        Type genericClazz = pt.getActualTypeArguments()[0]; //【4】 得到泛型里的class类型对象。

						//
                        if(Class.class.isInstance(genericClazz)){
                            if(aClass.equals((Class)genericClazz)){
                                List<AutoTestBaseBean> tempLs = (List<AutoTestBaseBean>)l.getGetMethod().invoke(autoTestBaseBean);
                                if(tempLs == null){
                                    tempLs = new ArrayList<>();
                                    l.getSetMethod().invoke(autoTestBaseBean,tempLs);
                                }
                                tempLs.add(autoTestBaseBeanChild);
                                break;
                            }
                        }else{
                            //System.out.println(((ParameterizedTypeImpl)genericClazz).getRawType());
                        }


					}
				}
			}
		}


	}

	
}
