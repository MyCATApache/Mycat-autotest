package io.mycat.db.autotest.server;

import io.mycat.db.autotest.autoTestCheckPerformance.AutoTestRunStatus;
import io.mycat.db.autotest.autoTestCheckPerformance.cache.AutoTestCacheUtils;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.bean.testgroup.usecase.UseCase;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.parsing.ParsingAnalysisMain;
import io.mycat.db.autotest.performance.PerformanceExec;
import io.mycat.db.autotest.server.cache.MapdbCache;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import io.mycat.db.autotest.server.memory.AutoTestBeanTagsEngine;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务核心类
 */
public class AutoTestServer {

    private static boolean isJar = false;
    private static String jarPath = "";

    public AutoTestServer() {
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            String recourseFolder = URLDecoder.decode(url.getPath(), "utf-8");
            if (recourseFolder.endsWith(".jar")) {
                isJar = true;
                jarPath = recourseFolder;
            }
        } catch (UnsupportedEncodingException e) {
            LogFrameFile.getInstance().error("", e);
        }
    }

    public void strat(String projectConfigPath, String ids, String outPath, String s, boolean stratCache) {
        try {
            //清理数据
            AutoTestRunStatus.clearUseCaseAlls();
            AutoTestRunStatus.clearUseCaseList();
            BeanFactory.clearGroupBases();
            BeanFactory.clearBases();


            ProjectConfig projectConfig = null;

            if (stratCache && AutoTestCacheUtils.isNotChange(projectConfigPath)) {
                projectConfig = MapdbCache.getUseCaseConfigCache("projectConfig");
                ConcurrentHashMap<String, AutoTestBaseBean> beans = BeanFactory.getBeans();
                beans.putAll(MapdbCache.getUseCaseConfigCache("beans"));
            } else {
                if (!stratCache) {
                    MapdbCache.clearFile();
                }
                AutoTestBeanTagsEngine.loadTagClass();
                // 解析配置文件
                ParsingAnalysisMain.analysis(projectConfigPath);
                projectConfig = BeanFactory.getBeanByClass(ProjectConfig.class);
                if (projectConfig == null) {
                    throw new AutoTestException("解析配置出错");
                }
                if (stratCache) {
                    MapdbCache.setUseCaseConfigCache("projectConfig", projectConfig);
                    MapdbCache.setUseCaseConfigCache("beans", BeanFactory.getBeans());
                }
            }


            if (StringUtils.isNoneBlank(outPath)) {
                projectConfig.setOutPath(outPath);
            }
            String outPathN = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
            FileUtils.deleteDirectory(new File(outPathN));
            // 当以jar执行时，将静态资源解压
            uzipResources(outPathN);

            try {
                projectConfig.initDataSource();
                // 装配配置文件
                BeanFactory.setProjectConfig(projectConfig);

                if (StringUtils.isNotEmpty(ids)) {
                    String[] idas = ids.split(",");
                    for (String id : idas) {
                        AutoTestRunStatus.addUseCaseList(id);
                    }
                }
                List<TestGroupBaseBean> testGroupBaseBeans = BeanFactory.getBeanByClasses(TestGroupBaseBean.class);
                for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
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
                            throw new AutoTestException("不存在id=" + id + "用例");
                        }
                    }
                }

                execUseCase(projectConfig, testGroupBaseBeans);
            } finally {
                projectConfig.close();
            }


            // 是否启动定时器
            if (StringUtils.isNotEmpty(s)) {
                if (Boolean.valueOf(s)) {
                    s = projectConfig.getQuartz();
                }
                SchedulerFactory schedulerfactory = new StdSchedulerFactory();
                //      通过schedulerFactory获取一个调度器
                Scheduler scheduler = schedulerfactory.getScheduler();

                //       创建jobDetail实例，绑定Job实现类
                //       指明job的名称，所在组的名称，以及绑定job类
                JobDetail job = JobBuilder.newJob(AutoTestJob.class).withIdentity("job1", "jgroup1").build();


                //       定义调度触发规则

                //      使用simpleTrigger规则
                //        Trigger trigger=TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")
                //                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1).withRepeatCount(8))
                //                        .startNow().build();
                //      使用cornTrigger规则  每天10点42分
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")
                        .withSchedule(CronScheduleBuilder.cronSchedule(s))//"0 42 10 * * ? *"
                        .startNow().build();

                //       把作业和触发器注册到任务调度中
                scheduler.scheduleJob(job, trigger);

                //       启动调度
                scheduler.start();
                boolean flag = true;
                while (flag) {
                    Thread.sleep(30000);
                }

            }
            LogFrameFile.getInstance().debug("测试完成");
        } catch (Exception e) {
            LogFrameFile.getInstance().error("", e);
        }

    }


    private static void execUseCase(ProjectConfig projectConfig, Collection<TestGroupBaseBean> testGroupBaseBeans) throws Exception {
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
                    try {
                        testGroupBaseBean.initDataSource();
                        testGroupBaseBean.exec();
                    } finally {
                        testGroupBaseBean.close();
                    }
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

        LogFrameFile.getInstance().error("开始执行性能用例");
        if (StringUtils.isNotEmpty(projectConfig.getPerformanceExec())) {
            Constructor constructor = Class.forName(projectConfig.getPerformanceExec()).getConstructor(List.class);
            PerformanceExec performanceExec = (PerformanceExec) constructor.newInstance(testGroupBaseBeans);
            performanceExec.exec();
        }
        LogFrameFile.getInstance().error("开始生成报告");
        String path = "index.html";
        Map<String, Object> datas = new HashMap<>();
        datas.put("testGroupBaseBeans", testGroupBaseBeans);
        try {
            createHtml(projectConfig, datas, path);
        } catch (UnsupportedEncodingException e) {
            LogFrameFile.getInstance().error("", e);
        }
        for (TestGroupBaseBean testGroupBaseBean : testGroupBaseBeans) {
            testGroupBaseBean.createHtml();
        }
    }

    public static class AutoTestJob implements Job {

        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {

            try {
                ProjectConfig projectConfig = BeanFactory.getProjectConfig();
                projectConfig.setTodayTime(DateUtil.getStrDatebyTobayTime());
                String outPathN = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
                FileUtils.deleteDirectory(new File(outPathN));
                // 当以jar执行时，将静态资源解压
                uzipResources(outPathN);

                try {
                    projectConfig.initDataSource();
                    // 装配配置文件
                    BeanFactory.setProjectConfig(projectConfig);

                    List<TestGroupBaseBean> testGroupBaseBeans = BeanFactory.getBeanByClasses(TestGroupBaseBean.class);

                    execUseCase(projectConfig, testGroupBaseBeans);
                } finally {
                    projectConfig.close();
                }

            } catch (Exception e) {
                LogFrameFile.getInstance().error("", e);
            }
        }
    }

    private static void uzipResources(String outPathN) throws Exception {
        if (isJar) {
            ZipUtils zip = new ZipUtils();
            String folder = System.getProperty("java.io.tmpdir");
            zip.unzip(jarPath, folder + "autotest");
            FileUtils.copyDirectoryToDirectory(new File(folder + "autotest/resources/js"), new File(outPathN));
            LogFrameFile.getInstance().debug(folder + "autotest/resources/js === 拷贝完成");
        } else {
            FileUtils.copyDirectoryToDirectory(new File(URLDecoder.decode(AutoTestServer.class.getClassLoader().getResource("resources/js").getPath(), "utf-8")), new File(outPathN));
        }
    }

    static Callable<String> callable(TestGroupBaseBean testGroupBaseBean) {
        return () -> {
            try {
                testGroupBaseBean.initDataSource();
                testGroupBaseBean.exec();
            } catch (Exception e){
                e.printStackTrace();
            } finally{
                testGroupBaseBean.close();
            }
            return testGroupBaseBean.getId();
        };
    }

    private static boolean createHtml(ProjectConfig projectConfig, Map<String, Object> datas, String path) throws UnsupportedEncodingException {
        String outPath = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
        String templateid = "index.html";
        BeetlUtils.createpathTemplate(outPath + "/" + path, templateid, datas);
        return true;
    }

    public static void main(String[] args) {
        Options opt = new Options();
        opt.addOption("p", "path", true, "设置config.xml 的位置");
        opt.addOption("i", "ids", true, "执行用例，多个以,分割");// 当本参数为空时，执行所有用例
        opt.addOption("o", "outpath", true, "用例输出目录");
        opt.addOption("s", "server", true, "是否已服务模式启动");
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

        if (StringUtils.isBlank(projectConfigPath)) {
            LogFrameFile.getInstance().error("无projectConfigPath路径");
        } else {
            LogFrameFile.getInstance().debug("启动");
            AutoTestServer ats = new AutoTestServer();
            ats.strat(projectConfigPath, i, outpath, s, true);
        }

    }
}
