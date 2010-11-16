package selenesse;

import java.net.*;
import java.util.List;
import java.io.*;

/**
 * 
 * @author marisa
 * @author jeff.payne
 * 
 */
public class HttpUtils
{
	private static final String boundary = "7d021a37605f0";
	
	/**
	 * Makes a multipart file POST request to the given URL setting the given cookies. The
	 * `mediaTypes' List must contain a valid media type for every element of `filenames'.
	 * 
	 * @param url
	 * @param cookies
	 * @param mediaTypes
	 * @param filenames
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String postMultipartFiles(String url, String cookies, List<String> mediaTypes, List<String> filenames)
		throws MalformedURLException, IOException
	{
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setChunkedStreamingMode(1024);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		connection.setRequestProperty("Cookie", cookies);
		connection.setRequestMethod("POST");

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		// Write file contents to connection output stream.
		for (int i = 0; i < filenames.size(); i++)
		{
			File f = new File(filenames.get(i));
			// @formatter:off
			String str = "--" + boundary + "\r\n"
				+ "Content-Disposition: form-data;name=\"file" + i + "\"; filename=\"" + f.getName() + "\"\r\n"
				+ "Content-Type: " + mediaTypes.get(i) + "\r\n"
				+ "\r\n";
			// @formatter:on

			out.write(str.getBytes());

			FileInputStream uploadFileReader = new FileInputStream(f);
			int numBytesToRead = 1024;
			int availableBytesToRead;
			while ((availableBytesToRead = uploadFileReader.available()) > 0)
			{
				byte[] bufferBytesRead;
				bufferBytesRead = availableBytesToRead >= numBytesToRead ? new byte[numBytesToRead]
					: new byte[availableBytesToRead];
				uploadFileReader.read(bufferBytesRead);
				out.write(bufferBytesRead);
				out.flush();
			}
		}

		// Closing boundary.
		out.write(("--" + boundary + "--\r\n").getBytes());
		out.flush();
		out.close();

		// Get the response.
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	/**
	 * Makes a simple file POST request to the given URL setting the given cookies. The `mediaType'
	 * must be a valid media type `filename'.
	 * 
	 * @param url
	 * @param cookies
	 * @param mediaType
	 * @param filename
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String postSimpleFile(String url, String cookies, String mediaType, String filename)
		throws MalformedURLException, IOException
	{
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setChunkedStreamingMode(1024);
		connection.setRequestProperty("Content-Type", mediaType);
		connection.setRequestProperty("Cookie", cookies);
		connection.setRequestMethod("POST");

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		// Write file contents to connection output stream.
		File f = new File(filename);
		FileInputStream uploadFileReader = new FileInputStream(f);
		int numBytesToRead = 1024;
		int availableBytesToRead;
		while ((availableBytesToRead = uploadFileReader.available()) > 0)
		{
			byte[] bufferBytesRead;
			bufferBytesRead = availableBytesToRead >= numBytesToRead ? new byte[numBytesToRead]
				: new byte[availableBytesToRead];
			uploadFileReader.read(bufferBytesRead);
			out.write(bufferBytesRead);
			out.flush();
		}

		out.close();

		// Get the response.
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	public static String makeRequest(String requestType, String url, String cookie) throws Exception
	{
		StringBuilder response = new StringBuilder();
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setRequestMethod(requestType);
		connection.setRequestProperty("Cookie", cookie);
		connection.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}

		in.close();
		connection.disconnect();
		return response.toString();
	}

}
