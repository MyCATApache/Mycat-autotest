package io.mycat.db.autotest.bean.testgroup.usecase;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import io.mycat.db.autotest.autoTestCheckPerformance.AutoTestRunStatus;
import io.mycat.db.autotest.autoTestCheckPerformance.check.vo.CheckMsg;
import io.mycat.db.autotest.autoTestCheckPerformance.performance.vo.PerfromanceMsg;
import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.*;
import io.mycat.db.autotest.bean.annotation.FieldStep;
import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.bean.testgroup.Config;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.BeetlUtils;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.PathUtils;


public class UseCase extends AutoTestBaseBean implements AutoTestDataSource, TagServerType,CreateHtmlExec {


    public UseCase() {
        super(Arrays.asList("config", "fieldStep"), "useCase", null);
    }

    private Config config;

    @FieldStep(name = {"init", "check", "performance", "clean"})
    private List<AutoTestBaseBean> fieldStep = new ArrayList<>();

    @FieldType(childName = "connection", childType = io.mycat.db.autotest.bean.Connection.class)
    private List<io.mycat.db.autotest.bean.Connection> connections;

    private String path;

    private String depend;

    private boolean asyn;

    private boolean stauts = true;

    private boolean stautsCreateHtml = true;

    private transient List<CheckMsg> checks = new ArrayList<>();

    private transient List<PerfromanceMsg> perfromances = new ArrayList<>();

    public boolean isStautsCreateHtml() {
        return stautsCreateHtml;
    }

    public void setStautsCreateHtml(boolean stautsCreateHtml) {
        this.stautsCreateHtml = stautsCreateHtml;
    }

    public void setChecks(List<CheckMsg> checks) {
        this.checks = checks;
    }

    public void setPerfromances(List<PerfromanceMsg> perfromances) {
        this.perfromances = perfromances;
    }

    public boolean isStauts() {
        return stauts;
    }

    public void setStauts(boolean stauts) {
        this.stauts = stauts;
    }


    public String getDepend() {
        return depend;
    }

    public void setDepend(String depend) {
        this.depend = depend;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<AutoTestBaseBean> getFieldStep() {
        return fieldStep;
    }

    public void setFieldStep(List<AutoTestBaseBean> fieldStep) {
        this.fieldStep = fieldStep;
    }

    public String getPath() {
        return path;
    }

    public String doPath() {
        AutoTestBaseBean autoTestBaseBean = BeanFactory.getBeanById(this.getParentId());
        if(autoTestBaseBean instanceof TestGroupBaseBean){
            return ((TestGroupBaseBean) autoTestBaseBean).getPath()+"/"+path;
        }
        return "";
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

    public boolean isAsyn() {
        return asyn;
    }

    public void setAsyn(boolean asyn) {
        this.asyn = asyn;
    }

    public List<CheckMsg> getChecks() {
        return checks;
    }

    public void addChecks(List<CheckMsg> checks) {
        this.checks.addAll(checks);
    }

    public void addCheck(CheckMsg check) {
        this.checks.add(check);
    }

    public List<PerfromanceMsg> getPerfromances() {
        return perfromances;
    }

    public void addPerfromances(List<PerfromanceMsg> perfromances) {
        this.perfromances.addAll(perfromances);
    }

    public void addPerfromance(PerfromanceMsg perfromance) {
        this.perfromances.add(perfromance);
    }

    @Override
    public void initDataSource() {

    }

    @Override
    public java.sql.Connection getConnection(String name) throws SQLException {

        if(connections != null){
            for (io.mycat.db.autotest.bean.Connection conn : connections) {
                if (name.equals(conn.getId())) {
                    return getConnection(conn);
                }
            }
        }

        AutoTestBaseBean autoTestBaseBean = BeanFactory.getBeanById(this.getParentId());
        if (autoTestBaseBean instanceof AutoTestDataSource) {
            java.sql.Connection conn = ((AutoTestDataSource) autoTestBaseBean).getConnection(name);
            if (conn != null) {
                return conn;
            }
        }


        return null;
    }

    private static java.sql.Connection getConnection(io.mycat.db.autotest.bean.Connection conn1) {
        try {
            String url = conn1.getUrl();
            url = url.replace("${ip}", conn1.getHost());
            url = url.replace("${post}", conn1.getPost() + "");
            url = url.replace("${pid}", conn1.getDatabase());

            Class.forName(conn1.getDriver());
            Connection conn = DriverManager.getConnection(url, conn1.getUsername(), conn1.getPassword());
            return conn;
        } catch (Exception e) {
            LogFrameFile.getInstance().error("", e);
            return null;
        }
    }

    @Override
    public void close() throws Exception {

    }


    @Override
    public boolean exec() throws Exception {
        AutoTestBaseBean pAutoTestBaseBean = BeanFactory.getBeanById(this.getParentId());
        if(!AutoTestRunStatus.getUseCaseList().isEmpty() && !AutoTestRunStatus.isUseCaseList(this.getId())){
            if(!AutoTestRunStatus.isUseCaseList(pAutoTestBaseBean.getId())){
                this.setStautsCreateHtml(false);
                LogFrameFile.getInstance().debug("用例id="+this.getId()+"   , name="+this.getName()+"被忽略");
                return true;
            }
        }
        for (AutoTestBaseBean autoTestBaseBean : fieldStep) {
            if(((TestGroupBaseBean)pAutoTestBaseBean).getType() == 1 && autoTestBaseBean instanceof Performance ){
                continue;
            }
            if(((TestGroupBaseBean)pAutoTestBaseBean).getType() == 2 && autoTestBaseBean instanceof Check ){
                continue;
            }
            if(autoTestBaseBean instanceof TagServerType){

                ((TagServerType)autoTestBaseBean).exec();
            }
        }

        return true;
    }

    private static boolean createHtml( Map<String, Object> datas, String path) throws UnsupportedEncodingException {
        ProjectConfig projectConfig = BeanFactory.getProjectConfig();
        String outPath = PathUtils.getPath(projectConfig.getPath(),projectConfig.getOutPath());
        String templateid = "useCase.html";
        BeetlUtils.createpathTemplate(outPath + "/" + path,templateid,datas);
        return true;
    }

    @Override
    public boolean createHtml() {
        if(!this.isStautsCreateHtml()){
            return true;
        }
        AutoTestBaseBean stb = BeanFactory.getBeanById(this.getParentId());
        String path = "groupUseCase/"+stb.getId()+"/"+this.getId()+".html";
        Map<String, Object> datas = new HashMap<>();
        datas.put("useCase",this);
        try {
            createHtml(datas,path);
        } catch (UnsupportedEncodingException e) {
            LogFrameFile.getInstance().error("",e);
            return false;
        }
        return true;
    }
}
