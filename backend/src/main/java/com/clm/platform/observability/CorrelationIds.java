package com.clm.platform.observability;

import org.slf4j.MDC;

public final class CorrelationIds {

	public static final String MDC_KEY = "correlationId";

	public static final String REQUEST_ATTRIBUTE = CorrelationIds.class.getName() + ".correlationId";

	private CorrelationIds() {
	}

	public static String current() {
		return MDC.get(MDC_KEY);
	}
}
