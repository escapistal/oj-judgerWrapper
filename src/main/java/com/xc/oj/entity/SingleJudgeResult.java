package com.xc.oj.entity;

public class SingleJudgeResult {

    private JudgeResultEnum result;
    private Integer execTime;
    private Integer execMemory;

    public JudgeResultEnum getResult() {
        return result;
    }

    public void setResult(JudgeResultEnum result) {
        this.result = result;
    }

    public Integer getExecTime() {
        return execTime;
    }

    public void setExecTime(Integer execTime) {
        this.execTime = execTime;
    }

    public Integer getExecMemory() {
        return execMemory;
    }

    public void setExecMemory(Integer execMemory) {
        this.execMemory = execMemory;
    }
}
