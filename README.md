# mypinpoint-plugin
APM-pinpoint第三方插件

BRPC，XXL-JOB（执行器），TARS插件

1,下载pinpoint源码https://github.com/naver/pinpoint.git
2,本地安装好java6,java7,java8,java9(9可以使用高版本替代)
3,将插件项目作为module导入到pinpoint的plugins目录下
4,在plugins/assembly/pom.xml文件中添加插件依赖 
	<dependency>
        <groupId>com.navercorp.pinpoint.plugin.xxx</groupId>
        <artifactId>pinpoint-xxx-plugin</artifactId>
        <version>${project.version}</version>
    </dependency>
5,编译打包 mvn clean install  -DskipTests=true
6,打包好了后，将pinpoint-XXX-plugin-XXX.jar插件jar文件放到pinpoint-agent/plugin/目录下，
插件依赖的包放到pinpoint-agent/lib/目录下，还需要把插件jar文件和其依赖的文件一同放到web和collector的/WEB-INF/lib目录下
7,依次重启web,collector以及业务服务
