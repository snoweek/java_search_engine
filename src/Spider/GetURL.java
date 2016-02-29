package Spider;

import java.sql.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class GetURL{
	public static void main(String[] args) {
		UseMysql useMysql=new UseMysql();
		try{
			Document doc = Jsoup.connect("http://www.163.com").get();
			Elements link=doc.select("a[href^=http://news.163.com/16]");
			System.out.println("Links: "+link.size());
			Connection conn=useMysql.connectMysql();
	        for (Element links : link) {        	
	           String url=links.attr("href");
	           System.out.println( url);					 
	           PreparedStatement pstmt=conn.prepareStatement("insert into geturl(url)values(?)");
			   pstmt.setString(1,url);
			   pstmt.executeUpdate();    
	        }
		}catch(Exception e){
			System.out.print("错误"+e.getMessage());
		}        
	}
}
