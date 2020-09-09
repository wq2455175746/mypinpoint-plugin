package com.navercorp.pinpoint.plugin.bigo.xxljob;

import com.navercorp.pinpoint.common.trace.*;

public class XxlJobPluginConstants {
    public static final String XXL_JOB_SCOPE = "XXL_JOB_ASYNC";

    public static final ServiceType XXLJOB_SERVICE_TYPE = ServiceTypeFactory.of(1997, XXL_JOB_SCOPE);

    public static final AnnotationKey XXLJOB_ANNOTATION_KEY = AnnotationKeyFactory.of(993, "async.job", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String META_DO_NOT_TRACE = "_XXLJOB_DO_NOT_TRACE";

}
