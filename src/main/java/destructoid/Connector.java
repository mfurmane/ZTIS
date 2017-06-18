package destructoid;

import java.io.IOException;
import java.util.Collections;
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
	static int i = 0;
	static List<String> titles = new Vector<String>();
	static Map<String, ArticleDTO> articles = new HashMap<String, ArticleDTO>();
	static Map<String, AuthorDTO> authors = new HashMap<String, AuthorDTO>();

	public static void exploreAuthor(String name) throws IOException {
		AuthorDTO dto = authors.get(name);
		// System.out.println(name);
		Connection connect = Jsoup.connect(dto.getUrl());
		Document document = connect.timeout(10000).get();

		Elements e = document.select("div#cbuser_bio");
		dto.setInfo(e.text());

		for (Element elem : document.select("img")) {
			String link = elem.attr("src");
			if (link.startsWith("https://www.destructoid.com/ul/user")) {
				if (link.endsWith("300.jpg"))
					dto.setPhoto(link);
				else if (link.endsWith("cropped.jpg"))
					dto.setBackground(link);
				else
					dto.setData(link);
			}
		}
	}

	public static void exploreSite(String href) throws IOException {
		Connection connect = Jsoup.connect("http://www.destructoid.com/" + href);
		Document document = null;
		// try {
		document = connect.timeout(10000).get();
		// } catch (IOException e) {
		// exploreSite(href);
		// }

		Element commentDiv = document.select("div#disqus_thread").first();
		// System.out.println(document.title());
		// for (Element elem : commentDiv.children()) {
		// System.out.println(elem);
		// }

		// Element commentDiv = document.select("div#disqus_thread").first();
		// System.out.println("###");
		// for (Element elem : document.select("body")) {
		// System.out.println("########");
		// }

		String toRemove = "Gaming News, Game Reviews, Game Trailers, E3 News about usprivacysupport / contact   Log in         Sign up   HOT DEALS REVIEWS VIDEOS COMMUNITY GENRE Action Indie Free games Fighting MMO Music Platformers Puzzle Racing RPGs Sports Shooters Strategy Virtual Reality More tags DEVICE PC Switch PS4 Xbox One Wii U 3DS PS Vita iOS Android Xbox 360 PS3 Mac More PC   |   SWITCH   |   PS4   |   XBOX   |   SCORPIO   |   3DS   |   VITA   |   VR   |   JP   |   FILM   |   TOYS   |   MOB   Close Contest: Win a Nintendo Switch from Destructoid Can't see comments? EasyList, Avast, and other blockers break Disqus. Tweak your extensions, brosef.   ";
		String string = document.body().text().replace(toRemove, "").replace((document.title() + " "), "")
				.split("You are logged out.")[0];
		if (string.contains("  ·  ")) {
			ArticleDTO dto = new ArticleDTO();
			String[] parts = string.split("\\s*·\\s*");
			String tmpDate = parts[0];
			String author;
			String article;
			if (tmpDate.endsWith(" "))
				tmpDate = tmpDate.substring(0, tmpDate.length() - 1);

			dto.setTitle(document.title());
			dto.setDate(tmpDate.substring(tmpDate.length() - 19));
			article = parts[parts.length - 1];
			author = article.split(" 0 ")[0].split("@")[0].substring(1);
			String authorUrl = "http://www.destructoid.com"
					+ document.select("img[alt=" + author + "]").first().parent().attr("href");

			article = article.replaceAll(author + " 0 ", "");
			dto.setContent(article);
			dto.setData(document);

			if (authors.containsKey(author))
				dto.setAuthor(authors.get(author));
			else {
				AuthorDTO authorDTO = new AuthorDTO();
				authorDTO.setName(author);
				authorDTO.setUrl(authorUrl);
				dto.setAuthor(authorDTO);
				authors.put(author, authorDTO);
			}
			articles.put(document.title(), dto);
		}
		// }

		// System.out.println("...");
		titles.add(document.title());
	}

	public static void main(String[] args) throws IOException {
		Connection connect = Jsoup.connect("http://www.destructoid.com/");
		// Connection connect2 = Jsoup.connect("http://kotaku.com/");
		Document document = connect.timeout(10000).get();
		Elements links = document.select("a");
		Set<String> hrefs = new HashSet<String>();

		for (Element elem : links) {
			String href = elem.attr("href");
			if (!href.contains("destructoid") && !href.contains("/"))
				// System.out.println(href);
				if (href.endsWith(".phtml") && !href.contains("destructoid") && !href.contains("/"))
				hrefs.add(href);
		}

		for (String string : hrefs) {
			exploreSite(string);
		}
		Collections.sort(titles);

		for (String name : authors.keySet()) {
			exploreAuthor(name);
		}

		for (String string : titles) {
			ArticleDTO d = articles.get(string);
			if (d != null) {
				// System.out.println(d.getAuthor().getName() + " - " +
				// d.getTitle());
				// System.out.println(d.getDate());
				// String cont =
				// d.getContent().replace(d.getContent().split("0")[0] + "0 ",
				// "");
				// System.out.println(cont);
				// System.out.println(d.getData().size() + " - " + d.getData());
				// System.out.println("author photos: " +
				// d.getAuthor().getPhoto() + ", " +
				// d.getAuthor().getBackground());
				// System.out.println("About author: " +
				// d.getAuthor().getInfo());
				// System.out.println();
			}
		}

		HttpClient httpClient = HttpClientBuilder.create().build();

		for (String string : authors.keySet()) {
			exploreAuthor(string);
			JSONObject author = new JSONObject(authors.get(string));

			saveAuthor(httpClient, author.toString());

			// System.out.println(author.toString());
		}
		for (String string : articles.keySet()) {
			JSONObject artic = new JSONObject(articles.get(string));

			saveArticle(httpClient, artic.toString());

			// System.out.println(artic);
		}

	}

	// System.out.println(document);

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
