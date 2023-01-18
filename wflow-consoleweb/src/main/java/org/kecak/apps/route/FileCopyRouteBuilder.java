package org.kecak.apps.route;

import org.apache.camel.builder.RouteBuilder;

@Deprecated
public class FileCopyRouteBuilder extends RouteBuilder {
	
	private String fromUri = "file:data/inbox?noop=true";
	private String toUri = "file:data/outbox";

	@Override
	public void configure() {
		String from = fromUri;
		String to = toUri;
		System.out.println("from :"+from);
		
		from(from).to(to);
	}

	public String getFromUri() {
		return fromUri;
	}

	public void setFromUri(String fromUri) {
		this.fromUri = fromUri;
	}

	public String getToUri() {
		return toUri;
	}

	public void setToUri(String toUri) {
		this.toUri = toUri;
	}
	
	
}