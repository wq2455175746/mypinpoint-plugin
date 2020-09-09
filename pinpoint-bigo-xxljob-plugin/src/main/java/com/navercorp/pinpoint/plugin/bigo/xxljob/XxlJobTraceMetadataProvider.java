package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class XxlJobTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
        context.addAnnotationKey(XxlJobPluginConstants.XXLJOB_ANNOTATION_KEY);
    }
}
