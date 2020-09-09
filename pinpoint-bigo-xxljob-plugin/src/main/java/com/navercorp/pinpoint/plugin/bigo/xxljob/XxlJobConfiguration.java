package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * Created by whg on 8/26/2020.
 */
public class XxlJobConfiguration {
    private final boolean xxljobEnabled;

    private final List<String> xxljobSpecialClasses;


    public XxlJobConfiguration(ProfilerConfig config) {
        this.xxljobEnabled = config.readBoolean("profiler.xxljob-pinpoint.enable", false);
        this.xxljobSpecialClasses = config.readList("profiler.xxljob.special.classes");

    }

    public boolean isXxlJobEnabled() {
        return this.xxljobEnabled;
    }


    public List<String> getXxlJobSpecialClasses() {
        return this.xxljobSpecialClasses;
    }


    public String toString() {
        return "XxlJobConfiguration{xxljobEnabled=" + this.xxljobEnabled + ", brpcSpecialClasses=" + this.xxljobSpecialClasses + '}';
    }
}
