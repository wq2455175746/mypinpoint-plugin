package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.VarArgs;
import com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor.AsyncConstructorInterceptor;
import com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor.AsyncInitInterceptor;
import com.navercorp.pinpoint.plugin.bigo.xxljob.interceptor.AsyncRunInterceptor;

import java.security.ProtectionDomain;

/**
 * 自定义插件实现增强xxljob全异步化
 * 主要增强三个异步类
 * 执行器:XxlJobExecutor
 * 任务调度线程:JobThread
 * 业务的执行线程:FutureTask
 * Created by whg on 8/26/2020.
 */
public class XxlJobPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        XxlJobConfiguration config = new XxlJobConfiguration(context.getConfig());

        if (!config.isXxlJobEnabled()) {
            logger.info("{} disabled", getClass().getSimpleName());
            return;
        }

        logger.info("{} config:{}", getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final XxlJobDetector xxlJobDetector = new XxlJobDetector(config.getXxlJobSpecialClasses());
            if (xxlJobDetector.detect()) {
                logger.info("Detected application type : {}", XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
                if (!context.registerApplicationType(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), XxlJobPluginConstants.XXLJOB_SERVICE_TYPE);
                }
            }
        }

        logger.info("Add XXL_JOB transformers");

        //--执行器 XxlJobExecutor->removeJobThread->JobThread
        transformTemplate.transform("com.xxl.job.core.executor.XxlJobExecutor", JobExecutorTransform.class);

        //--任务调度线程 JobThread.run()
        transformTemplate.transform("com.xxl.job.core.thread.JobThread", JobThreadTransform.class);

        //--业务执行线程FutureTask
        transformTemplate.transform("java.util.concurrent.FutureTask", FutureTransform.class);
    }


    public static class JobExecutorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws InstrumentException {
            InterceptorScope scope = instrumentor.getInterceptorScope(XxlJobPluginConstants.XXL_JOB_SCOPE);
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classFileBuffer);
            //获取声明的registJobThread方法
            InstrumentMethod registJobThread = target.getDeclaredMethod("registJobThread",
                    new String[]{"int", "com.xxl.job.core.handler.IJobHandler", "java.lang.String"});
            if (registJobThread != null) {
                registJobThread.addScopedInterceptor(AsyncInitInterceptor.class, scope);
            }
            return target.toBytecode();
        }
    }

    public static class JobThreadTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            //--构造方法 添加异步标识
            InstrumentMethod constructor = target.getConstructor(new String[]{"int", "com.xxl.job.core.handler.IJobHandler"});
            if (constructor != null) {
                InterceptorScope scope = instrumentor.getInterceptorScope(XxlJobPluginConstants.XXL_JOB_SCOPE);
                constructor.addScopedInterceptor(AsyncConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);
            }
            //--增强run方法
            InstrumentMethod run = target.getDeclaredMethod("run");
            if (run != null) {
                run.addInterceptor(AsyncRunInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class FutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod invokeMethod = target.getDeclaredMethod("get", new String[]{"long", "java.util.concurrent.TimeUnit"});

            if (invokeMethod != null) {
                invokeMethod.addInterceptor(BasicMethodInterceptor.class, VarArgs.va(XxlJobPluginConstants.XXLJOB_SERVICE_TYPE));
            }

            return target.toBytecode();
        }
    }

}
