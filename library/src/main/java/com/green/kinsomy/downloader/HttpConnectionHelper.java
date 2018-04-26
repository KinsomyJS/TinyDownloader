package com.green.kinsomy.downloader;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Created by kinsomy on 2018/4/25.
 */

class HttpConnectionHelper {

	private final static int TIMEOUT = 5*1000;
	/**
	 * 设置头部参数
	 *
	 * @throws ProtocolException
	 */
	static HttpURLConnection setConnectParam(HttpURLConnection conn)
			throws ProtocolException {
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
		StringBuilder accept = new StringBuilder();
		accept.append("image/gif, ")
				.append("image/jpeg, ")
				.append("image/pjpeg, ")
				.append("image/webp, ")
				.append("application/xml, ")
				.append("application/xaml+xml, ")
				.append("application/xhtml+xml, ")
				.append("application/x-shockwave-flash, ")
				.append("application/x-ms-xbap, ")
				.append("application/x-ms-application, ")
				.append("application/msword, ")
				.append("application/vnd.ms-excel, ")
				.append("application/vnd.ms-xpsdocument, ")
				.append("application/vnd.ms-powerpoint, ")
				.append("text/plain, ")
				.append("text/html, ")
				.append("*/*");
		conn.setRequestProperty("Accept", accept.toString());
		conn.setRequestProperty("Accept-Encoding", "identity");
		conn.setRequestProperty("Accept-Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(TIMEOUT);
		//httpurlconnection 自管理重定向
		conn.setInstanceFollowRedirects(true);
		return conn;
	}
}
