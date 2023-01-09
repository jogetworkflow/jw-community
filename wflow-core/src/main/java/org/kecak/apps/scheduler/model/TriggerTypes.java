package org.kecak.apps.scheduler.model;

public enum TriggerTypes {
	
	CRON("Cron"), Simple("Simple");
	
	private String code;

	TriggerTypes(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
