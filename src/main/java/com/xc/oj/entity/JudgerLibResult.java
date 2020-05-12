package com.xc.oj.entity;

public class JudgerLibResult {
    int cpu_time;
    int real_time;
    long memory;
    int signal;
    int exit_code;
    int result;

    public int getCpu_time() {
        return cpu_time;
    }

    public void setCpu_time(int cpu_time) {
        this.cpu_time = cpu_time;
    }

    public int getReal_time() {
        return real_time;
    }

    public void setReal_time(int real_time) {
        this.real_time = real_time;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public int getExit_code() {
        return exit_code;
    }

    public void setExit_code(int exit_code) {
        this.exit_code = exit_code;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
