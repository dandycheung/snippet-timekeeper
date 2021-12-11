package com.microsoft.snippet;

/**
 * POJO Alert!
 * Wraps all the information that can be returned through the library.
 * Custom implementation of {@link ExecutionPath} might/might not have to extend this to
 * accommodate any additional information.
 */
public class ExecutionContext {
    private String mClass;
    private String mMethod;
    private int mLineNo;
    private String mThreadName;
    private long mExecutionDuration;

    void setClassName(String clazz) {
        this.mClass = clazz;
    }

    void setMethod(String method) {
        this.mMethod = method;
    }

    void setLineNo(int line) {
        this.mLineNo = line;
    }

    void setThreadName(String threadName) {
        this.mThreadName = threadName;
    }

    void setExecutionDuration(long duration) {
        this.mExecutionDuration = duration;
    }

    public String getClassName() {
        return this.mClass;
    }

    public String getMethodName() {
        return this.mMethod;
    }

    public String getThreadName() {
        return this.mThreadName;
    }

    public int getLineNo() {
        return this.mLineNo;
    }

    public long getExecutionDuration() {
        return this.mExecutionDuration;
    }
}
