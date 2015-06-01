package cz.cvut.sigmet.gsmWebUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.cvut.sigmet.model.CellDTO;

public class GsmWebManagerImpl implements GsmWebManager {

	private static final Pattern MAP_PATTERN = Pattern.compile("http://www\\.mapy\\.cz/turisticka\\?x=(\\d\\d\\.\\d+?)&y=(\\d\\d\\.\\d+)");

	private DocumentBuilderFactory dbf;
	private XPathFactory xf;
	private XPath xPath;

	public GsmWebManagerImpl() {
		xf = XPathFactory.newInstance();
		xPath = xf.newXPath();
		dbf = DocumentBuilderFactory.newInstance();
	}

	// TODO EXCEPITON MAPPINGS
	@Override
	public CellDTO getGsmInfo(String CId, String lac) throws Exception {
		for (CellDTO g : getGsmInfo(CId)) {
			if (g.getLac().equals(lac)) {
				return g;
			}
		}
		throw new Exception("NO CID OR LAC CODE FOUND");
	}

	// TODO EXCEPITON MAPPINGS
	@Override
	public List<CellDTO> getGsmInfo(String cid) throws Exception {
		HttpResponse resp = getResponse(createRequest(cid));
		String res = getResponseString(resp);
		return parsePage(res, cid);
	}

	private List<CellDTO> parsePage(String s, String CId) throws Exception {
		Matcher m = Pattern.compile("<table>.*?</table>", Pattern.DOTALL).matcher(s);
		if (!m.find()) {
			throw new Exception("cannot find table");
		}
		s = m.group();
		s = s.replaceAll("<tr.*?>", "<tr>");
		s = s.replaceAll("<td.*?>", "<td>");
		s = s.replaceAll("(<[iI][mM][gG].*?>)", "");
		s = s.replaceAll("&nbsp;", "");

		InputStream is = new ByteArrayInputStream(s.getBytes());

		Document doc = dbf.newDocumentBuilder().parse(is);

		// String xpath =
		// String.format("/table/tr[td/a/@href='%s']/following-sibling::*[1]",
		// op.getGsmWebCode());
		String xpath = String.format("//tr[td[contains(text(), '%s')]]", CId);
		NodeList rows = (NodeList) xPath.evaluate(xpath, doc.getDocumentElement(), XPathConstants.NODESET);

		List<CellDTO> ret = new ArrayList<CellDTO>();
		for (int i = 0; i < rows.getLength(); i++) {
			Node n = rows.item(i);
			CellDTO g = null;
			EOperator eop = getOperator(n);
			if (eop.isLTE()) {
				g = parseLte(n);
			} else {
				g = parseGsm(n);
			}
			g.setCId(CId);
			ret.add(g);

		}

		return ret;
	}

	private HttpResponse getResponse(HttpPost request) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		response = client.execute(request);
		return response;
	}

	private HttpPost createRequest(String CId) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost("http://gsmweb.cz/search.php");
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("udaj", CId));
		urlParameters.add(new BasicNameValuePair("par", "cid"));
		urlParameters.add(new BasicNameValuePair("op", "all"));
		urlParameters.add(new BasicNameValuePair("razeni", "original"));
		urlParameters.add(new BasicNameValuePair("smer", "vzestupne"));

		request.setEntity(new UrlEncodedFormEntity(urlParameters));

		return request;
	}

	private CellDTO parseGsm(Node n) throws XPathExpressionException {	
		CellDTO ret = new CellDTO();
		NodeList tds = n.getChildNodes();

		// get link with latitude and longtitude
		String mapRef = getMapRefString(n);
		ret.setLatitude(getLatitude(mapRef));
		ret.setLongtitude(getLongtitude(mapRef));
		ret.setLac(tds.item(2).getTextContent());
		ret.setBch(tds.item(3).getTextContent());
		ret.setBsic(tds.item(4).getTextContent());
		ret.setDate(tds.item(5).getTextContent());
		ret.setAddres(tds.item(7).getTextContent());
		return ret;
	}

	private CellDTO parseLte(Node n) throws XPathExpressionException {
		CellDTO ret = new CellDTO();
		// String mapRef = getMapRefString(n);
		// ret.setLatitude(getLatitude(mapRef));
		// ret.setLongtitude(getLongtitude(mapRef));
		return ret;
	}

	private EOperator getOperator(Node n) throws XPathExpressionException {
		String oplink = null;
		// find header it contains a element in first cell of table
		do {
			n = n.getPreviousSibling();
		} while (n != null && (oplink = getOplink(n)) == null);
		return EOperator.getByGsmWebCode(oplink);
	}

	private String getOplink(Node n) throws XPathExpressionException {
		String ret = (String) xPath.evaluate("./td[1]/a/@href", n, XPathConstants.STRING);
		if (ret != null && !ret.isEmpty()) {
			return ret.replaceAll("/", "");
		}
		return null;
	}

	private String getMapRefString(Node n) throws XPathExpressionException {
		//return (String) xPath.evaluate("./td[9]/a/@href", n, XPathConstants.STRING);
		return n.getChildNodes().item(8).getFirstChild().getAttributes().getNamedItem("HREF").getTextContent();
	}

	// string format
	// href="http://www.mapy.cz/turisticka?x=14.938633333333&y=50.273241666667&z=15&source=coor&id=14.938633333333,50.273241666667"
	private double getLongtitude(String s) {
		Matcher m = MAP_PATTERN.matcher(s);
		if (m.find()) {
			return Double.parseDouble(m.group(1));
		}

		throw new RuntimeException("CAN NOTP PARSE LONGTITUDE");
	}

	private double getLatitude(String s) {
		Matcher m = MAP_PATTERN.matcher(s);
		if (m.find()) {
			return Double.parseDouble(m.group(2));
		}

		throw new RuntimeException("CAN NOTP PARSE LONGTITUDE");
	}

	private String getResponseString(HttpResponse response) throws IllegalStateException, IOException {
		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();

		rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		return result.toString();

	}

}
