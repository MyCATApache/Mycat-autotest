package io.mycat.db.autotest.server;

import io.mycat.db.autotest.autoTestCheckPerformance.AutoTestRunStatus;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.bean.testgroup.usecase.UseCase;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.parsing.ParsingAnalysisMain;
import io.mycat.db.autotest.performance.PerformanceExec;
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务核心类
 */
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

	public void strat(String projectConfigPath, String ids, String outPath, String s) {
		try {
			AutoTestBeanTagsEngine.loadTagClass();

			// 解析配置文件
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
			// 当以jar执行时，将静态资源解压
			if(isJar){
				ZipUtils zip = new ZipUtils();
				String folder = System.getProperty("java.io.tmpdir");
				zip.unzip(jarPath,folder+"autotest");
				FileUtils.copyDirectoryToDirectory(new File( folder+"autotest/resources/js"),new File(outPathN));
				LogFrameFile.getInstance().debug(folder+"autotest/resources/js === 拷贝完成");
			}else{
				FileUtils.copyDirectoryToDirectory(new File( URLDecoder.decode(AutoTestServer.class.getClassLoader().getResource("resources/js").getPath(),"utf-8")),new File(outPathN));
			}

			try {
				projectConfig.initDataSource();
				BeanFactory.setProjectConfig(projectConfig);
				if (StringUtils.isNotEmpty(ids)) {
					String[] idas = ids.split(",");
					for (String id : idas) {
						AutoTestRunStatus.addUseCaseList(id);
					}
				}
				List<TestGroupBaseBean> testGroupBaseBeans = BeanFactory.getBeanByClasses(TestGroupBaseBean.class);
				for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
					testGroupBaseBean.initDataSource();
					BeanFactory.addGroupBases(testGroupBaseBean);
					if (StringUtils.isNotEmpty(ids)) {
						AutoTestRunStatus.addUseCaseAlls(testGroupBaseBean.getId());
						for (UseCase useCase : testGroupBaseBean.getUseCases()) {
							AutoTestRunStatus.addUseCaseAlls(useCase.getId());
						}
					}
				}
				if (StringUtils.isNotEmpty(ids)) {
					for (String id : AutoTestRunStatus.getUseCaseList()) {
						if (!AutoTestRunStatus.isUseCaseAlls(id)) {
							throw new AutoTestException("不存在id为" + id + "用例");
						}
					}
				}
				LogFrameFile.getInstance().error("用例装配完成，连接池初始化完成......");

				LogFrameFile.getInstance().error("开始执行验证用例");
				ExecutorService executor = null;
				try {
					executor = Executors.newFixedThreadPool(projectConfig.getCheckConcurrency());
					List<Callable<String>> callables = new ArrayList<>();
					for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
						testGroupBaseBean.setType(1);
						// 如果不能异步执行的组，先执行
						if (testGroupBaseBean.isAsyn()) {
							callables.add(callable(testGroupBaseBean));
						} else {
							testGroupBaseBean.exec();
						}
					}
					if (callables.size() > 0) {
						String result = executor.invokeAny(callables);
					}
				} finally {
					if (executor != null) {
						executor.shutdown();
					}
				}

				LogFrameFile.getInstance().error("性能用例执行开始");
				if (StringUtils.isNotEmpty(projectConfig.getPerformanceExec())) {
					Constructor constructor = Class.forName(projectConfig.getPerformanceExec()).getConstructor(List.class);
					PerformanceExec performanceExec = (PerformanceExec) constructor.newInstance(testGroupBaseBeans);
					performanceExec.exec();
				}
				LogFrameFile.getInstance().error("开始生成报告");
				String path = "index.html";
				Map<String, Object> datas = new HashMap<>();
				datas.put("testGroupBaseBeans",testGroupBaseBeans);
				try {
					createHtml(projectConfig,datas,path);
				} catch (UnsupportedEncodingException e) {
					LogFrameFile.getInstance().error("",e);
				}
				for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
					testGroupBaseBean.createHtml();
				}
			}finally {
				projectConfig.close();
			}


			// 是否启动定时器
			if(Boolean.valueOf(s)){
				
			}
			LogFrameFile.getInstance().debug("测试完成");
		}catch (Exception e){
			LogFrameFile.getInstance().error("", e);
		}

	}

	Callable<String> callable(TestGroupBaseBean testGroupBaseBean) {
		return () -> {
			testGroupBaseBean.exec();
			return testGroupBaseBean.getId();
		};
	}

	private static boolean createHtml(ProjectConfig projectConfig,Map<String, Object> datas, String path) throws UnsupportedEncodingException {
		String outPath = PathUtils.getPath(projectConfig.getPath(),projectConfig.getOutPath());
		String templateid = "index.html";
		BeetlUtils.createpathTemplate(outPath + "/" + path,templateid,datas);
		return true;
	}

	public static void main(String[] args) {
		Options opt = new Options();
		opt.addOption("p", "path", true, "设置config.xml 的位置");
		opt.addOption("i","ids", true, "执行用例，多个以,分割");// 当本参数为空时，执行所有用例
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
		/*boolean all = true;
		if (cl.hasOption("a")) {
			all = Boolean.valueOf(cl.getOptionValue("a"));
		}*/

		String outpath = null;
		if (cl.hasOption("o")) {
			outpath = cl.getOptionValue("o");
		}

		String i = null;
		if (cl.hasOption("i")) {
			i = cl.getOptionValue("i");
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
			ats.strat(projectConfigPath,i,outpath,s);
		}
		
	}
}
