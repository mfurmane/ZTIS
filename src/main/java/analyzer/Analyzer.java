package analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import dbmock.DBMock;
import kotaku.Connector;

public class Analyzer {

	private static final String ARTICLES_URL = "localhost:5000";
	private static final String AUTHORS_URL = "localhost:5000";
	public static int lastAuth = 0;
	public static int lastArt = 0;
	public static List<String> authorsJSONs = new Vector<>();
	public static List<String> articlesJSONs = new Vector<>();

	public static void main(String[] args) {
		prepareFakeData();
		getData();
		for (String authorJSON : authorsJSONs) {
			JSONObject jsonObject = new JSONObject(authorJSON);
			System.out.println(jsonObject.get("name"));
		}
	}

	public static void getData() {
		getArticles();
		getAuthors();
	}

	public static void getArticles() {
		articlesJSONs = new Vector<>();
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet getRequest = new HttpGet(ARTICLES_URL);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response;
			response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String json = "";
			while ((output = br.readLine()) != null) {
				json+=output;
			}
			articlesJSONs.add(json);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getAuthors() {
		authorsJSONs = new Vector<>();
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet getRequest = new HttpGet(AUTHORS_URL);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response;
			response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String json = "";
			while ((output = br.readLine()) != null) {
				json+=output;
			}
			authorsJSONs.add(json);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void prepareFakeData() {
		mockKotaku();
		mockDestructoid();
		// waitAndSysoThings();
		while (true) {
			try {
				Thread.sleep(5000);
				System.out.println(DBMock.art.size() + " - " + DBMock.auth.size());
				if (DBMock.art.size() > 55 && DBMock.auth.size() > 20)
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		authorsJSONs = DBMock.auth;
		articlesJSONs = DBMock.art;
	}

	public static void waitAndSysoThings() {
		while (true) {
			try {
				Thread.sleep(5000);
				if (DBMock.art.size() > lastArt) {
					for (int i = lastArt; i < DBMock.art.size(); i++) {
						String json = DBMock.art.get(i);
						JSONObject jsonObject = new JSONObject(json);
						for (String key : jsonObject.keySet()) {
							System.out.println(key + " : " + jsonObject.get(key));
						}
						System.out.println();
					}
					lastArt = DBMock.art.size();
				}
				if (DBMock.auth.size() > lastAuth) {
					for (int i = lastAuth; i < DBMock.auth.size(); i++) {
						String json = DBMock.auth.get(i);
						JSONObject jsonObject = new JSONObject(json);
						for (String key : jsonObject.keySet()) {
							System.out.println(key + " : " + jsonObject.get(key));
						}
						System.out.println();
					}
					lastAuth = DBMock.auth.size();
				}
				// System.out.println("article count: "+DBMock.art.size()+" ###
				// authors count: "+DBMock.auth.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void mockKotaku() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Connector.main(new String[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void mockDestructoid() {
		new Thread(new Runnable() {
			public void run() {
				try {
					destructoid.Connector.main(new String[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
