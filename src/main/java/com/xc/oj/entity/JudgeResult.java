package com.xc.oj.entity;

import java.util.List;

public class JudgeResult {
    private Long submissionId;
    private Integer timeLimit;
    private Integer memoryLimit;
    private List<SingleJudgeResult> detail;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
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

    public List<SingleJudgeResult> getDetail() {
        return detail;
    }

    public void setDetail(List<SingleJudgeResult> detail) {
        this.detail = detail;
    }
}
