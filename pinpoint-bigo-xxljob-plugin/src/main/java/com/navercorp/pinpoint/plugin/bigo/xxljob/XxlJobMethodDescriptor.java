package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;

/**
 * 插件描述定义
 * Created by whg on 7/28/2020.
 */
public class XxlJobMethodDescriptor implements MethodDescriptor {
    private int apiId = 0;

    private int type = 100;

    public String getMethodName() {
        return null;
    }

    public String getClassName() {
        return null;
    }

    public String[] getParameterTypes() {
        return new String[0];
    }

    public String[] getParameterVariableName() {
        return new String[0];
    }

    public String getParameterDescriptor() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    public String getFullName() {
        return XxlJobMethodDescriptor.class.getName();
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getApiId() {
        return this.apiId;
    }

    public String getApiDescriptor() {
        return "XXL_JOB_Process";
    }

    public int getType() {
        return this.type;
    }
}
