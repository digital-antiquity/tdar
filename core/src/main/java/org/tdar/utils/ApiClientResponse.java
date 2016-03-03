package org.tdar.utils;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiClientResponse {
    private Logger logger = LoggerFactory.getLogger(getClass());

	private Header contentType;
	private String body;
	private Header[] headers;
	private StatusLine statusLine;
	private int statusCode;

	public ApiClientResponse(CloseableHttpResponse response) {
		this.statusCode = response.getStatusLine().getStatusCode();
		this.statusLine = response.getStatusLine();
		this.headers = response.getAllHeaders();
		try {
			this.body = IOUtils.toString(response.getEntity().getContent());
		} catch (UnsupportedOperationException | IOException e) {
			logger.error("error in response", e);
		}
		this.contentType = response.getEntity().getContentType();
	}

	public Header getContentType() {
		return contentType;
	}

	public void setContentType(Header contentType) {
		this.contentType = contentType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}

	public void setStatusLine(StatusLine statusLine) {
		this.statusLine = statusLine;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	
}
