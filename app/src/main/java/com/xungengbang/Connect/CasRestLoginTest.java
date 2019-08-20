//package com.xungengbang.Connect;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import cn.cbsw.httpclient.HttpClientUtils;
//import cn.cbsw.httpclient.MyCloseableHttpClient;
//import cn.cbsw.httpclient.RedirectLocationStrategy;
//import cn.cbsw.spring.ContextUtil;
//
///**
// * 该类仅用于参考
// *
// * @author sun
// *
// */
//public class CasRestLoginTest {
//
//	protected static Logger log = LoggerFactory.getLogger(CasRestLoginTest.class);
//
//	public static void main(String[] args) {
//		CasRestLoginTest casRestLoginTest = new CasRestLoginTest();
//		casRestLoginTest.restLogin("http://14.23.69.2:1143/", "slj", "88888", "0", "111", "http://127.0.0.1:8103/",
//				"lte/authc/loginInfo");
//	}
//
//	private String location;
//	private String successUrl = "/lte/index";
//
//	public String getLocation() {
//		return location;
//	}
//
//	public void setSuccessUrl(String successUrl) {
//		this.successUrl = successUrl;
//	}
//
//	/**
//	 * https://apereo.github.io/cas/5.3.x/protocol/REST-Protocol.html
//	 *
//	 * @param casHost
//	 * @param username
//	 * @param password
//	 * @param authtype
//	 * @param authcode
//	 * @param clientHost
//	 * @param userinfoUri
//	 * @return
//	 */
//	public String restLogin(String casHost, String username, String password, String authtype, String authcode,
//			String clientHost, String userinfoUri) {
//		if (!casHost.endsWith("/")) {
//			casHost += "/";
//		}
//		if (!clientHost.endsWith("/")) {
//			clientHost += "/";
//		}
//		// 1获取TGT，TGT可以使用多次
//		MyCloseableHttpClient httpClient = HttpClientUtils.getInstance().getHttpClient(new RedirectLocationStrategy());
//		httpClient.setNeedResponseHeader(true);
//		httpClient.setUrl(casHost + "v1/tickets");
//		Map<String, Object> paras = new HashMap<>(4);
//		paras.put("username", username);
//		paras.put("password", password);
//		paras.put("authtype", authtype);
//		paras.put("authcode", authcode);
//		httpClient.setParas(paras);
//		Map<String, String> headers = new HashMap<>(1);
//		headers.put("Content-Type", "application/x-www-form-urlencoded");
//		httpClient.setHeaders(headers);
//		String tgt = httpClient.executeAsString();
//		log.debug("tgt:" + tgt);
//		// CAS will respond with a 400 Bad Request error
//		if (httpClient.getStatusCode() != 201) {
//			return null;
//		}
//		Map<String, String> respHeaders = httpClient.getRespHeaders();
//		location = respHeaders.get("Location");
//		log.debug("location:" + location);
//		// http://192.168.11.150:8000/v1/tickets/TGT-1-z4O3sEUCBukxahckI0DHYuDdEvCshb2TPZ2n2-BDa8S9C6muOF3dfP87VBflhez6RaEsun
//		if (location == null) {
//			return null;
//		}
//		// 2获取ST
//		httpClient.setNeedResponseHeader(false);
//		httpClient.setUrl(location);
//		paras.clear();
//		// header沿用,此处service必须与服务器端xml配置的loginUrl一致
//		paras.put("service", clientHost + "pac4j-cas?client_name=CasClient");
//		String st = httpClient.executeAsString();
//		log.debug("st:" + st);
//		// st只能使用一次
//		// 3校验ST
//		httpClient.setUrl(casHost+"p3/serviceValidate");
//		httpClient.setMethod("get");// 必须是get请求 //沿用service参数
//		paras.put("ticket", st);
//		String vs = httpClient.executeAsString();
//		// CAS will send a 400 Bad Request. If an incorrect media type issent,
//		// it will send the 415 Unsupported Media Type.
//		if (httpClient.getStatusCode() != 200) {
//			return null;
//		}
//		log.debug("validat result:" + vs);
//		System.out.println("validat result:" + vs);
//
//		// 3使用ST来登陆
//		/*httpClient.setUrl(clientHost + "pac4j-cas?client_name=CasClient&ticket=" + st);
//		httpClient.setMethod("get");// 必须是get请求才能登陆进去,post请求会到登陆页
//		st = httpClient.executeAsString();
//		String relocation = ((RedirectLocationStrategy) httpClient.getRedirectStrategy()).getLocation();
//		if (!successUrl.equals(relocation)) {
//			return null;
//		}
//		System.out.println("登陆成功,接下来获取登陆者信息");
//		// 4取得登陆后的信息返回给APP
//		httpClient.setUrl(clientHost + userinfoUri);
//		st = httpClient.executeAsString();
//		System.out.println(st);
//		log.debug("user info:" + st);*/
//		return st;
//	}
//
//	public String restLogout() {
//		MyCloseableHttpClient httpClient = ContextUtil.getBean(HttpClientUtils.class).getHttpClient();
//		httpClient.setUrl(location);
//		httpClient.setMethod("delete");
//		String st = httpClient.executeAsString();
//		log.debug("logout result:" + st);
//		// System.out.println(st);
//		return st;
//	}
//}
