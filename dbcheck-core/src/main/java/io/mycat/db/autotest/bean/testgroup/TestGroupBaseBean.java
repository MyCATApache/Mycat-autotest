package io.mycat.db.autotest.bean.testgroup;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.*;
import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.bean.testgroup.usecase.Transaction;
import io.mycat.db.autotest.bean.testgroup.usecase.UseCase;
import io.mycat.db.autotest.dataSource.UseDataSource;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.BeetlUtils;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.PathUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

public class TestGroupBaseBean extends AutoTestBaseBean implements AutoTestDataSource, TagServerType, UseCaseLocalPath,CreateHtmlExec {

    public TestGroupBaseBean() {
        super(Arrays.asList("config", "beforeTestGroup", "afterTestGroup", "beforeTest", "afterTest"), "testGroup", null);
    }

    private Config config = new Config();

    private TestGroupTransaction beforeTestGroup;

    private TestGroupTransaction afterTestGroup;

    private TestGroupTransaction beforeTest;

    private TestGroupTransaction afterTest;

    private List<UseCase> useCases;

    private transient List<String> useCaseExecuteComplete = new ArrayList<>();

    private transient List<UseCase> lazyUseCases  = new ArrayList<>();

    private String path;

    private transient int type = 1;

    //private String defaultDataSource;

    @FieldType(childName = "connection", childType = io.mycat.db.autotest.bean.Connection.class)
    private List<io.mycat.db.autotest.bean.Connection> connections;

    private transient Map<String, DataSource> dataSources = new java.util.concurrent.ConcurrentHashMap<>();

	/*public String getDefaultDataSource() {
        return defaultDataSource;
	}

	public void setDefaultDataSource(String defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}*/

