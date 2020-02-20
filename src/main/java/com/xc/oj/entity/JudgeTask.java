package com.xc.oj.entity;

import java.io.Serializable;

public class JudgeTask implements Serializable {
    private Long submissionId;
    private String testcaseMd5;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String language;
    private String code;
    private String spjMd5;
    private Boolean lazyEval;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getTestcaseMd5() {
        return testcaseMd5;
    }

    public void setTestcaseMd5(String testcaseMd5) {
        this.testcaseMd5 = testcaseMd5;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSpjMd5() {
        return spjMd5;
    }

    public void setSpjMd5(String spjMd5) {
        this.spjMd5 = spjMd5;
    }

    public Boolean getLazyEval() {
        return lazyEval;
    }

    public void setLazyEval(Boolean lazyEval) {
        this.lazyEval = lazyEval;
    }
}
