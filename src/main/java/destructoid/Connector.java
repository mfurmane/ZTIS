package destructoid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Connector {
	static int i = 0;
	static List<String> titles = new Vector<String>();
	static Map<String, ArticleDTO> articles = new HashMap<String, ArticleDTO>();
	static Map<String, AuthorDTO> authors = new HashMap<String, AuthorDTO>();
	
	public static void exploreAuthor(String name) throws IOException {
		AuthorDTO dto = authors.get(name);
		Connection connect = Jsoup.connect(dto.getUrl());
		Document document = connect.get();

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
		Document document = connect.get();

		Element commentDiv = document.select("div#disqus_thread").first();
//		System.out.println(commentDiv);
//		for (Element elem : commentDiv.children()) {
//			System.out.println(elem);
//		}
		
//		Element commentDiv = document.select("div#disqus_thread").first();
		System.out.println("###");
		for (Element elem : document.select("body")) {
			System.out.println("########");
		}
		
		

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
		Document document = connect.get();
		Elements links = document.select("a");
		Set<String> hrefs = new HashSet<String>();

		for (Element elem : links) {
			String href = elem.attr("href");
			if (!href.contains("destructoid") && !href.contains("/"))
			System.out.println(href);
			if (href.endsWith(".phtml") && !href.contains("destructoid") && !href.contains("/"))
				hrefs.add(href);
		}

		for (String string : hrefs) {
			exploreSite(string);
		}
		Collections.sort(titles);

		// for (String string : titles) {
		// DestructoidDTO d = articles.get(string);
		// if (d != null) {
		// System.out.println(d.getAuthor().getName() + " - " + d.getTitle());
		// System.out.println(d.getDate());
		// System.out.println(d.getContent());
		// System.out.println(d.getData().size() + " - " + d.getData());
		// System.out.println();
		// }
		// }

		for (String name : authors.keySet()) {
			exploreAuthor(name);
		}

		// System.out.println(document);
		
	}

}
