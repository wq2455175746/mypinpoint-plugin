package com.navercorp.pinpoint.plugin.bigo.tars.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.bigo.tars.TarsConstants;
import com.qq.tars.client.rpc.ServantInvokeContext;
import com.qq.tars.client.rpc.ServantInvoker;

import static com.navercorp.pinpoint.plugin.bigo.tars.TarsConstants.*;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsConsumerInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final boolean isDebug = this.logger.isDebugEnabled();

    private final MethodDescriptor descriptor;

    private final TraceContext traceContext;

    public TarsConsumerInterceptor(MethodDescriptor descriptor, TraceContext traceContext) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (this.isDebug) {
            this.logger.beforeInterceptor(target, args);
        }
        ServantInvoker invoker = (ServantInvoker) target;
        ServantInvokeContext invokeContext = (ServantInvokeContext) args[0];
        this.logger.debug("before rpcName:{}", invoker.getUrl().getPath() + ":" + invokeContext.getMethodName());
       
        Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }
        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(TarsConstants.TARS_CONSUMER_SERVICE_TYPE);
            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            setAttachment(invokeContext, META_TRANSACTION_ID, nextId.getTransactionId());
            setAttachment(invokeContext, META_SPAN_ID, Long.toString(nextId.getSpanId()));
            setAttachment(invokeContext, META_PARENT_SPAN_ID, Long.toString(nextId
                    .getParentSpanId()));
            setAttachment(invokeContext, META_PARENT_APPLICATION_TYPE, Short.toString(this.traceContext
                    .getServerTypeCode()));
            setAttachment(invokeContext, META_PARENT_APPLICATION_NAME, this.traceContext
                    .getApplicationName());
            setAttachment(invokeContext, META_FLAGS, Short.toString(nextId.getFlags()));
            setAttachment(invokeContext, META_HOST, invoker.getUrl().getAddress());
        } else {
            setAttachment(invokeContext, META_DO_NOT_TRACE, "1");
        }
    }

    private void setAttachment(ServantInvokeContext context, String name, String value) {
        context.setAttachment(name, value);
        this.logger.debug("Set attachment {}={}", name, value);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (this.isDebug) {
            this.logger.afterInterceptor(target, args);
        }
        ServantInvoker invoker = (ServantInvoker) target;
        ServantInvokeContext invokeContext = (ServantInvokeContext) args[0];
        this.logger.debug("after rpcName:{}", invoker.getUrl().getPath() + ":" + invokeContext.getMethodName());

        Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(this.descriptor);
            if (throwable == null) {
                String endPoint = invoker.getUrl().getAddress();
                recorder.recordEndPoint(endPoint);
                recorder.recordDestinationId(endPoint);
                recorder.recordAttribute(TarsConstants.TARS_ARGS_ANNOTATION_KEY, invokeContext.getArguments());
                recorder.recordAttribute(TarsConstants.TARS_RESULT_ANNOTATION_KEY, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
