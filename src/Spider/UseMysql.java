package Spider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UseMysql {
	public Connection connectMysql(){
		Connection conn=null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/search?useUnicode=true&characterEncoding=utf-8","root","123456");			
		} catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		return conn;
	}
	

}
