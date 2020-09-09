package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by whg on 8/26/2020.
 */
public class XxlJobDetector {

    private static final String DEFAULT_EXPECTED_MAIN_CLASS = "com.xxl.job.core.biz.impl.ExecutorBizImpl.run";

    private List<String> expectedMainClasses;

    public XxlJobDetector(List<String> expectedMainClasses) {
        if (CollectionUtils.isEmpty(expectedMainClasses)) {
            this.expectedMainClasses = Collections.singletonList(DEFAULT_EXPECTED_MAIN_CLASS);
        } else {
            this.expectedMainClasses = expectedMainClasses;
        }
    }

    public boolean detect() {
        String bootstrapMainClass = MainClassCondition.INSTANCE.getValue();
        boolean isExpectedMainClass = expectedMainClasses.contains(bootstrapMainClass);
        if (isExpectedMainClass) {
            return true;
        }
        return false;
    }
}
