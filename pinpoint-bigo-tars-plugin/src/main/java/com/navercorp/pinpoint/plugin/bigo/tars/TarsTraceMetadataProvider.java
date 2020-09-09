package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(TarsConstants.TARS_PROVIDER_SERVICE_TYPE);
        context.addServiceType(TarsConstants.TARS_CONSUMER_SERVICE_TYPE);
        context.addServiceType(TarsConstants.TARS_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        context.addAnnotationKey(TarsConstants.TARS_ARGS_ANNOTATION_KEY);
        context.addAnnotationKey(TarsConstants.TARS_RESULT_ANNOTATION_KEY);
        context.addAnnotationKey(TarsConstants.TARS_RPC_ANNOTATION_KEY);
    }
}
