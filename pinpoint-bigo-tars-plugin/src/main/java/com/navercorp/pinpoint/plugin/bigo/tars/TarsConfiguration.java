package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsConfiguration {
    private final boolean tarsEnabled;

    private final List<String> tarsSpecialClasses;

    public TarsConfiguration(ProfilerConfig config) {
        this.tarsEnabled = config.readBoolean("profiler.tars-pinpoint.enable", false);
        this.tarsSpecialClasses = config.readList("profiler.tars.special.classes");
    }

    public boolean isTarsEnabled() {
        return this.tarsEnabled;
    }

    public List<String> getTarsSpecialClasses() {
        return this.tarsSpecialClasses;
    }

    public String toString() {
        return "TafConfiguration{tarsEnabled=" + this.tarsEnabled + ", tarsSpecialClasses=" + this.tarsSpecialClasses + '}';
    }
}