    public boolean isAsyn() {
        if (config != null && !config.isSync()) {
            return true;
        }
        return false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getUseCaseExecuteComplete() {
        return useCaseExecuteComplete;
    }

    public void setUseCaseExecuteComplete(List<String> useCaseExecuteComplete) {
        this.useCaseExecuteComplete = useCaseExecuteComplete;
    }

    public List<UseCase> getLazyUseCases() {
        return lazyUseCases;
    }

    public void setLazyUseCases(List<UseCase> lazyUseCases) {
        this.lazyUseCases = lazyUseCases;
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public TestGroupTransaction getBeforeTestGroup() {
        return beforeTestGroup;
    }

    public void setBeforeTestGroup(TestGroupTransaction beforeTestGroup) {
        this.beforeTestGroup = beforeTestGroup;
    }

    public TestGroupTransaction getAfterTestGroup() {
        return afterTestGroup;
    }

    public void setAfterTestGroup(TestGroupTransaction afterTestGroup) {
        this.afterTestGroup = afterTestGroup;
    }

    public TestGroupTransaction getBeforeTest() {
        return beforeTest;
    }

    public void setBeforeTest(TestGroupTransaction beforeTest) {
        this.beforeTest = beforeTest;
    }

    public TestGroupTransaction getAfterTest() {
        return afterTest;
    }

    public void setAfterTest(TestGroupTransaction afterTest) {
        this.afterTest = afterTest;
    }

    public List<UseCase> getUseCases() {
        return useCases;
    }

    public void setUseCases(List<UseCase> useCases) {
        this.useCases = useCases;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<io.mycat.db.autotest.bean.Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<io.mycat.db.autotest.bean.Connection> connections) {
        this.connections = connections;
    }

    @Override
    public void initDataSource() {
        if (connections != null) {
            for (io.mycat.db.autotest.bean.Connection connection : connections) {
                dataSources.put(connection.getId(), UseDataSource.getDataSource(connection));
            }
        }
    }

    @Override
    public java.sql.Connection getConnection(String name) throws SQLException {
        DataSource dataSource = dataSources.get(name);
        if (dataSource == null) {
            //全局
            ProjectConfig projectConfig = BeanFactory.getProjectConfig();
            return projectConfig.getConnection(name);
        }
        return dataSource.getConnection();
    }

    @Override
    public void close() throws Exception {
        Collection<DataSource> ls = dataSources.values();
        for (DataSource dataSource : ls) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean exec() throws Exception {
        TestGroupBaseBean tgbb = this;
        TestGroupTransaction afterTestGroup = tgbb.getAfterTestGroup();
        TestGroupTransaction beforeTestGroup = tgbb.getBeforeTestGroup();
        TestGroupTransaction afterTest = tgbb.getAfterTest();
        TestGroupTransaction beforeTest = tgbb.getBeforeTest();
        //Config config = tgbb.getConfig();

        if (beforeTestGroup != null) {
            beforeTestGroup.exec();
        }


        ExecutorService executor = null;
        try{
            executor = Executors.newFixedThreadPool(10);
            List<Callable<String>> callables = new ArrayList<>();

            try {
                for (UseCase useCase : useCases) {
                    String id = useCase.getDepend();
                    if(StringUtils.isNotEmpty(id) && !useCaseExecuteComplete.contains(id)){
                        lazyUseCases.add(useCase);
                        continue;
                    }
                    //当前只有支持异步执行且不依赖别的用例的用例才能异步执行
                    if(useCase.isAsyn() && this.getType() == 1){
                        callables.add(callable(useCase));
                    }else{

                        if (beforeTest != null) {
                            beforeTest.exec();
                        }

                        useCase.exec();

                        if (afterTest != null) {
                            afterTest.exec();
                        }
                        this.useCaseExecuteComplete.add(useCase.getId());
                    }
                }

                if((lazyUseCases.size()) > 0){
                    execlazyUseCases(0,lazyUseCases,afterTest,beforeTest);
                }

            } finally { // 保证资源清理回收
                if (afterTestGroup != null) {
                    afterTestGroup.exec();
                }
            }

            if(callables.size() > 0){
                String result = executor.invokeAny(callables);
                //System.out.println(result);
            }
        }finally {
            if(executor != null){
                executor.shutdown();
            }
        }

        /*try {
            for (UseCase useCase : useCases) {

                String id = useCase.getDepend();
                if(StringUtils.isNotEmpty(id) && !useCaseExecuteComplete.contains(id)){
                    lazyUseCases.add(useCase);
                    continue;
                }

                if (beforeTest != null) {
                    beforeTest.exec();
                }

                useCase.exec();

                if (afterTest != null) {
                    afterTest.exec();
                }
                this.useCaseExecuteComplete.add(useCase.getId());
            }

            if((lazyUseCases.size()) > 0){
                execlazyUseCases(0,lazyUseCases,afterTest,beforeTest);
            }

        } finally { // 保证资源清理回收
            if (afterTestGroup != null) {
                afterTestGroup.exec();
            }
        }*/



        return true;
    }

    Callable<String> callable(UseCase useCase) {
        return () -> {
            useCase.exec();
            return useCase.getId();
        };
    }

    private boolean execlazyUseCases(int oldLength,List<UseCase> lazyUseCases, TestGroupTransaction afterTest, TestGroupTransaction beforeTest) throws Exception {
        int nowLength = lazyUseCases.size();
        if(oldLength == nowLength){
            throw new AutoTestException("请查看是否有循环依赖");
        }
        for (Iterator<UseCase> iterator = lazyUseCases.iterator(); iterator.hasNext(); ) {
            UseCase useCase =  iterator.next();
            String id = useCase.getDepend();
            if(StringUtils.isNotEmpty(id) && !useCaseExecuteComplete.contains(id)){
                continue;
            }
            if (beforeTest != null) {
                beforeTest.exec();
            }

            useCase.exec();

            if (afterTest != null) {
                afterTest.exec();
            }
            this.useCaseExecuteComplete.add(useCase.getId());
            iterator.remove();
        }
        return execlazyUseCases(nowLength,lazyUseCases,afterTest,beforeTest);
    }

    private static boolean createHtml(Map<String, Object> datas, String path) throws UnsupportedEncodingException {
        ProjectConfig projectConfig = BeanFactory.getProjectConfig();
        String outPath = PathUtils.getPath(projectConfig.getPath(), projectConfig.getOutPath());
        String templateid = "groupUseCase.html";
        BeetlUtils.createpathTemplate(outPath + "/" + path, templateid, datas);
        return true;
    }

    @Override
    public boolean createHtml() {
        String path = "groupUseCase/" + this.getId() + ".html";
        Map<String, Object> datas = new HashMap<>();
        datas.put("useCases", useCases);
        datas.put("groupUseCase", this);
        try {
            createHtml(datas, path);
        } catch (UnsupportedEncodingException e) {
            LogFrameFile.getInstance().error("", e);
            return false;
        }
        boolean flag = true;
        for (UseCase useCase : useCases) {
            if(!useCase.createHtml()){
                flag = false;
            }
        }
        return flag;
    }
}
