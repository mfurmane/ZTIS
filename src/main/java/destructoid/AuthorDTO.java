package destructoid;
import java.util.List;
import java.util.Vector;

public class AuthorDTO {
	private String url;
	private String name;
	private String info;
	private String photo;
	private String background;
	private List<String> data = new Vector<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public List<String> getData() {
		return data;
	}
	public void setData(String photo) {
		this.data.add(photo);
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
}
