package com.xc.oj.entity;

import java.util.List;
import java.util.Map;

public class JudgeResult {
    private Long submissionId;
    private Integer executeTime;
    private Integer executeMemory;
    private List<Map<String,String>> detail;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Integer getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Integer executeTime) {
        this.executeTime = executeTime;
    }

    public Integer getExecuteMemory() {
        return executeMemory;
    }

    public void setExecuteMemory(Integer executeMemory) {
        this.executeMemory = executeMemory;
    }

    public List<Map<String, String>> getDetail() {
        return detail;
    }

    public void setDetail(List<Map<String, String>> detail) {
        this.detail = detail;
    }
}
