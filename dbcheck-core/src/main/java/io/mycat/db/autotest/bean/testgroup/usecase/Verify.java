package io.mycat.db.autotest.bean.testgroup.usecase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.mycat.db.autotest.autoTestCheckPerformance.check.VerifyUtils;
import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.DataBaseUtils;
import io.mycat.db.autotest.utils.FileSqlUtils;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.PathUtils;
import org.apache.commons.beanutils.BeanUtils;

public class Verify extends AutoTestBaseBean implements TagServerType {

    private String verifySqlSrc = "select.sql";

    private String verifySql;

    private String verifyConnection;

    private boolean verifyOrder = true;

    private String verifyCheckfile = "check.xlsx";

    private String verifyCheckfileType = "excel";

    private String verifyDescription;

    public Verify() {
        super(Arrays.asList("verifySqlSrc", "verifySql", "verifyConnection", "verifyOrder", "verifyCheckfile", "verifyCheckfileType", "verifyDescription"), "verify", null);
    }

    public String getVerifySqlSrc() {
        return verifySqlSrc;
    }

    public void setVerifySqlSrc(String verifySqlSrc) {
        this.verifySqlSrc = verifySqlSrc;
    }

    public String getVerifyConnection() {
        return verifyConnection;
    }

    public void setVerifyConnection(String verifyConnection) {
        this.verifyConnection = verifyConnection;
    }

    public boolean isVerifyOrder() {
        return verifyOrder;
    }

    public void setVerifyOrder(boolean verifyOrder) {
        this.verifyOrder = verifyOrder;
    }

    public String getVerifyCheckfile() {
        return verifyCheckfile;
    }

    public void setVerifyCheckfile(String verifyCheckfile) {
        this.verifyCheckfile = verifyCheckfile;
    }

    public String getVerifyDescription() {
        return verifyDescription;
    }

    public void setVerifyDescription(String verifyDescription) {
        this.verifyDescription = verifyDescription;
    }

    public String getVerifyCheckfileType() {
        return verifyCheckfileType;
    }

    public void setVerifyCheckfileType(String verifyCheckfileType) {
        this.verifyCheckfileType = verifyCheckfileType;
    }

    public String getVerifySql() {
        return verifySql;
    }

    public void setVerifySql(String verifySql) {
        this.verifySql = verifySql;
    }

    public Connection getConn(UseCase useCase) {
        String conn = getVerifyConnection();
        //UseCase useCase = getUseCase();
        if (conn == null) {
            if(useCase.getConfig() != null){
                conn = useCase.getConfig().getDefualutConnection();
            }
        }
        try {
            return useCase.getConnection(conn);
        } catch (SQLException e) {
            LogFrameFile.getInstance().error("", e);
        }
        return null;
    }

    private AutoTestBaseBean getPerformanceOrTransaction(AutoTestBaseBean autoTestBaseBean) {
        if (autoTestBaseBean instanceof Performance || autoTestBaseBean instanceof Transaction) {
            return autoTestBaseBean;
        } else {
            return getPerformanceOrTransaction(BeanFactory.getBeanById(autoTestBaseBean.getParentId()));
        }
    }

    private UseCase getUseCase(AutoTestBaseBean atbbT) {
        if(atbbT == null){
            atbbT = this;
        }
        AutoTestBaseBean atbb = BeanFactory.getBeanById(atbbT.getParentId());
        if (atbb instanceof UseCase) {
            return (UseCase) atbb;
        } else {
            return getUseCase(atbb);
        }
    }

    @Override
    public boolean exec() throws SQLException {

        Verify sql = this;
        UseCase useCase = sql.getUseCase(null);
        Connection conn = null;
        //AutoTestBaseBean atbb = getPerformanceOrTransaction(this);
        try {
            conn = sql.getConn(useCase);
            String sqlStr = null;
            if (verifySql != null) {
                sqlStr = FileSqlUtils.getSqls(verifySql).get(0);
            } else {
                sqlStr = FileSqlUtils.getSqls(new File( PathUtils.getPath(useCase.getPath(), sql.getVerifySqlSrc()))).get(0);
            }

            return VerifyUtils.check(this,useCase,conn,sqlStr);
        } catch (IOException e) {
            LogFrameFile.getInstance().error("", e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return false;
    }
}
