package com.navercorp.pinpoint.plugin.bigo.brpc;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * 注册ServiceType和AnnotationKey
 * Created by whg on 7/28/2020.
 */
public class BrpcTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(BrpcConstants.BRPC_SERVER_SERVICE_TYPE);
        context.addServiceType(BrpcConstants.BRPC_CLIENT_SERVICE_TYPE);
        context.addServiceType(BrpcConstants.BRPC_SERVERSERVICE_NO_STATISTICS_TYPE);
        context.addAnnotationKey(BrpcConstants.BRPC_ARGS_ANNOTATION_KEY);
        context.addAnnotationKey(BrpcConstants.BRPC_RESULT_ANNOTATION_KEY);
        context.addAnnotationKey(BrpcConstants.BRPC_RPC_ANNOTATION_KEY);
    }
}
