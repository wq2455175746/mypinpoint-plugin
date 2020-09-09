package com.navercorp.pinpoint.plugin.bigo.brpc;

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
import com.navercorp.pinpoint.plugin.bigo.brpc.interceptor.BrpcClientInterceptor;
import com.navercorp.pinpoint.plugin.bigo.brpc.interceptor.BrpcServerInterceptor;

import java.security.ProtectionDomain;

/**
 * 插件类增强
 * Created by whg on 7/28/2020.
 */
public class BrpcPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;

    private static final String BRPC_REQUEST_REFER = "com.baidu.brpc.protocol.Request";

    private static final String BRPC_RESPONSE_REFER = "com.baidu.brpc.protocol.Response";

    private static final String BRPC_INTERCEPTORCHAIN_REFER = "com.baidu.brpc.interceptor.InterceptorChain";

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        //--对类增强
        BrpcConfiguration config = new BrpcConfiguration(context.getConfig());

        if (!config.isBrpcEnabled()) {
            logger.info("{} disabled", getClass().getSimpleName());
            return;
        }

        logger.info("{} config:{}", getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final BrpcServerDetector brpcServerDetector = new BrpcServerDetector(config.getBrpcSpecialClasses());
            if (brpcServerDetector.detect()) {
                logger.info("Detected application type : {}", BrpcConstants.BRPC_SERVER_SERVICE_TYPE);
                if (!context.registerApplicationType(BrpcConstants.BRPC_SERVER_SERVICE_TYPE)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), BrpcConstants.BRPC_SERVER_SERVICE_TYPE);
                }
            }
        }

        logger.info("Add BRPC transformers");

        transformTemplate.transform("com.baidu.brpc.interceptor.ClientInvokeInterceptor", ClientInvokeTransform.class);

        transformTemplate.transform("com.baidu.brpc.interceptor.ServerInvokeInterceptor", ServerInvokeTransform.class);
    }


    public static class ClientInvokeTransform implements TransformCallback {
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
                                    String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("aroundProcess",
                    new String[]{BRPC_REQUEST_REFER, BRPC_RESPONSE_REFER, BRPC_INTERCEPTORCHAIN_REFER});
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(BrpcClientInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ServerInvokeTransform implements TransformCallback {
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("aroundProcess",
                    new String[]{BRPC_REQUEST_REFER, BRPC_RESPONSE_REFER, BRPC_INTERCEPTORCHAIN_REFER});
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(BrpcServerInterceptor.class);
            }
            return target.toBytecode();
        }
    }


}
