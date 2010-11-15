package selenesse;

import java.net.*;
import java.io.*;

public class URLConnectionReader {
	public static String getResponse(String requestType, String urlToOpen, String cookie) throws Exception {
		String response = "";
		URL url = new URL(urlToOpen);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(requestType);
		connection.setRequestProperty("Cookie", cookie);	
		connection.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {
			response = response + inputLine;
		}
		
		in.close();
		connection.disconnect();
		return response;
	}
}

