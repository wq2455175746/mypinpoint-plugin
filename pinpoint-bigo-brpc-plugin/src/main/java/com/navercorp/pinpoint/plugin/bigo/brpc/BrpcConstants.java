package com.navercorp.pinpoint.plugin.bigo.brpc;

import com.navercorp.pinpoint.common.trace.*;

/**
 * Created by whg on 7/28/2020.
 */
public class BrpcConstants {

    private BrpcConstants() {
    }

    public static final ServiceType BRPC_SERVER_SERVICE_TYPE = ServiceTypeFactory.of(1998, "BRPC_SERVER", new ServiceTypeProperty[]{ServiceTypeProperty.RECORD_STATISTICS});

    public static final ServiceType BRPC_CLIENT_SERVICE_TYPE = ServiceTypeFactory.of(9996, "BRPC_CLIENT", new ServiceTypeProperty[]{ServiceTypeProperty.RECORD_STATISTICS});

    public static final ServiceType BRPC_SERVERSERVICE_NO_STATISTICS_TYPE = ServiceTypeFactory.of(9995, "BRPC", new ServiceTypeProperty[0]);

    public static final AnnotationKey BRPC_ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(994, "brpc.args", new AnnotationKeyProperty[0]);

    public static final AnnotationKey BRPC_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(995, "brpc.result", new AnnotationKeyProperty[0]);

    public static final AnnotationKey BRPC_RPC_ANNOTATION_KEY = AnnotationKeyFactory.of(996, "brpc.rpc", new AnnotationKeyProperty[]{AnnotationKeyProperty.VIEW_IN_RECORD_SET});

    public static final String META_DO_NOT_TRACE = "_BRPC_DO_NOT_TRACE";

    public static final String META_TRANSACTION_ID = "_BRPC_TRASACTION_ID";

    public static final String META_SPAN_ID = "_BRPC_SPAN_ID";

    public static final String META_PARENT_SPAN_ID = "_BRPC_PARENT_SPAN_ID";

    public static final String META_PARENT_APPLICATION_NAME = "_BRPC_PARENT_APPLICATION_NAME";

    public static final String META_PARENT_APPLICATION_TYPE = "_BRPC_PARENT_APPLICATION_TYPE";

    public static final String META_FLAGS = "_BRPC_FLAGS";

    public static final String META_HOST = "_BRPC_HOST";

}
