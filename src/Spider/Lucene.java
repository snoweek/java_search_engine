package Spider;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Lucene {
	public static void main(String[] args) {
		UseMysql useMysql=new UseMysql();
		Connection conn=useMysql.connectMysql();
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try{				
			pstmt=conn.prepareStatement("select * from artical ");
			rs=pstmt.executeQuery();
			while(rs.next()){
				
				System.out.println(rs.getString("title"));
				File indexDir = new File("/home/sunyan/code/eclipse/lucene_result");
				Directory fsDirectory = FSDirectory.open(indexDir);
				Document doc = new Document();				   
				TextField title= new TextField("title", rs.getString("title"), Store.YES);
				TextField content= new TextField("content", rs.getString("content"), Store.YES);
				TextField url= new TextField("url_id", rs.getString("url_id"), Store.YES);
				doc.add(title);
				doc.add(content);
				doc.add(url);
				Analyzer analyzer = new IKAnalyzer();
				IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
				iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			    IndexWriter indexWriter = new IndexWriter(fsDirectory, iwConfig);
			    indexWriter.addDocument(doc);
			    indexWriter.close();			
			}
		}catch(Exception e){
				System.out.print("错误"+e.getMessage());
		}				
	}
}
