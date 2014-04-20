package com.wujy.dhuhelper.net;

import org.jsoup.nodes.Document;

/**
 * @author wujy http响应封装类
 */
public class MyHttpResponse {
	private Document data;
	private int pipIndex;

	public MyHttpResponse(int pipIndex, Document data) {
		super();
		this.pipIndex = pipIndex;
		this.data = data;
	}

	public Document getData() {
		return data;
	}

	public int getPipIndex() {
		return pipIndex;
	}

	public void setData(Document data) {
		this.data = data;
	}

	public void setPipIndex(int pipIndex) {
		this.pipIndex = pipIndex;
	}

}
