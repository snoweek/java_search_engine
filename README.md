
# search_engine

search_engine,是一个基于java和mysql的网易新闻搜索工具，爬虫模块使用了jsoup库，搜索模块使用了Lucene。
## 数据库search表信息
如果数据连接信息，例如数据库名或密码发生变化，可以到src/Spider/UseMysql.java中修改。
* geturl
```
CREATE TABLE `geturl` (
  `url_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `url` text,
  PRIMARY KEY (`url_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8; 
```
表geturl包含两列，url_id用于记录链接爬取的顺序，同时用作主键;url用于记录链接的具体内容。
* artical
```
CREATE TABLE `artical` (
  `artical_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `title` text,
  `content` text,
  `url_id` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`artical_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8; 
```
表artical包含四列，artical_id用于记录文章获取的顺序，同时用作主键;title用于记录文章标题;content用于记录文章的具体内容;url_id用于指示文章所属的链接。

## 网络爬虫
网络爬虫，是一种按照一定的规则，自动地抓取万维网信息的程序或者脚本.在此使用了jsoup库。jsoup 是一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。它提供了一套非常省力的API，可通过DOM，CSS以及类似于jQuery的操作方法来取出和操作数据。
##搜索模块
搜索模块使用了Lucene。Lucene是一个全文检索的框架，其全文检索的功能可以非常方便的实现根据关键字来搜索整个应用系统的内容。
Lucene的操作方式和操作数据库有点相似，所以如果要使用Lucene，就要先创建“数据库”，然后往这个“数据表”中一行一行的插入数据，数据插入成功之后，就可以操作这张“数据表”，实现增删改查操作了。
### 索引过程中用到的类
###### 1.FSDirectory:创建了磁盘目录对象fsdDirectory,该方式创建的索引数据保存在磁盘上，不会因为程序的退出而消失。
```
File indexDir = new File("/home/sunyan/code/eclipse/lucene_result");
Directory fsDirectory = FSDirectory.open(indexDir);
```
Lucene提供了两种索引库的创建方式,FSDirectory和RAMDirectory两个类。RAMDirectory方式创建的索引数据保存在内存中，会因为程序的退出而消失。
```
Directory ramDirectory=New RAMDirectory()
```
###### 2.TextField:文档对象的字段
```
TextField(String name, String value, Store store)  
```
name  : 字段名称  
value : 字段的值    
store : 

        1.Field.Store.YES:存储字段值（未分词前的字段值） 
          
        2.Field.Store.NO:不存储,存储与索引没有关系

        3.Field.Store.COMPRESS:压缩存储,用于长文本或二进制，但性能受损 

```
TextField title= new TextField("title", rs.getString("title"), Store.YES);
TextField content= new TextField("content", rs.getString("content"), Store.YES);
TextField url= new TextField("url_id", rs.getString("url_id"), Store.YES);
```
###### 3.Document:文档对象，对象中可以有字段,往里面添加内容之后可以根据字段去匹配查询。 
```
Document doc = new Document();
doc.add(title);
doc.add(content);
doc.add(url);
```

###### 4.Analyzer:文本文件在被索引之前，需要经过Analyzer处理。常用的中文分词器有庖丁、IKAnalyzer。
```
Analyzer analyzer = new IKAnalyzer();
```
实例化一个分词器对象，在创建索引时会用到分词器，在使用字符串搜索时也会用到分词器，这两个地方要使用同一个分词器，否则可能会搜索不出结果。
分词器的一般工作流程：切分关键词、去除停用词、对于英文单词，把所有字母转为小写（搜索时不区分大小写）。
停用词有些词在文本中出现的频率非常高，但是对文本所携带的信息基本不产生影响，例如英文的“a、an、the、of”，或中文的“的、了、着”，以及各 种标点符号等，这样的词称为停用词（stop word）。文本经过分词之后，停用词通常被过滤掉，不会被进行索引。在检索的时候，用户的查询中如果含有停用词，检索系统也会将其过滤掉（因为用户输入 的查询字符串也要进行分词处理）。排除停用词可以加快建立索引的速度，减小索引库文件的大小。

###### 5.IndexWriter:索引写入器。

用于创建一个新的索引并把文档加到已有的索引中去，也可以向索引中添加、删除和更新被索引文档的信息。
```
IndexWriter indexWriter = new IndexWriter(fsDirectory, iwConfig);//创建索引写入器indexWriter，
```
在索引写入器创建之前，还需要以下初始配置，如版本，分词器，索引维护的方式
```
IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);//设置索引维护的方式
```
一旦IndexWriter对象创建完成，改变IndexWriterConfig的配置对已创建的IndexWriter对象不会产生影响。

```
indexWriter.addDocument(doc);//利用索引写入器将指定的数据存入内存目录对象中
indexWriter.close();//关闭IndexWriter 写入器   
```
indexWriter调用函数addDocument将索引写到索引文件夹（索引库）中,并自动指定一个内部编号，用来唯一标识这条数据。

### 搜索过程中用到的类
###### 1.IndexSearcher：IndexWriter创建的索引进行搜索。
```
Directory fsDirectory = FSDirectory.open(indexDir);
DirectoryReader ireader = DirectoryReader.open(fsDirectory);
IndexSearcher isearcher = new IndexSearcher(ireader);
```
创建IndexSearcher 检索索引的对象，里面要传递上面写入的内存目录对象directory。

###### 2.QueryParser：查询分析器 
```
QueryParser qp = new QueryParser(Field字段，分词器) 
Query query  =  qb.parser(“要查询的字串”)
```
```
QueryParser qp = new QueryParser("title", analyzer);     
qp.setDefaultOperator(QueryParser.AND_OPERATOR);
Query query = qp.parse("习近平");  
```
查询分析器，处理用户输入的查询字符串，把用户输入的非格式化检索词转化成后台检索可以理解的Query对象调用parser进行语法分析，形成查询语法树。查询字符串也要先经过Analyzer（分词器）。要求搜索时使用的Analyzer要与建立索引时使用的 Analzyer要一致，否则可能搜不出正确的结果

###### 3.TopDocs:是一个简单的指针容器，指针一般指向前N个排名的搜索结果，搜索结果即匹配查询条件的文档。
```
TopDocs topDocs = isearcher.search(query , 5);
System.out.println("记录条数:" + topDocs.totalHits);
ScoreDoc[] scoreDocs = topDocs.scoreDocs;
for (int i = 0; i < topDocs.totalHits; i++){
  Document targetDoc = isearcher.doc(scoreDocs[i].doc);
    System.out.println("内容:" + targetDoc.toString());
    System.out.println("内容:" + targetDoc.get("title"));
    System.out.println("内容:" + targetDoc.get("content"));
    System.out.println("url_id:" + targetDoc.get("url_id"));                
}
```
IndexSearcher调用search对查询语法树Query进行搜索，得到结果。此方法返回值为TopDocs，是包含结果的多个信息的一个对象。其中有 totalHits 代表决记录数，ScoreDoc的数组。ScoreDoc是代表一个结果的相关度得分与文档编号等信息的对象。取出要用到的数据列表。调用IndexSearcher.doc(scoreDoc.doc)以取出指定编号对应的Document数据。在分页时要用到：一次只取一页的数据。
应的Document数据。在分页时要用到：一次只取一页的数据。

```
命中:1
内容:Document<stored,indexed,tokenized<id:182> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html#f=www>>
```

### 问题与解决办法
#####1. Access Denied
由于大量且频繁的爬取数据，爬虫被拒绝了，无法再爬取数据。我的解决办法是伪装user agent。
User agent是HTTP协议的中的一个字段， 其作用是描述发出HTTP请求的客户端的一些信息。服务器通过这个字段就可以知道要访问网站的是什么人了。每个浏览器，每个正规的爬虫都有其固定的user agent，因此只要将这个字段改为这些知名的user agent，就可以成功伪装了。不过，不推荐伪装知名爬虫，因为这些爬虫很可能有固定的IP，如百度爬虫。与此相对的，伪装浏览器的user agent是一个不错的主意，因为浏览器是任何人都可以用的，换名话说，就是没有固定IP。
```
Connection conWeb = Jsoup.connect(Config.URL_PATH);
conWeb.header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; MALC)");
```
#####2. Lucene特殊字符处理
若遇到类似错误Cannot parse '{{{': Encountered "<EOF>"，说明
查询语句中含有Lucene保留的关键字或者语料库中当前被分析的文本是空文本。
需要用QueryParser的静态方法escape(string s)进行处理。

## License
Apache 






