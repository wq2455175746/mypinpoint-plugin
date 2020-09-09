package com.navercorp.pinpoint.plugin.bigo.brpc.interceptor;

import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.bigo.brpc.BrpcConstants;

import java.util.HashMap;

/**
 * client send rpc request
 * Created by whg on 7/28/2020.
 */
public class BrpcClientInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final boolean isDebug = this.logger.isDebugEnabled();

    private final MethodDescriptor descriptor;

    private final TraceContext traceContext;

    public BrpcClientInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (this.isDebug) {
            this.logger.beforeInterceptor(target, args);
        }

        Request request = (Request) args[0];

        logger.debug("brpc_client before rpcName:{}", request.getServiceName() + ":" + request.getMethodName());

        Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }

        if (request.getKvAttachment() == null) {
            request.setKvAttachment(new HashMap<String, Object>());
        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(BrpcConstants.BRPC_CLIENT_SERVICE_TYPE);
            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            request.getKvAttachment().put(BrpcConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            request.getKvAttachment().put(BrpcConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            request.getKvAttachment().put(BrpcConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));

            request.getKvAttachment().put(BrpcConstants.META_PARENT_APPLICATION_TYPE, Short.toString(this.traceContext.getServerTypeCode()));
            request.getKvAttachment().put(BrpcConstants.META_PARENT_APPLICATION_NAME, this.traceContext.getApplicationName());

            request.getKvAttachment().put(BrpcConstants.META_FLAGS, Short.toString(nextId.getFlags()));

            request.getKvAttachment().put(BrpcConstants.META_HOST, Utlis.getRemoteHost(request));

        } else {
            request.getKvAttachment().put(BrpcConstants.META_DO_NOT_TRACE, "1");
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (this.isDebug) {
            this.logger.afterInterceptor(target, args);
        }

        final Request request = (Request) args[0];

        logger.debug("brpc_client after rpcName:{}", request.getServiceName() + ":" + request.getMethodName());

        Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable == null) {
                Response response = (Response) args[1];
                String remoteHost = Utlis.getRemoteHost(request);
                recorder.recordEndPoint(remoteHost);
                recorder.recordDestinationId(remoteHost);
                recorder.recordAttribute(BrpcConstants.BRPC_ARGS_ANNOTATION_KEY, request.getArgs());
                recorder.recordAttribute(BrpcConstants.BRPC_RESULT_ANNOTATION_KEY, response);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
