package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsProviderDetector {
    private static final String DEFAULT_TAF_SPECIAL_CLASS = "com.qq.tars.server.core.Server";

    private List<String> expectedTafSpecialClasses;

    public TarsProviderDetector(List<String> expectedTafSpecialClasses) {
        if (CollectionUtils.isEmpty(expectedTafSpecialClasses)) {
            this.expectedTafSpecialClasses = Collections.singletonList(DEFAULT_TAF_SPECIAL_CLASS);
        } else {
            this.expectedTafSpecialClasses = expectedTafSpecialClasses;
        }
    }

    public boolean detect() {
        for (String expectClass : this.expectedTafSpecialClasses) {
            boolean isExpected = ClassResourceCondition.INSTANCE.check(expectClass);
            if (isExpected)
                return true;
        }
        return false;
    }
}
