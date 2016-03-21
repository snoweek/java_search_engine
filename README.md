# search_engine
search_engine,是一个基于java和mysql的新闻搜索工具，爬虫模块使用了jsoup库，搜索模块使用了Lucene。
##数据库search表信息
如果数据连接信息，例如数据库名或密码发生变化，可以到src/Spider/UseMysql.java中修改。
* geturl
```
CREATE TABLE `geturl` (
  `url_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `url` text,
  PRIMARY KEY (`url_id`)
) 
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
)
```
表artical包含四列，artical_id用于记录文章获取的顺序，同时用作主键;title用于记录文章标题;content用于记录文章的具体内容;url_id用于指示文章所属的链接。

##网络爬虫
网络爬虫，是一种按照一定的规则，自动地抓取万维网信息的程序或者脚本.在此使用了jsoup库。jsoup 是一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。它提供了一套非常省力的API，可通过DOM，CSS以及类似于jQuery的操作方法来取出和操作数据。
##搜索模块
搜索模块使用了Lucene。我把lucene理解成一个操作索引库的工具。索引库是一个目录，用于存放二进制文件。Lucene的数据结构为Document与Field。Document代表一条数据，Field代表数据中的一个属性。一个Document中有多个Field。我们只需程序中的对象转成Document，就可以交给Lucene管理了，搜索的结果中的数据列表也是Document的集合。对索引库的操作可以分为两种：建立索引与搜索索引。从索引库中搜索索引查询使用IndexSearcher。
* 建立索引
建立索引库使用IndexWriter。

1. 创建索引库
创建一个文件夹，用于存放索引文件，这就是一个索引库。Lucene提供了两种索引库的创建方式,FSDirectory和RAMDirectory两个类。
* FSDirectory
```
File indexDir = new File("/home/sunyan/code/eclipse/lucene_result");
Directory fsDirectory = FSDirectory.open(indexDir);
```
该方式创建的索引数据保存在磁盘上，不会因为程序的退出而消失。因为我希望建立的索引存在硬盘中，所以选择了这种方式。
* RAMDirectory
```
Directory ramDirectory=New RAMDirectory()
IndexWriter indexWriter = new IndexWriter(ramDirectory, iwConfig);
```
或者
```
indexWrtier indexWriter  =   new  IndexWriter( new  RAMDirectory(), iwConfig);
```
该方式创建的索引数据保存在内存中，会因为程序的退出而消失。
2. 添加数据
```
Document doc = new Document();
TextField id = new TextField("id",String.valueOf(rs.getInt("artical_id")) , Store.YES);  
TextField title= new TextField("title", rs.getString("title"), Store.YES);	    
doc.add(id);
doc.add(title);
```
创建索引库中的一条数据doc，并往数据中中添加属性id和title
3. 实例化分词器对象
```
Analyzer analyzer = new IKAnalyzer()
```
在创建索引时会用到分词器，在使用字符串搜索时也会用到分词器。这两个地方要使用同一个分词器，否则可能会搜索不出结果。
分词器的一般工作流程：切分关键词、去除停用词、对于英文单词，把所有字母转为小写（搜索时不区分大小写）。
停用词有些词在文本中出现的频率非常高，但是对文本所携带的信息基本不产生影响，例如英文的“a、an、the、of”，或中文的“的、了、着”，以及各 种标点符号等，这样的词称为停用词（stop word）。文本经过分词之后，停用词通常被过滤掉，不会被进行索引。在检索的时候，用户的查询中如果含有停用词，检索系统也会将其过滤掉（因为用户输入的查询字符串也要进行分词处理）。排除停用词可以加快建立索引的速度，减小索引库文件的大小。
4. 初始配置
```
IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
```
IndexWriterConfig对象用来设置IndexWriter一些初始配置，一旦IndexWriter对象创建完成，改变IndexWriterConfig的配置对已创建的IndexWriter对象不会产生影响。
5. 设置索引维护的方式
```
iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
```
6.创建索引
```
IndexWriter indexWriter = new IndexWriter(fsDirectory, iwConfig);
indexWriter.addDocument(doc);
indexWriter.close();
```
Indexwriter的构造方法：IndexWriter(Directory d, IndexWriterConfig iwc)
ndexWriter调用函数addDocument将索引写到索引文件夹（索引库）中,并自动指定一个内部编号，用来唯一标识这条数据。


* 搜索
从索引库中搜索的执行过程中，先在词汇表中查找，得到符合条件的文档编号列表，再根据文档编号真正的去取出数据（Document）。
1. 获取需要搜索的关键字
```
QueryParser qp = new QueryParser("title", analyzer); 
qp.setDefaultOperator(QueryParser.AND_OPERATOR);
Query query = qp.parse("航空");
```
以上步骤可抽象为 
```
QueryParser(Field字段， new  分析器) 
Query query  =  QueryParser.parser(“要查询的字串”)
```
先 使用QueryParser查询分析器构造Query对象
QueryParser查询分析器，处理用户输入的查询字符串，把用户输入的非格式化检索词转化成后台检索可以理解的Query对象调用parser进行语法分析，形成查询语法树。查询字符串也要先经过Analyzer（分词器）。要求搜索时使用的Analyzer要与建立索引时使用的 Analzyer要一致，否则可能搜不出正确的结果。
2. 获取搜索结果
```
TopDocs topDocs = isearcher.search(query , 5); 
System.out.println("命中:" + topDocs.totalHits);
ScoreDoc[] scoreDocs = topDocs.scoreDocs;
for (int i = 0; i < topDocs.totalHits; i++){
    Document targetDoc = isearcher.doc(scoreDocs[i].doc);
    System.out.println("内容:" + targetDoc.toString());
 }
```
IndexSearcher调用search对查询语法树Query进行搜索，得到结果。此方法返回值为TopDocs，是包含结果的多个信息的一个对象。其中有 totalHits 代表决记录数，ScoreDoc的数组。ScoreDoc是代表一个结果的相关度得分与文档编号等信息的对象。取出要用到的数据列表。调用IndexSearcher.doc(scoreDoc.doc)以取出指定编号对应的Document数据。在分页时要用到：一次只取一页的数据。

搜索索结果如下
```
命中:1
内容:Document<stored,indexed,tokenized<id:182> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html#f=www>>
```
##License
Apache 






