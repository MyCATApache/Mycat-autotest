package io.mycat.db.autotest.exception;

public class AutoTestException extends RuntimeException {
    public AutoTestException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public AutoTestException(String msg) {
		super(msg);
	}
}