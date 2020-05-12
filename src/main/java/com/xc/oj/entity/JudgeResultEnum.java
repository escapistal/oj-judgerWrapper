package com.xc.oj.entity;

public enum JudgeResultEnum {
    PENDING("Pending"),
    JUDGING("Judging"),
    AC("Accepted"),
    WA("Wrong Answer"),
    TLE("Time Limit Exceed"),
    MLE("Memory Limit Exceed"),
    RE("Runtime Error"),
    PE("Presentation Error"),
    CE("Compile Error"),
    SE("System Error")
    ;

    private String res;

    JudgeResultEnum(String res) {
        this.res=res;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }
}
