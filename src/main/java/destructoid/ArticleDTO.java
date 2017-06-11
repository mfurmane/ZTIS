package destructoid;
import java.util.List;
import java.util.Vector;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ArticleDTO {
	private String title;
	private String date;
	private String content;
	private AuthorDTO author;
	private List<String> data = new Vector<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public AuthorDTO getAuthor() {
		return author;
	}

	public void setAuthor(AuthorDTO author) {
		this.author = author;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(Document document) {
		for (Element elem : document.select("iframe")) {
			String link = elem.attr("src");
			this.data.add(link);
		}

		for (Element elem : document.select("img")) {
			String link = elem.attr("src");
			if (link.length() > 0 && link.startsWith("http") && link.charAt(link.length() - 4) == '.'
					&& !link.startsWith("https://pixel") && !link.startsWith("https://www.destructoid.com/ul/user"))
				this.data.add(link);
		}
	}
}
