package com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.bigo.xxljob.XxlJobPluginConstants;

public class AsyncInitInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final MethodDescriptor descriptor;

    private final TraceContext traceContext;

    private final InterceptorScope scope;

    public AsyncInitInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
        recorder.recordApi(descriptor, new Object[]{args});
        AsyncContext asyncContext = recorder.recordNextAsyncContext();
        scope.getCurrentInvocation().setAttachment(asyncContext);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = this.traceContext.newTraceObject();
        }
        if (trace == null) {
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable != null) {
                recorder.recordServiceType(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
