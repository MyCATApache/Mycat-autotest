# Mycat-autotest


## 配置信息

### 环境
JDK 1.8

### release 包
autotest [-p/--path][-i/--ids][-o/--outpath][-s/--server] <br/>
-p/--path 设置config.xml 的位置 <br/>
-i/--ids 执行用例，多个以,分割, 当本参数为空时，执行所有用例,可填写组id和用例id，组id比用例id级别高 <br/>
-o/--outpath 用例输出目录 <br/>
-s/--server 是否已服务模式启动（暂未实现） <br/>