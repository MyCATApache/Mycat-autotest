package io.mycat.db.autotest.server;

import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.parsing.ParsingAnalysisMain;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.BeetlUtils;
import io.mycat.db.autotest.utils.PathUtils;
import io.mycat.db.autotest.utils.ZipUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
import io.mycat.db.autotest.utils.LogFrameFile;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoTestServer {

	private boolean isJar = false;
	private String jarPath = "";

	public AutoTestServer(){
		URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
		try {
			String recourseFolder = URLDecoder.decode(url.getPath(), "utf-8");
			if (recourseFolder.endsWith(".jar")) {
				isJar = true;
				jarPath = recourseFolder;
			}
		} catch (UnsupportedEncodingException e) {
			LogFrameFile.getInstance().error("",e);
		}
	}

	public void strat(String projectConfigPath, boolean all, String outPath, String s) {
		try {
			AutoTestBeanTagsEngine.loadTagClass();
			ParsingAnalysisMain.analysis(projectConfigPath);
			ProjectConfig projectConfig = BeanFactory.getBeanByClass(ProjectConfig.class);
			if(projectConfig == null){
				throw new AutoTestException("解析配置出错");
			}
			if(StringUtils.isNoneBlank(outPath)){
				projectConfig.setOutPath(outPath);
			}
			String outPathN = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
			FileUtils.deleteDirectory(new File(outPathN));
			if(isJar){
				ZipUtils zip = new ZipUtils();
				String folder = System.getProperty("java.io.tmpdir");
				zip.unzip(jarPath,folder+"autotest");
				FileUtils.copyDirectoryToDirectory(new File( folder+"autotest/resources/js"),new File(outPathN));
				LogFrameFile.getInstance().debug(folder+"autotest/resources/js === 拷贝完成");
			}else{
				FileUtils.copyDirectoryToDirectory(new File( URLDecoder.decode(AutoTestServer.class.getClassLoader().getResource("resources/js").getPath(),"utf-8")),new File(outPathN));
			}

			projectConfig.initDataSource();
			BeanFactory.setProjectConfig(projectConfig);
			List<TestGroupBaseBean> testGroupBaseBeans = BeanFactory.getBeanByClasses(TestGroupBaseBean.class);
			for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
				testGroupBaseBean.initDataSource();
				BeanFactory.addGroupBases(testGroupBaseBean);
			}
			LogFrameFile.getInstance().debug("用例装配完成，连接池初始化完成......");

			for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
				testGroupBaseBean.exec();
			}

			String path = "index.html";
			Map<String, Object> datas = new HashMap<>();
			datas.put("testGroupBaseBeans",testGroupBaseBeans);
			try {
				createHtml(datas,path);
			} catch (UnsupportedEncodingException e) {
				LogFrameFile.getInstance().error("",e);
			}

			// 是否启动定时器
			if(Boolean.valueOf(s)){

			}
			LogFrameFile.getInstance().debug("测试完成");
		} catch (InstantiationException e) {
			e.printStackTrace();
			LogFrameFile.getInstance().error("", e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			LogFrameFile.getInstance().error("", e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			LogFrameFile.getInstance().error("", e);
		}catch (Exception e){
			e.printStackTrace();
			LogFrameFile.getInstance().error("", e);
		}catch (Throwable e){
			e.printStackTrace();
			LogFrameFile.getInstance().error("", e);
		}

	}

	private static boolean createHtml(Map<String, Object> datas, String path) throws UnsupportedEncodingException {
		ProjectConfig projectConfig = BeanFactory.getProjectConfig();
		String outPath = PathUtils.getPath(projectConfig.getPath(),projectConfig.getOutPath());
		String templateid = "index.html";
		BeetlUtils.createpathTemplate(outPath + "/" + path,templateid,datas);
		return true;
	}

	public static void main(String[] args) {
		Options opt = new Options();
		opt.addOption("p", "path", true, "设置config.xml 的位置");
		opt.addOption("a","all", true, "默认为true，执行所有的用例");
		opt.addOption("n","name", true, "执行用例");
		opt.addOption("o","outpath", true, "用例输出目录");
		opt.addOption("s","server", true, "是否已服务模式启动");
		String formatstr = "autotest [-p/--path][-a/--all][-n/--name][-o/--outpath][-s/--server]";

		HelpFormatter formatter = new HelpFormatter();
		DefaultParser parser = new DefaultParser();
		CommandLine cl = null;
		try {
			// 处理Options和参数
			cl = parser.parse(opt, args);
		} catch (ParseException e) {
			formatter.printHelp(formatstr, opt); // 如果发生异常，则打印出帮助信息
		}
		String projectConfigPath = null;
		if (cl.hasOption("p")) {
			projectConfigPath = cl.getOptionValue("p");
		}
		boolean all = true;
		if (cl.hasOption("a")) {
			all = Boolean.valueOf(cl.getOptionValue("a"));
		}

		String outpath = null;
		if (cl.hasOption("o")) {
			outpath = cl.getOptionValue("o");
		}

		String n = null;
		if (cl.hasOption("n")) {
			n = cl.getOptionValue("n");
		}

		String s = null;
		if (cl.hasOption("s")) {
			s = cl.getOptionValue("s");
		}

		if(StringUtils.isBlank(projectConfigPath)){
			System.out.println("无projectConfigPath路径");
		}else{
			System.out.println("开始启动");
			AutoTestServer ats = new AutoTestServer();
			ats.strat(projectConfigPath,all,outpath,s);
		}
		
	}
}
