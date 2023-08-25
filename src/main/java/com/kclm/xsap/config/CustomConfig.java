package com.kclm.xsap.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Component
@PropertySource("classpath:xsap-config.properties")
public class CustomConfig {

	@Value("${reservation.gap_minute}")
	private Long gapMinute;

	@Value("${custom.cache_time}")
	private Long cacheTime;

	public CustomConfig() {
		super();
	}

	public void setGapMinute(Long gapMinute) {
		this.gapMinute = gapMinute;
	}

	public void setCacheTime(Long cacheTime) {
		this.cacheTime = cacheTime;
	}
}
