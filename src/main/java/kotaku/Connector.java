package kotaku;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dbmock.DBMock;

public class Connector {
	private static final String ARTICLES_URL = "https://jsonplaceholder.typicode.com/posts/1";
	private static final String AUTHORS_URL = "https://jsonplaceholder.typicode.com/posts/1";
	private static final boolean MOCK = true;

	static int day = 12;

	static int i = 0;
	static List<String> titles = new Vector<String>();
	static List<String> usedUrls = new Vector<String>();
	static Map<String, ArticleDTO> articles = new HashMap<String, ArticleDTO>();
	static Map<String, AuthorDTO> authors = new HashMap<String, AuthorDTO>();

	public static void exploreAuthor(String name) throws IOException {
		AuthorDTO dto = authors.get(name);
		Connection connect = null;
		if (dto.getUrl() != "") {
			connect = Jsoup.connect(dto.getUrl());
			Document document = connect.get();
			for (Element elem : document.select("div[style]")) {
				if (elem.attr("style").startsWith("background-image: url("))
					dto.setPhoto(elem.attr("style").replace("background-image: url(", "").replace(")", ""));
			}
		}

		// Elements e = document.select("div#cbuser_bio");
		// dto.setInfo(e.text());
		//
		// for (Element elem : document.select("img")) {
		// String link = elem.attr("src");
		// if (link.startsWith("https://www.destructoid.com/ul/user")) {
		// if (link.endsWith("300.jpg"))
		// dto.setPhoto(link);
		// else if (link.endsWith("cropped.jpg"))
		// dto.setBackground(link);
		// else
		// dto.setData(link);
		// }
		// }
	}

	public static String prepareDate(String input) {
		Date date = new Date();
		if (input.split(" ").length == 1)
			return (date.getMonth() + 1) + "/" + day + "/" + (date.getYear() - 100) + " " + input;
		return input.replace("Yesterday", (date.getMonth() + 1) + "/" + (day - 1) + "/" + (date.getYear() - 100))
				.replace("Today", (date.getMonth() + 1) + "/" + (day) + "/" + (date.getYear() - 100));

	}

	public static void exploreSite(String href) throws IOException {
		Connection connect = Jsoup.connect(href);
		Document document = connect.get();

		String toRemove = "kotaku Deadspin Fusion Gizmodo Jalopnik Jezebel Kotaku Lifehacker The Root Video The Bests Steamed Cosplay Compete Snacktaku Highlight Reel Podcast Review "
				+ document.title() + " ";
		String toRemove2 = "ShareTweet About Blog Need Help? Content Guide Kotaku Store Redirecting to the Kotaku store in Disclaimer: You are leaving a Gizmodo Media Group, LLC website and going to a third party site, which is subject to its own privacy policy and terms of use. Continue Permissions Privacy Terms of Use Advertising Jobs RSS ©2017 Gizmodo Media Group Kinja is in read-only mode. We are working to restore service.";
		String[] cleanedText = document.body().text().replace(toRemove, "").split("permalink ");

		String author = cleanedText[0].split("\\d")[0].replace("Yesterday", "").replace("Today", "")
				.replace("Monday", "").replace("Tuesday", "").replace("Wednesday", "").replace("Thursday", "")
				.replace("Friday", "").replace("Saturday", "").replace("Sunday", "").trim();
		if (!author.startsWith("kotaku")) {
			// System.out.println(
			// "####################################" + author +
			// "########################################");
			// System.out.println("#" + cleanedText[0]);
			// System.out.println("#" + cleanedText[0].replaceAll(author, ""));
			// System.out.println("#" + cleanedText[0].replaceAll(author,
			// "").split(" Filed to:")[0].trim());

			String date = prepareDate(cleanedText[0].replaceAll(author, "").split(" Filed to:")[0].trim());
			String filledTo = cleanedText[0].split(" Filed to:")[1]
					.replace("Edit Promote Share to Kinja Toggle Conversation tools Go to", "").trim();
			String content = cleanedText[1].replace(toRemove2, "").split(author)[0];

			ArticleDTO dto = new ArticleDTO();
			dto.setContent(content);
			dto.setDate(date);
			dto.setTitle(document.title());
			dto.setFilledTo(filledTo);

			if (authors.containsKey(author))
				dto.setAuthor(authors.get(author));
			else {
				String authorUrl = "";
				for (Element elem : document.select("a[title]")) {
					if (elem.attr("title").equals("Author Name"))
						authorUrl = elem.attr("href");
				}
				if (usedUrls.contains(authorUrl)) {
					for (String string : authors.keySet()) {
						if (authors.get(string).getUrl().equals(authorUrl))
							dto.setAuthor(authors.get(string));
					}
				} else {
					AuthorDTO authorDTO = new AuthorDTO();
					authorDTO.setName(author);
					authorDTO.setUrl(authorUrl);
					dto.setAuthor(authorDTO);
					authors.put(author, authorDTO);
					usedUrls.add(authorUrl);
				}
			}

			// System.out.println(document.title());
			dto.setData(document);
			articles.put(document.title(), dto);
		}

	}

	public static void main(String[] args) throws IOException {
		Connection connect = Jsoup.connect("http://kotaku.com/");
		// Connection connect2 = Jsoup.connect("http://kotaku.com/");
		Document document = connect.timeout(10000).get();
		Elements links = document.select("a");
		Set<String> hrefs = new HashSet<String>();

		for (Element elem : links) {
			String href = elem.attr("href");
			if (!href.contains("#") && !href.contains("rss") && href.startsWith("http://kotaku.com/"))
				hrefs.add(href);
		}

		for (String string : hrefs) {
			exploreSite(string);
		}

		for (String string : authors.keySet()) {
			if (!string.contains("kotaku")) {
				// System.out.println(string + " - " +
				// authors.get(string).getUrl());

				exploreAuthor(string);
			}
		}

		for (String string : articles.keySet()) {
			// System.out.println(articles.get(string).getAuthor().getName() + "
			// - " + articles.get(string).getTitle());
			// System.out.println(articles.get(string).getContent());
			// System.out.println(articles.get(string).getDate());
			// System.out.println();
		}

		HttpClient httpClient = HttpClientBuilder.create().build();

		for (String string : authors.keySet()) {
			if (!string.contains("kotaku")) {
				exploreAuthor(string);
				JSONObject author = new JSONObject(authors.get(string));

				saveAuthor(httpClient, author.toString());

//				System.out.println(author.toString());
			}
		}
		for (String string : articles.keySet()) {
			JSONObject artic = new JSONObject(articles.get(string));

			saveArticle(httpClient, artic.toString());

//			System.out.println(artic);
		}

	}

	public static void saveArticle(HttpClient httpClient, String artic) {
		if (MOCK) {
			DBMock.art.add(artic);
		} else
			try {
				HttpPost request = new HttpPost(ARTICLES_URL);
				StringEntity params = new StringEntity(artic.toString());
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(params);
				HttpResponse response = httpClient.execute(request);
				// handle response here...
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public static void saveAuthor(HttpClient httpClient, String auth) {
		if (MOCK) {
			DBMock.auth.add(auth);
		} else
			try {
				HttpPost request = new HttpPost(AUTHORS_URL);
				StringEntity params = new StringEntity(auth.toString());
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(params);
				HttpResponse response = httpClient.execute(request);
				// handle response here...
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
