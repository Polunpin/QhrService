## 目录结构说明
~~~
.
├── Dockerfile                      Dockerfile 文件
├── README.md                       README 文件
├── mvnw                            mvnw 文件，处理mevan版本兼容问题
├── mvnw.cmd                        mvnw.cmd 文件，处理mevan版本兼容问题
├── pom.xml                         pom.xml文件
└── src                             源码目录
    └── main                        源码主目录
        ├── java                    业务逻辑目录
        └── resources               资源文件目录
~~~

[config｜配置层](src/main/java/com/qhr/config)

[controller｜控制层](src/main/java/com/qhr/controller) 

[dao｜mapper层](src/main/java/com/qhr/dao) 

[dto｜请求Bean层](src/main/java/com/qhr/dto) 

[model｜TableBean层](src/main/java/com/qhr/model)

[service｜服务层](src/main/java/com/qhr/service)

[vo｜业务Bean层](src/main/java/com/qhr/vo)
