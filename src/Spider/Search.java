package Spider;
import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
public class Search {
	public static void main(String[] args) {
		Analyzer analyzer = new IKAnalyzer();
        File indexDir = new File("/home/sunyan/code/eclipse/index");
        try {
            Directory fsDirectory = FSDirectory.open(indexDir);
            DirectoryReader ireader = DirectoryReader.open(fsDirectory);
            IndexSearcher isearcher = new IndexSearcher(ireader);//创建IndexSearcher 检索索引的对象，里面要传递上面写入的内存目录对象directory ，
            QueryParser qp = new QueryParser("title", analyzer); // 然后根据关键字封装QueryParse查询对象；     
            qp.setDefaultOperator(QueryParser.AND_OPERATOR);
            Query query = qp.parse("台湾");   
            TopDocs topDocs = isearcher.search(query , 5);//去索引目录中查询，返回的是TopDocs对象，里面存放的就是上面放的document文档对象  
            System.out.println("记录条数:" + topDocs.totalHits);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (int i = 0; i < topDocs.totalHits; i++){
                Document targetDoc = isearcher.doc(scoreDocs[i].doc);
                System.out.println("内容:" + targetDoc.toString());
                System.out.println("内容:" + targetDoc.get("title"));
                System.out.println("内容:" + targetDoc.get("content"));
                System.out.println("url_id:" + targetDoc.get("url_id"));                
            }
        } catch (Exception e) {
        	System.out.print("错误"+e.getMessage());
        }
	}

}