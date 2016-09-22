package Spider;

import java.sql.PreparedStatement;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetURL {
	public static void main(String[] args) {
		UseMysql useMysql = new UseMysql();
		try {
			org.jsoup.Connection conWeb = Jsoup.connect(Config.URL_PATH);
			conWeb.header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; MALC)");
			Document doc = conWeb.get();
			// Elements link=doc.select("a[href^=http://news.163.com/16]");
			Elements link = doc.select("a[href^=http://news.163.com]");
			System.out.println("Links: " + link.size());
			Connection conMysql = useMysql.connectMysql();
			for (Element links : link) {
				String url = links.attr("href");
				System.out.println(url);
				PreparedStatement pstmt = conMysql.prepareStatement("insert into geturl(url)values(?)");
				pstmt.setString(1, url);
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			System.out.print("错误" + e.getMessage());
		}
	}
}
