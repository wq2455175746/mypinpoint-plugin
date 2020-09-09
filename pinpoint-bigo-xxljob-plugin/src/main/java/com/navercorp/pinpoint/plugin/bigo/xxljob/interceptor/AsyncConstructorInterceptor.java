package com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

public class AsyncConstructorInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final InterceptorScope scope;

    public AsyncConstructorInterceptor(InterceptorScope scope) {
        this.scope = scope;
    }

    @IgnoreMethod
    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        AsyncContext asyncContext = (AsyncContext) scope.getCurrentInvocation().getAttachment();
        ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
    }
}
