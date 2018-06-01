# 百科爬虫

## 1. 网页爬虫
### 1.1 任务
这个模块的输入是待抓取的任务文件，放到BAIKE_DIR/tasks下。BAIKE_DIR=lili@ai-hz1-spark2:/data/lili/deepnlu/data
任务文件的格式为：
```
url1
url2
```
### 1.2 从baikeurl导入任务
```
lili@ai-hz1-spark2:/data/lili/deepnlu/baikecrawler$ cat import-baikeurl.sh 
java  -Dlog4j.configuration=log4j-import.properties -cp ".:./baikecrawler-1.0-SNAPSHOT-jar-with-dependencies.jar" com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.ImportTasksFromBaikeWapUrl baikeurl tasks &

mv tasks ../data/url_tasks/waptask
```

### 1.3 创建html存放的子目录
```
lili@ai-hz1-spark2:/data/lili/deepnlu/baikecrawler$ cat mk-subdirs.sh 
java  -Dlog4j.configuration=log4j.properties -cp ".:./baikecrawler-1.0-SNAPSHOT-jar-with-dependencies.jar" com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CreateSubDataDirs /data/lili/deepnlu/data  1000
```





