首先讲解网络爬虫部分。
网络爬虫（又被称为网页蜘蛛，网络机器人，在FOAF社区中间，更经常的称为网页追逐者），是一种按照一定的规则，自动地抓取万维网信息的程序或者脚本。
在这里，用到了jsoup 。jsoup 是一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。它提供了一套非常省力的API，可通过DOM，CSS以及类似于jQuery的操作方法来取出和操作数据。
[jsoup开发手册 http://www.open-open.com/jsoup/](http://www.open-open.com/jsoup/)
[jsoup下载地址 http://jsoup.org/download](http://jsoup.org/download)
jsoup的jar包下载之后，导入我们的工程里就行了。


那么，现在网页内容已经扒取完毕，接下来就是搜索部分了，我用到了Lucene
总的来说，我把lucene理解成一个操作索引库的工具，就像mysql，至于倒排索引、分词等功能具体是怎样实现的，我先不管，只要知道有这个功能，而我又可以通过Lucene的API（应用程序接口）使用就好。
索引库是一个目录（即一个文件夹），里面是一些二进制文件，就如同数据库，所有的数据也是以文件的形式存在文件系统中的。我们不能直接操作这些二进制文件，而是使用Lucene提供的API完成相应的操作，就像操作数据库应使用SQL语句一样。Lucene的 数据结构为Document与Field。Document代表一条数据，Field代表数据中的一个属性。一个Document中有多个 Field。我们只需要把在我们的程序中的对象转成Document，就可以交给Lucene管理了，搜索的结果中的数据列表也是Document的集合。对索引库的操作可以分为两种：建立索引与搜索索引。建立索引库使用IndexWriter，从索引库中搜索索引查询使用IndexSearcher。
一．建立索引

1.
```
File indexDir = new File("D://java/Test");
 Directory fsDirectory = FSDirectory.open(indexDir);
```
创建一个文件夹，用于存放索引文件，这就是一个索引库。
因为我希望建立的索引存在硬盘中，通过FSDirectory类加载indexDir文件夹，该方式创建的索引数据保存在磁盘上，不会因为程序的退出而消失。
Directory指明索引存放的位置;lucene提供了两种索引存放的位置相应地lucene提供了FSDirectory和RAMDirectory两个类。
一种是磁盘，这种方法首先需建立一个文件，然后通过open方法加载。
一种是内存，
```
Directory ramDirectory=New RAMDirectory()
IndexWriter indexWriter = new IndexWriter(ramDirectory, iwConfig);
```
或者
```
ndexWrtier indexWriter  =   new  IndexWriter( new  RAMDirectory(), iwConfig);
```
2.
```
Document doc = new Document();
TextField id = new TextField("id",String.valueOf(rs.getInt("artical_id")) , Store.YES);  
TextField title= new TextField("title", rs.getString("title"), Store.YES);	    
doc.add(id);
doc.add(title);
```
创建索引库中的一条数据doc，并往数据中中添加属性id和title
3.
```
Analyzer analyzer = new IKAnalyzer()
```
实例化一个分词器对象，在创建索引时会用到分词器，在使用字符串搜索时也会用到分词器，这两个地方要使用同一个分词器，否则可能会搜索不出结果。
分词器的一般工作流程：切分关键词、去除停用词、对于英文单词，把所有字母转为小写（搜索时不区分大小写）。
停用词有些词在文本中出现的频率非常高，但是对文本所携带的信息基本不产生影响，例如英文的“a、an、the、of”，或中文的“的、了、着”，以及各 种标点符号等，这样的词称为停用词（stop word）。文本经过分词之后，停用词通常被过滤掉，不会被进行索引。在检索的时候，用户的查询中如果含有停用词，检索系统也会将其过滤掉（因为用户输入 的查询字符串也要进行分词处理）。排除停用词可以加快建立索引的速度，减小索引库文件的大小。
4.
```
IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
```
IndexWriterConfig对象用来设置IndexWriter一些初始配置，一旦IndexWriter对象创建完成，改变IndexWriterConfig的配置对已创建的IndexWriter对象不会产生影响。
iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
设置索引维护的方式
6.
```
IndexWriter indexWriter = new IndexWriter(fsDirectory, iwConfig);
indexWriter.addDocument(doc);
indexWriter.close();
```
Indexwriter的构造方法：IndexWriter(Directory d, IndexWriterConfig iwc)
ndexWriter调用函数addDocument将索引写到索引文件夹（索引库）中,并自动指定一个内部编号，用来唯一标识这条数据。


二. 搜索索引
从索引库中搜索的执行过程中，先在词汇表中查找，得到符合条件的文档编号列表，再根据文档编号真正的去取出数据（Document）。
1，
```
QueryParser qp = new QueryParser("title", analyzer);               qp.setDefaultOperator(QueryParser.AND_OPERATOR);
 Query query = qp.parse("航空");
```
以上步骤可抽象为 
```
QueryParser(Field字段， new  分析器) 
Query query  =  QueryParser.parser(“要查询的字串”)
```
先 使用QueryParser查询分析器构造Query对象
QueryParser查询分析器，处理用户输入的查询字符串，把用户输入的非格式化检索词转化成后台检索可以理解的Query对象调用parser进行语法分析，形成查询语法树。查询字符串也要先经过Analyzer（分词器）。要求搜索时使用的Analyzer要与建立索引时使用的 Analzyer要一致，否则可能搜不出正确的结果。
2，
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
搜索索引代码如下
```
命中:4
内容:Document<stored,indexed,tokenized<id:182> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html#f=www>>
内容:Document<stored,indexed,tokenized<id:198> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html>>
内容:Document<stored,indexed,tokenized<id:216> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html>>
内容:Document<stored,indexed,tokenized<id:251> stored,indexed,tokenized<title:昆明航空多名空姐被恶搞塞进行李架(图)> stored,indexed,tokenized<url:http://news.163.com/15/1011/22/B5M981CT00011229.html#f=www




or create a new repository on the command line

echo "# java_searchenginee" >> README.md
git init
git add README.md
git commit -m "first commit"
git remote add origin https://github.com/snoweek/java_searchenginee.git
git push -u origin master

…or push an existing repository from the command line

git remote add origin https://github.com/snoweek/java_searchenginee.git
git push -u origin master
