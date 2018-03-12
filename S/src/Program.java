import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.splunk.Args;
import com.splunk.Event;
import com.splunk.HttpService;
import com.splunk.Index;
import com.splunk.IndexCollection;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;
import com.splunk.ServiceArgs;


public class Program {

	private static String getPWD() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/tmp/splunk.p"))));
		String s;
		String password;
		while((s = br.readLine())!=null) {
			System.out.println("Password .... [OK]");
			password=s;
		}
		return s;
	}
	static Service service = null;
	// This is a template for new users of the Splunk SDK for Java.
	// The code below connects to a Splunk instance, runs a search,
	// and prints out the results in a crude form.
	public static void main(String[] args) throws IOException {
		// Create login parameters. We suggest finding
		// a better way to store these than hard coding
		// them in your program for production code.
		ServiceArgs serviceArgs = new ServiceArgs();
		serviceArgs.setUsername("admin");
		serviceArgs.setPassword(getPWD());
		serviceArgs.setHost("localhost");
		serviceArgs.setPort(8089);
		serviceArgs.setScheme("http");

		System.out.println("Connecting ...");
//		com.splunk.HttpService.set;
		// Create a Service instance and log in with the argument map
		service = Service.connect(serviceArgs);
		
		System.out.println("Connected");
//		readData1();
		writeDataOverTCP();
	}
	
	public static void readData1() {
		// Set the parameters for the search
				Args oneshotSearchArgs = new Args();
				// For a full list of options, see:
				//
				//     http://docs.splunk.com/Documentation/Splunk/latest/RESTAPI/RESTsearch#POST_search.2Fjobs

				// oneshotSearchArgs.put("earliest_time", "now-1w");
				// oneshotSearchArgs.put("latest_time",   "now");

				ResultsReaderXml resultsReader = null;
				InputStream resultsStream = service.oneshotSearch("search index=\"syslog\" | stats count by MAC",
						oneshotSearchArgs);

				try {
					resultsReader = new ResultsReaderXml(resultsStream);

					for (Event event : resultsReader) {
						// Process each event
						for (String key: event.keySet()) {
						   System.out.println(key + ": " + event.get(key));
						}
						System.out.println("");
					}

					resultsReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

	}
	
	static void writeDataOverTCP() {
		String indexName="test";
		IndexCollection indexCol = service.getIndexes();
		Index index = null;
		if(indexCol.get(indexName)==null) {
			indexCol.create(indexName);
			System.out.println("Index ["+indexName+"] created");
		}
		index=indexCol.get(indexName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String date = sdf.format(new Date());
		Socket socket = null;
		try {
			socket = index.attach();
			OutputStream outs = socket.getOutputStream();
			Writer wr = new OutputStreamWriter(outs, "UTF8");
			wr.write("Event 1! \r\n");
			wr.write("Event 2! \r\n");
			wr.write("Event 3! \r\n");
			wr.flush();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			;;;
		}
	}

}
