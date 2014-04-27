package org.sjutas.dhu.net;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

/**
 * @author wujy http请求封装类
 */
public class MyHttpRequest {
	public boolean isBlock;
	private ArrayList<NameValuePair> params;
	private int pipIndex = 0;
	private int type = 0;
	private String url;

	/**
	 * 
	 * @param 1为get,2为post
	 * @param url
	 * @param params
	 * @param isBlock
	 */
	public MyHttpRequest(int type, String url, ArrayList<NameValuePair> params,
			boolean isBlock) {
		super();
		this.type = type;
		this.url = url;
		this.params = params;
		this.isBlock = isBlock;
	}

	public ArrayList<NameValuePair> getParams() {
		return params;
	}

	public int getPipIndex() {
		return pipIndex;
	}

	public int getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public void setParams(ArrayList<NameValuePair> params) {
		this.params = params;
	}

	public void setPipIndex(int pipIndex) {
		this.pipIndex = pipIndex;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
