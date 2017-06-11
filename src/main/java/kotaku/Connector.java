package kotaku;

import java.io.IOException;
import java.util.Date;
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
	static int day = 11;

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
			for (Element elem: document.select("div[style]")) {
				if (elem.attr("style").startsWith("background-image: url(")) dto.setPhoto(elem.attr("style").replace("background-image: url(", "").replace(")", ""));
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
		return input.replace("Yesterday", (date.getMonth() + 1) + "/" + (day - 1) + "/" + (date.getYear() - 100));

	}

	public static void exploreSite(String href) throws IOException {
		Connection connect = Jsoup.connect(href);
		Document document = connect.get();

		String toRemove = "kotaku Deadspin Fusion Gizmodo Jalopnik Jezebel Kotaku Lifehacker The Root Video The Bests Steamed Cosplay Compete Snacktaku Highlight Reel Podcast Review "
				+ document.title() + " ";
		String toRemove2 = "ShareTweet About Blog Need Help? Content Guide Kotaku Store Redirecting to the Kotaku store in Disclaimer: You are leaving a Gizmodo Media Group, LLC website and going to a third party site, which is subject to its own privacy policy and terms of use. Continue Permissions Privacy Terms of Use Advertising Jobs RSS ©2017 Gizmodo Media Group Kinja is in read-only mode. We are working to restore service.";
		String[] cleanedText = document.body().text().replace(toRemove, "").split("permalink ");

		String author = cleanedText[0].split("\\d")[0].replace("Yesterday", "").replace("Today", "").trim();
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
		
//		System.out.println(document.title());
		dto.setData(document);

		// System.out.println(author);
		// System.out.println("\n"+document.title());
		// System.out.println(date);
		// System.out.println(document.baseUri());
		// System.out.println(author+ " - " + cleanedText[0].replaceAll(author,
		// "") );
		// System.out.println(cleanedText[0].replace(" Edit Promote Share to
		// Kinja Toggle Conversation tools Go to", "").split("\\d")[0]);
		// System.out.println(cleanedText[1]);

		// String string = document.body().text().replace(toRemove,
		// "").replace((document.title() + " "), "")
		// .split("You are logged out.")[0];
		// if (string.contains("  ·  ")) {
		//// ArticleDTO dto = new ArticleDTO();
		// String[] parts = string.split("\\s*·\\s*");
		// String tmpDate = parts[0];
		//// String author;
		// String article;
		// if (tmpDate.endsWith(" "))
		// tmpDate = tmpDate.substring(0, tmpDate.length() - 1);
		//
		// dto.setTitle(document.title());
		// dto.setDate(tmpDate.substring(tmpDate.length() - 19));
		// article = parts[parts.length - 1];
		// author = article.split(" 0 ")[0].split("@")[0].substring(1);
		//
		//
		// article = article.replaceAll(author + " 0 ", "");
		// dto.setContent(article);
		// dto.setData(document);
		//
		//
		// articles.put(document.title(), dto);
		// }
		// // }
		//
		// // System.out.println("...");
		// titles.add(document.title());
	}

	public static void main(String[] args) throws IOException {
		Connection connect = Jsoup.connect("http://kotaku.com/");
		// Connection connect2 = Jsoup.connect("http://kotaku.com/");
		Document document = connect.get();
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
			System.out.println(string + " - " + authors.get(string).getUrl());
			exploreAuthor(string);
		}

		// Collections.sort(titles);
		//
		// // for (String string : titles) {
		// // DestructoidDTO d = articles.get(string);
		// // if (d != null) {
		// // System.out.println(d.getAuthor().getName() + " - " +
		// d.getTitle());
		// // System.out.println(d.getDate());
		// // System.out.println(d.getContent());
		// // System.out.println(d.getData().size() + " - " + d.getData());
		// // System.out.println();
		// // }
		// // }
		//
		// for (String name : authors.keySet()) {
		// exploreAuthor(name);
		// }
		//
		// // System.out.println(document);

	}

}
