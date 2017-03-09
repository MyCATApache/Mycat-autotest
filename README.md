# Mycat-autotest


## 配置信息

### 环境
JDK 1.8

### release 包
autotest [-p/--path][-i/--ids][-o/--outpath][-s/--server] <br/>
-p/--path 设置config.xml 的位置 <br/>
-i/--ids 执行用例，多个以,分割, 当本参数为空时，执行所有用例,可填写组id和用例id，组id比用例id级别高 <br/>
-o/--outpath 用例输出目录(会覆盖config.xml的outPath属性) <br/>
-s/--server 是否已服务模式启动(传入true，使用config.xml 中quartz的定时器配置，也可以传入定时器配置信息) <br/>

## 用例编写
*** 现在用例标签大小写必须完全匹配
###主配置文件config.xml编写
1. connection 为数据库链接池配置信息，config.xml中表示全局使用的配置文件
2. path 为测试用例目录
3. checkConcurrency 为可以并发测试的用例的同时执行数量
4. outPath 为用例结果输出目录，可被命令行参数 -o/--outpath 覆盖掉 
```html
<projectConfig>
	<connections> 
		<connection id="test" host="192.168.1.5" database="test" post="3306" username="test" password="test" />
	</connections>
	<path>./</path><!-- 测试用例目录 -->
	<checkConcurrency>10</checkConcurrency><!-- 检查并发数目，只有可以异步的用例才可以，并发执行， 系统的执行过程 先执行所有同步的 在执行异步的用例 -->
	<outPath>./outpath</outPath>
	<quartz>0 0 1 * * ?</quartz><!-- 测试用例目录 -->
</projectConfig>
```

###用例组配置文件initTestGroup.xml编写
1. beforeTestGroup 本组用例启动前 可以配置多个transaction
2. afterTestGroup 本组用例全部结束后 可以配置多个transaction
3. beforeTest 每个用例执行前调用，可以配置多个 transaction
4. afterTest 每个用例执行后调用，可以配置多个 transaction
```html
<testGroup>
	<beforeTestGroup>
		<transaction connection="test" url="./init.sql"><!-- 本组用例启动前 可以配置多个transaction -->
		</transaction>
	</beforeTestGroup>
	<afterTestGroup>
		<transaction connection="test" url="./init_clean.sql"><!-- 本组用例全部结束后 可以配置多个transaction -->
		</transaction>
	</afterTestGroup>
	<!--<beforeTest> 每个用例执行前调用，可以配置多个 transaction
	</beforeTest>
	<afterTest > 每个用例执行后调用，可以配置多个 transaction
		<transaction connection="test" url="./cleanData.sql">
		</transaction>
	</afterTest>-->
</testGroup>
```
5. initTestGroup.xml 中也可以加入connections 标签，共本用例组，用例使用，如下
```html
    <connections> 
		<connection id="test" host="192.168.1.5" database="test" post="3306" username="test" password="test" />
	</connections>
```

### 用例配置文件useCase.xml编写
1. id 为用例主键，可用在用例引用、命令行执行 -i/--ids 中，不能重复
2. config 下的 defaultDataSource当前所有需要数据库链接默认数据源 如代替不同标签中的 verifyConnection 和 connection 属性的
3. init 为用例执行前的行为，可以配置多个 sql 和 transaction 如
```html
    <init>
        <sql url="./init.sql" connection="test" />
        <sql url="./init2.sql" connection="test2" />
    </init>
```
4. check 可以配置 transaction、select、verify等标签
5. select  可以配置 自身属性 url 数据库sql位置 connection 数据源 count执行次数只有性能测试中生效 和verify中的属性 verifySqlSrc 数据库sql位置 verifyOrder 是否需要验证顺序 verifyCheckfile 验证文件位置 verifyDescription错误信息
6. check 可以配置 transaction、select等标签
7. clean 为用例执行后的行为，可以配置多个 sql 和 transaction 和 init 标签类似
#### 例子
```html
<useCase>
	<id>queryExample</id> 
	<config defaultDataSource="test" name="queryExample" /><!-- defaultDataSource当前所有需要数据库链接默认数据源 如 verifyConnection 和 connection 属性的  -->
	<init url="./init.sql" connection="test" /><!-- url sql路径 -->
	<check><!-- verifySqlSrc 数据库sql位置 verifyOrder 是否需要验证顺序 verifyCheckfile 验证文件位置 verifyDescription错误信息 -->
		<select verifySqlSrc="./select.sql" verifyConnection="test" verifyOrder="true" verifyCheckfile="./check.xml" verifyDescription="查询错误" />
	</check>
	<performance><!-- 性能为独立检查服务，执行多次，显示最高、最低、平均 -->
		<select url="./select.sql" connection="test" count="10" /><!-- url sql路径 count 测试次数 -->
	</performance>
	<clean url="./clean.sql" connection="test"/>
</useCase>
```
