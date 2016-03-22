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
				File indexDir = new File("/home/sunyan/code/eclipse/lucene_result");//确定索引目录在磁盘中的位置
				Directory fsDirectory = FSDirectory.open(indexDir);//创建了磁盘目录对象fsdDirectory
				Analyzer analyzer = new IKAnalyzer();//实例化分词器对象，
				IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
				//IndexWriterConfig对象用来设置索引写入器IndexWriter一些初始配置，
				iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
				//设置索引维护的方式
				IndexWriter indexWriter = new IndexWriter(fsDirectory, iwConfig);//创建索引写入器indexWriter，
				Document doc = new Document();	
				 // 创建Document文档对象,对象中可以有字段,往里面添加内容之后可以根据字段去匹配查询   
				TextField title= new TextField("title", rs.getString("title"), Store.YES);
				TextField content= new TextField("content", rs.getString("content"), Store.YES);
				TextField url= new TextField("url_id", rs.getString("url_id"), Store.YES);
				doc.add(title);
				doc.add(content);
				doc.add(url);
			    indexWriter.addDocument(doc);//利用索引写入器将指定的数据存入内存目录对象中
			    indexWriter.close();//关闭IndexWriter 写入器		
			}
		}catch(Exception e){
				System.out.print("错误"+e.getMessage());
		}				
	}
}
