package leeloo.dallas.multipass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wind implements Element {
	Logger log = LoggerFactory.getLogger(this.getClass());
	private static Wind instance = null;
	private static String FILE = "FILE";
	private static String FINGERPRINT = "fingerprint";
	private static String DURATION = "duration";
	private static String CLIENT = "client";
	private static String CLIENT_KEY="WKASbrzJ";
	private static String META = "meta";
	private static String META_DATA_PARAMS = "recordings+releasegroups";
	protected Wind() {
		// Exists only to defeat instantiation.
	}

	public static Element getStone() {
		if (instance == null) {
			instance = new Wind();
		}
		return instance;
	}

	@Override
	public void process(File file) {
		URL CMD = getClass().getResource("/fpcalc.exe");
		String[] command = { CMD.getPath(), file.getAbsolutePath() };
		ProcessBuilder probuilder = new ProcessBuilder(command);
		try {
			Process process = probuilder.start();

			// Read out dir output
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			Map<String, String> fpMap = new HashMap<>();
			while ((line = br.readLine()) != null) {
				log.info(line);
				String[] rawInfo = line.split("=");
				fpMap.put(rawInfo[0].toLowerCase(), rawInfo[1]);
			}

			int exitValue = process.waitFor();
			log.debug("\n\nExit Value is " + exitValue);
			
			String request = "http://api.acoustid.org/v2/lookup";


				URI uri = new URIBuilder()
				.setScheme("http")
				.setHost("api.acoustid.org")
				.setPath("/v2/lookup")
				.setParameter(CLIENT,CLIENT_KEY)
				.setParameter(META,META_DATA_PARAMS)
				.setParameter(DURATION,fpMap.get(DURATION))
				.setParameter(FINGERPRINT,fpMap.get(FINGERPRINT))
				.build();
				HttpClient httpclient = HttpClients.createDefault();
				HttpPost httppost = new HttpPost(uri);
				HttpResponse response = httpclient.execute(httppost);
		handleResponse(response);

		    // Add POST parameters
//		    method.addParameter(CLIENT,CLIENT_KEY);
//		    method.addParameter(META,META_DATA_PARAMS);
//		    method.addParameter(DURATION,fpMap.get(DURATION));
//		    method.addParameter(FINGERPRINT,fpMap.get(FINGERPRINT));
//		    
//		    log.info(Arrays.toString(method.getParameters()));
		    // Send POST request
//method.validate();
//		    int statusCode = client.executeMethod(method);
//		    System.out.println(statusCode);

		 //   InputStream rstream = null;

		    
		    // Get the response body
//		    rstream = method.getResponseBodyAsStream();
//		    String linetest="";
//		    while(linetest!=null)
//			{linetest = test.readLine();System.out.println(linetest);}

	
	
		    
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//		    response.close();
		}

	}
    public void handleResponse(
            final HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
     //   Gson gson = new GsonBuilder().create();
        ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset();
        InputStreamReader reader = new InputStreamReader(entity.getContent(), charset);
        BufferedReader br = new BufferedReader(reader);
        String linetest = br.readLine();
	    while(linetest!=null)
		{linetest = br.readLine();System.out.println(linetest);}
       // return gson.fromJson(reader, MyJsonObject.class);
    }

}
