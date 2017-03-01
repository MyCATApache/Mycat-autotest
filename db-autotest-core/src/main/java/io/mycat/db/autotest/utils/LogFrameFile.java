package io.mycat.db.autotest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogFrameFile {

    private Logger log = LoggerFactory.getLogger(LogFrameFile.class);

    private static final LogFrameFile Instance = new LogFrameFile();

    private LogFrameFile() {

    }

    public static LogFrameFile getInstance() {
        return Instance;
    }

    public void debug(String message, Throwable e) {
        log.debug(message, e);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void error(String message, Throwable e) {
        log.error(message, e);
    }

    public void error(String message) {
        log.error(message);
    }

    public void info(String message, Throwable e) {
        log.info(message, e);
    }

    public void info(String message) {
        log.info(message);
    }

    public void warn(String message, Throwable e) {
        log.warn(message, e);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }
}
