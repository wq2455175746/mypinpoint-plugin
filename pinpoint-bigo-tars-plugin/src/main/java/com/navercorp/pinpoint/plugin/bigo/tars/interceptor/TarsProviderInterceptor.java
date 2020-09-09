package com.navercorp.pinpoint.plugin.bigo.tars.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bigo.tars.TarsConstants;
import com.navercorp.pinpoint.plugin.bigo.tars.TarsProviderMethodDescriptor;
import com.qq.tars.net.core.Session;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;

import java.util.HashMap;
import java.util.Map;

import static com.navercorp.pinpoint.plugin.bigo.tars.TarsConstants.*;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsProviderInterceptor extends SpanRecursiveAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private static final String SCOPE_NAME = "##TARS_PROVIDER_TRACE";

    private static final MethodDescriptor TARS_PROVIDER_METHOD_DESCRIPTOR = (MethodDescriptor) new TarsProviderMethodDescriptor();

    public TarsProviderInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(TARS_PROVIDER_METHOD_DESCRIPTOR);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        TarsServantRequest request = (TarsServantRequest) args[0];
        this.logger.debug("doInBeforeTrace request:{}", request.getServantName() + ":" + request.getFunctionName());
        recorder.recordServiceType(TarsConstants.TARS_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(this.methodDescriptor);
        recorder.recordAttribute(TarsConstants.TARS_ARGS_ANNOTATION_KEY, request.getServantName() + ":" + request.getFunctionName());
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Trace trace = readRequestTrace(args);
        if (trace.canSampled()) {
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(TarsConstants.TARS_PROVIDER_SERVICE_TYPE);
            recorder.recordApi(TARS_PROVIDER_METHOD_DESCRIPTOR);
            recordRequest(recorder, target, args);
        }
        return trace;
    }

    private Trace readRequestTrace(Object[] args) {
        TarsServantRequest request = (TarsServantRequest) args[0];

        Map<String, String> context = request.getContext();
        if (context == null) {
            this.logger.error("context is empty!request:" + request.toString());
            context = new HashMap<String, String>();
        }
        if (context.get(META_DO_NOT_TRACE) != null) {
            return this.traceContext.disableSampling();
        }
        String transactionId = context.get(META_TRANSACTION_ID);
        if (transactionId == null)
            return this.traceContext.newTraceObject();
        long parentSpanID = NumberUtils.parseLong(context.get(META_PARENT_SPAN_ID), -1L);
        long spanID = NumberUtils.parseLong(context.get(META_SPAN_ID), -1L);
        short flags = NumberUtils.parseShort(context.get(META_FLAGS), (short) 0);
        TraceId traceId = this.traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
        return this.traceContext.continueTraceObject(traceId);
    }

    private void recordRequest(SpanRecorder recorder, Object target, Object[] args) {
        TarsServantRequest request = (TarsServantRequest) args[0];
        Session session = (Session) args[1];
        Map<String, String> context = request.getContext();
        if (context == null) {
            this.logger.error("context is empty!request:" + request.toString());
            context = new HashMap<String, String>();
        }
        recorder.recordRpcName(request.getServantName() + ":" + request.getFunctionName());
        recorder.recordEndPoint(session.getRemoteIp() + ":" + session.getRemotePort());
        if (session.getRemoteIp() != null) {
            recorder.recordRemoteAddress(session.getRemoteIp() + ":" + session.getRemotePort());
        } else {
            recorder.recordRemoteAddress("Unknown");
        }
        if (!recorder.isRoot()) {
            String parentApplicationName = context.get(META_PARENT_APPLICATION_NAME);
            if (parentApplicationName != null) {
                short parentApplicationType = NumberUtils.parseShort(context.get(META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED
                        .getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                String host = context.get(META_HOST);
                if (host != null) {
                    recorder.recordAcceptorHost(host);
                } else {
                    recorder.recordAcceptorHost(session.getRemoteIp() + ":" + session.getRemotePort());
                }
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        TarsServantRequest request = (TarsServantRequest) args[0];
        this.logger.debug("doInAfterTrace request:{}", request.getServantName() + ":" + request.getFunctionName());
        recorder.recordServiceType(TarsConstants.TARS_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(this.methodDescriptor);
        recorder.recordAttribute(TarsConstants.TARS_ARGS_ANNOTATION_KEY, request.getMethodParameters());
        if (throwable == null) {
            recorder.recordAttribute(TarsConstants.TARS_RESULT_ANNOTATION_KEY, result);
        } else {
            recorder.recordException(throwable);
        }
    }
}
