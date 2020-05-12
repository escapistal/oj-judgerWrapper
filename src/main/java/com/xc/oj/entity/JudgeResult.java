package com.xc.oj.entity;

import java.util.List;

public class JudgeResult {
    private Long submissionId;
    private JudgeResultEnum result;
    private List<SingleJudgeResult> detail;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public JudgeResultEnum getResult() {
        return result;
    }

    public void setResult(JudgeResultEnum result) {
        this.result = result;
    }

    public List<SingleJudgeResult> getDetail() {
        return detail;
    }

    public void setDetail(List<SingleJudgeResult> detail) {
        this.detail = detail;
    }
}
