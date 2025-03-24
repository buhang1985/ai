package com.gjyy.ai.vo;

import java.time.LocalDateTime;

public class CallRecordsVO {
    /**
     * id
     */
    private int id;
    /**
     * 调用时间
     */
    private LocalDateTime call_time;
    /**
     * 调用计次
     */
    private int call_count;
    /**
     * 调用输入内容
     */
    private String call_input;
    /**
     * 调用IP
     */
    private String call_ip;
    /**
     * 调用用户编码
     */
    private String call_user_code;
    //get set 方法省略。

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCall_count() {
        return call_count;
    }

    public void setCall_count(int call_count) {
        this.call_count = call_count;
    }

    public LocalDateTime getCall_time() {
        return call_time;
    }

    public void setCall_time(LocalDateTime call_time) {
        this.call_time = call_time;
    }

    public String getCall_input() {
        return call_input;
    }

    public void setCall_input(String call_input) {
        this.call_input = call_input;
    }

    public String getCall_ip() {
        return call_ip;
    }

    public void setCall_ip(String call_ip) {
        this.call_ip = call_ip;
    }

    public String getCall_user_code() {
        return call_user_code;
    }

    public void setCall_user_code(String call_user_code) {
        this.call_user_code = call_user_code;
    }
}
