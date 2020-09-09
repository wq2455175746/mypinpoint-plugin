package com.navercorp.pinpoint.plugin.bigo.tars;

import com.navercorp.pinpoint.common.trace.*;

/**
 * Created by whg on 9/8/2020.
 */
public class TarsConstants {
    public static final ServiceType TARS_PROVIDER_SERVICE_TYPE = ServiceTypeFactory.of(1996, "TARS_PROVIDER", new ServiceTypeProperty[]{ServiceTypeProperty.RECORD_STATISTICS});

    public static final ServiceType TARS_CONSUMER_SERVICE_TYPE = ServiceTypeFactory.of(9994, "TARS_CONSUMER", new ServiceTypeProperty[]{ServiceTypeProperty.RECORD_STATISTICS});

    public static final ServiceType TARS_PROVIDER_SERVICE_NO_STATISTICS_TYPE = ServiceTypeFactory.of(9993, "TARS", new ServiceTypeProperty[0]);

    public static final AnnotationKey TARS_ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(990, "tars.args", new AnnotationKeyProperty[0]);

    public static final AnnotationKey TARS_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(991, "tars.result", new AnnotationKeyProperty[0]);

    public static final AnnotationKey TARS_RPC_ANNOTATION_KEY = AnnotationKeyFactory.of(992, "tars.rpc", new AnnotationKeyProperty[]{AnnotationKeyProperty.VIEW_IN_RECORD_SET});

    public static final String META_DO_NOT_TRACE = "_TARS_DO_NOT_TRACE";

    public static final String META_TRANSACTION_ID = "_TARS_TRASACTION_ID";

    public static final String META_SPAN_ID = "_TARS_SPAN_ID";

    public static final String META_PARENT_SPAN_ID = "_TARS_PARENT_SPAN_ID";

    public static final String META_PARENT_APPLICATION_NAME = "_TARS_PARENT_APPLICATION_NAME";

    public static final String META_PARENT_APPLICATION_TYPE = "_TARS_PARENT_APPLICATION_TYPE";

    public static final String META_FLAGS = "_TARS_FLAGS";

    public static final String META_HOST = "_TARS_HOST";

}
