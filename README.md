# 一款自动化检测git仓库 打war包 部署到tomcat的springboot项目

## 本项目基本原理

使用springboot自带定时任务 每分钟检查git仓库是否有更新  
如果有更新则拉取最新代码并执行maven构建  
将构建后的war包部署到tomcat webapps目录下

Java项目

1. 拉取git仓库 检查是否有更新
2. maven构建
3. 将war包部署到tomcat webapps目录下

html项目

1. 拉取git仓库 检查是否有更新
2. 将html文件复制到nginx配置的项目目录下

### 在使用此项目前需要现在服务器上安装maven tomcat

Ubuntu

````
sudo apt update
sudo apt install maven
````

验证

````
mvn -version
````

可以看到maven home

/usr/share/maven

配置conf/setting.xml

安装tomcat 并部署war包成功 (自行验证)

### 使用

在TaskRun.java中添加项目代码