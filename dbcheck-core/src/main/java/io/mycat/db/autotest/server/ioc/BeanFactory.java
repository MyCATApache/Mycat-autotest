package io.mycat.db.autotest.server.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;

public class BeanFactory {

	/**
	 * ioc对象池
	 */
	private static final ConcurrentHashMap<String, AutoTestBaseBean> beans = new ConcurrentHashMap<>();

	/**
	 * 延迟对象缓存
	 */
	private static final Set< AutoTestBaseBean> deferreds = new java.util.concurrent.ConcurrentSkipListSet<>();

	/**
	 * 用例组，缓存
	 */
	private static final Set<TestGroupBaseBean> groupBases = new java.util.concurrent.ConcurrentSkipListSet<>();

	private static ProjectConfig projectConfig;

	public static ProjectConfig getProjectConfig() {
		return projectConfig;
	}

	public static void setProjectConfig(ProjectConfig projectConfig) {
		BeanFactory.projectConfig = projectConfig;
	}

	public static  AutoTestBaseBean getBeanById(String id) {
		return beans.get(id);
	}

	public static  void put(String id,AutoTestBaseBean autoTestBaseBean) {
		beans.put(id,autoTestBaseBean);
	}

	public static ConcurrentHashMap<String, AutoTestBaseBean> getBeans() {
		return beans;
	}

	public static Set<TestGroupBaseBean> getGroupBases() {
		return groupBases;
	}

	public static void addGroupBases(TestGroupBaseBean tgb) {
		groupBases.add(tgb);
	}

	public static <T extends AutoTestBaseBean> List<T> getBeanByClasses(Class<?> clazz) {
		Collection<AutoTestBaseBean> ls = beans.values();
		List<AutoTestBaseBean> autoTestBaseBeans = new ArrayList<>();
		for (AutoTestBaseBean l : ls) {
			if(clazz.equals(l.getClass())){
				autoTestBaseBeans.add(l);
			}
		}
		return (List<T>)autoTestBaseBeans;
	}

	public static <T extends AutoTestBaseBean> T getBeanByClass(Class<?> clazz) {
		Collection<AutoTestBaseBean> ls = beans.values();
		for (AutoTestBaseBean l : ls) {
			if(clazz.equals(l.getClass())){
				return (T)l;
			}
		}
		return null;
	}

	public static Set< AutoTestBaseBean> getDeferreds() {
		return deferreds;
	}

	public static void addDeferred(AutoTestBaseBean autoTestBaseBean){
		deferreds.add(autoTestBaseBean);
	}


}
