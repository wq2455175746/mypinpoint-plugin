package com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.bigo.xxljob.XxlJobPluginConstants;

public class AsyncRunInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    protected final boolean isDebug;

    protected static final String ASYNC_TRACE_SCOPE = "##ASYNC_TRACE_SCOPE";

    protected final MethodDescriptor methodDescriptor;

    public AsyncRunInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.isDebug = this.logger.isDebugEnabled();

        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        } else if (methodDescriptor == null) {
            throw new NullPointerException("methodDescriptor must not be null");
        } else {
            this.methodDescriptor = methodDescriptor;
        }
    }

    @Override
    public void before(Object target, Object[] args) {
        if (this.isDebug) {
            this.logger.beforeInterceptor(target, args);
        }
        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            Trace trace = this.getAsyncTrace(asyncContext);
            if (trace != null) {
                this.entryAsyncTraceScope(trace);
                try {
                    SpanEventRecorder recorder = trace.traceBlockBegin();
                } catch (Throwable var6) {
                    logger.error("BEFORE. Caused:{}", var6.getMessage(), var6);
                }
            }
        }
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (this.isDebug) {
            this.logger.afterInterceptor(target, args, result, throwable);
        }

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            Trace trace = asyncContext.currentAsyncTraceObject();
            if (trace != null) {
                if (!this.leaveAsyncTraceScope(trace)) {
                    this.logger.warn("Failed to leave scope of async trace {}.", trace);
                    this.deleteAsyncContext(trace, asyncContext);
                } else {
                    try {
                        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                        recorder.recordServiceType(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
                        recorder.recordApi(methodDescriptor);
                        recorder.recordException(throwable);
                    } catch (Throwable var11) {
                        this.logger.warn("AFTER error. Caused:{}", var11.getMessage(), var11);
                    } finally {
                        trace.traceBlockEnd();
                        if (this.isAsyncTraceDestination(trace)) {
                            this.deleteAsyncContext(trace, asyncContext);
                        }
                    }
                }
            }
        }
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            this.logger.warn("Failed to continue async trace. 'result is null'");
            return null;
        }
        return trace;
    }

    private void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
        if (this.isDebug) {
            this.logger.debug("Delete async trace {}.", trace);
        }
        trace.close();
        asyncContext.close();
    }

    private void entryAsyncTraceScope(Trace trace) {
        TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            scope.tryEnter();
        }

    }

    private boolean leaveAsyncTraceScope(Trace trace) {
        TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            if (!scope.canLeave()) {
                return false;
            }
            scope.leave();
        }
        return true;
    }

    private boolean isAsyncTraceDestination(Trace trace) {
        if (!trace.isAsync()) {
            return false;
        } else {
            TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
            return scope != null && !scope.isActive();
        }
    }

}