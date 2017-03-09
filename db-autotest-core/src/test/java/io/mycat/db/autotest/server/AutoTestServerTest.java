package io.mycat.db.autotest.server;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by qiank on 2017/1/25.
 */
public class AutoTestServerTest {

    @Test
    public void strat() throws Exception {
       AutoTestServer ats = new AutoTestServer();
       //ats.strat(AutoTestServerTest.class.getClassLoader().getResource("test").getFile(),"queryExample","e://2",null);
        ats.strat(AutoTestServerTest.class.getClassLoader().getResource("test").getFile(),null,"e://2",null,true);
    }

}