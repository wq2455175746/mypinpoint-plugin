package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bigo.tars.interceptor.TarsConsumerInterceptor;
import com.navercorp.pinpoint.plugin.bigo.tars.interceptor.TarsProviderInterceptor;

import java.security.ProtectionDomain;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        TarsConfiguration config = new TarsConfiguration(context.getConfig());
        if (!config.isTarsEnabled()) {
            this.logger.info("{} disabled", getClass().getSimpleName());
            return;
        }
        this.logger.info("{} config:{}", getClass().getSimpleName(), config);
        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            TarsProviderDetector tarsProviderDetector = new TarsProviderDetector(config.getTarsSpecialClasses());
            if (tarsProviderDetector.detect()) {
                this.logger.info("Detected application type : {}", TarsConstants.TARS_PROVIDER_SERVICE_TYPE);
                if (!context.registerApplicationType(TarsConstants.TARS_PROVIDER_SERVICE_TYPE))
                    this.logger.info("Application type [{}] already set, skipping [{}] registration.", context
                            .getApplicationType(), TarsConstants.TARS_PROVIDER_SERVICE_TYPE);
            }
        }
        this.logger.info("Adding Tars transformers");
        addTransformers();
    }

    private void addTransformers() {
        this.transformTemplate.transform("com.qq.tars.server.core.TarsServantProcessor", ServantProcessorTransform.class);
        this.transformTemplate.transform("com.qq.tars.client.rpc.tars.TarsInvoker", ServantInvokerTransform.class);
    }

    public static class ServantProcessorTransform implements TransformCallback {
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("process", new String[]{"com.qq.tars.net.core.Request", "com.qq.tars.net.core.Session"});
            if (invokeMethod != null)
                invokeMethod.addInterceptor(TarsProviderInterceptor.class);
            return target.toBytecode();
        }
    }

    public static class ServantInvokerTransform implements TransformCallback {
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("doInvokeServant", new String[]{"com.qq.tars.client.rpc.ServantInvokeContext"});
            if (invokeMethod != null)
                invokeMethod.addInterceptor(TarsConsumerInterceptor.class);
            return target.toBytecode();
        }
    }

}
