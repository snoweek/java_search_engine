package Spider;
import java.sql.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
public class GetArtical {
	public static void main(String[] args) {
		UseMysql useMysql=new UseMysql();
		Connection conn=useMysql.connectMysql();
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try{			
			pstmt=conn.prepareStatement("select * from geturl ");
			rs=pstmt.executeQuery();
			while(rs.next()){
				Document doc = Jsoup.connect(rs.getString("url")).get();
				Elements head=doc.select("h1");
				Elements pageContent=doc.select("p");
				String title=head.text();
				String content=pageContent.text();
				System.out.println(title);
				pstmt=conn.prepareStatement("insert into artical(title,content,url_id)values(?,?,?)");
				pstmt.setString(1,title);
				pstmt.setString(2,content);
				pstmt.setString(3,rs.getString("url_id"));
				pstmt.executeUpdate();
			}
		}catch(Exception e){
			System.out.print("错误"+e.getMessage());
		}
	}
}
