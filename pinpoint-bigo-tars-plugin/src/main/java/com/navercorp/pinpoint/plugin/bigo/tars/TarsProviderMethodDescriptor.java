package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsProviderMethodDescriptor implements MethodDescriptor {
    private int apiId = 0;

    private int type = 100;

    @Override
    public String getMethodName() {
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String[] getParameterTypes() {
        return new String[0];
    }

    @Override
    public String[] getParameterVariableName() {
        return new String[0];
    }

    @Override
    public String getParameterDescriptor() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    public String getFullName() {
        return TarsProviderMethodDescriptor.class.getName();

    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getApiId() {
        return this.apiId;
    }

    public String getApiDescriptor() {
        return "Tars_Provider_Process";
    }

    public int getType() {
        return this.type;
    }
}
