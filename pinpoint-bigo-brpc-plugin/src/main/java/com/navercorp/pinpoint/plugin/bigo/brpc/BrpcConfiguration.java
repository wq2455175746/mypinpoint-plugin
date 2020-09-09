package com.navercorp.pinpoint.plugin.bigo.brpc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * Created by whg on 7/28/2020.
 */
public class BrpcConfiguration {
    private final boolean brpcEnabled;

    private final List<String> unTraceMethods;

    private final List<String> brpcSpecialClasses;


    public BrpcConfiguration(ProfilerConfig config) {
        this.brpcEnabled = config.readBoolean("profiler.brpc-pinpoint.enable", false);
        this.unTraceMethods = config.readList("profiler.brpc.untrace.methods");
        this.brpcSpecialClasses = config.readList("profiler.brpc.special.classes");
    }

    public List<String> getBrpcSpecialClasses() {
        return this.brpcSpecialClasses;
    }


    public boolean isBrpcEnabled() {
        return this.brpcEnabled;
    }

    public String toString() {
        return "BrpcConfiguration{brpcEnabled=" + this.brpcEnabled + ", brpcSpecialClasses=" + this.brpcSpecialClasses + ", unTraceMethods=" + this.unTraceMethods + '}';
    }
}
