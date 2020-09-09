package com.navercorp.pinpoint.plugin.bigo.brpc.interceptor;

import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bigo.brpc.BrpcConstants;
import com.navercorp.pinpoint.plugin.bigo.brpc.BrpcServerMethodDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * server
 * Created by whg on 7/28/2020.
 */
public class BrpcServerInterceptor extends SpanRecursiveAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private static final String SCOPE_NAME = "##BRPC_SERVER_TRACE";

    private static final MethodDescriptor BRPC_SERVER_METHOD_DESCRIPTOR = new BrpcServerMethodDescriptor();

    public BrpcServerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(BRPC_SERVER_METHOD_DESCRIPTOR);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Trace trace = readRequestTrace(args);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(BrpcConstants.BRPC_SERVER_SERVICE_TYPE);
            recorder.recordApi(BRPC_SERVER_METHOD_DESCRIPTOR);
            recordRequest(recorder, args);
        }
        return trace;
    }

    private Trace readRequestTrace(Object[] args) {
        Request request = (Request) args[0];

        Map<String, Object> context = request.getKvAttachment();
        if (context == null) {
            logger.debug("brpc_server context is empty!request:" + request.toString());
            context = new HashMap<String, Object>();
        }

        if (context.get(context.get(BrpcConstants.META_DO_NOT_TRACE)) != null) {
            return traceContext.disableSampling();
        }

        Object transactionId = context.get(BrpcConstants.META_TRANSACTION_ID);
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        long parentSpanID = NumberUtils.parseLong(context.get(BrpcConstants.META_PARENT_SPAN_ID).toString(), -1L);
        long spanID = NumberUtils.parseLong(context.get(BrpcConstants.META_SPAN_ID).toString(), -1L);
        short flags = NumberUtils.parseShort(context.get(BrpcConstants.META_FLAGS).toString(), (short) 0);
        TraceId traceId = traceContext.createTraceId(transactionId.toString(), parentSpanID, spanID, flags);

        logger.debug("brpc_server readReq TraceId:{},request:{}", traceId, request.getServiceName() + ":" + request.getMethodName());

        return this.traceContext.continueTraceObject(traceId);
    }

    private void recordRequest(SpanRecorder recorder, Object[] args) {
        Request request = (Request) args[0];
        Map<String, Object> context = request.getKvAttachment();
        if (context == null) {
            logger.debug("brpc_server context is empty!request:" + request.toString());
            context = new HashMap<String, Object>();
        }
        recorder.recordRpcName(request.getServiceName() + ":" + request.getMethodName());

        String localHost = Utlis.getLocalHost(request);
        String remoteHost = Utlis.getRemoteHost(request);
        recorder.recordEndPoint(localHost);
        recorder.recordRemoteAddress(remoteHost);

        if (!recorder.isRoot()) {
            Object parentApplicationName = context.get(BrpcConstants.META_PARENT_APPLICATION_NAME);
            if (parentApplicationName != null) {
                final short parentApplicationType = NumberUtils.parseShort(context.get(
                        BrpcConstants.META_PARENT_APPLICATION_TYPE).toString(), ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName.toString(), parentApplicationType);
                Object host = context.get(BrpcConstants.META_HOST);
                if (host != null && !host.equals("unKnown")) {
                    recorder.recordAcceptorHost(host.toString());
                } else {
                    recorder.recordAcceptorHost(localHost);
                }
            }
        }

        logger.debug("brpc_server record rpcName:{},endpoint:{},remoteAddr:{},acceptHost:{}", new Object[]{request.getServiceName() + ":" + request.getMethodName(), localHost, remoteHost, localHost});
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        Request request = (Request) args[0];
        logger.debug("brpc_server doInBeforeTrace request:{}", request.getServiceName() + ":" + request.getMethodName());
        recorder.recordServiceType(BrpcConstants.BRPC_SERVERSERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(BrpcConstants.BRPC_ARGS_ANNOTATION_KEY, request.getServiceName() + ":" + request.getMethodName());
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        Request request = (Request) args[0];
        logger.debug("brpc_server doInAfterTrace request:{}", request.getServiceName() + ":" + request.getMethodName());
        recorder.recordServiceType(BrpcConstants.BRPC_SERVERSERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(BrpcConstants.BRPC_ARGS_ANNOTATION_KEY, request.getArgs());
        if (throwable == null) {
            Response response = (Response) args[1];
            recorder.recordAttribute(BrpcConstants.BRPC_RESULT_ANNOTATION_KEY, response);
        } else {
            recorder.recordException(throwable);
        }
    }
}
