# 官方说仅支持JDK8，但是pom有很多漏洞，我决定坚持升级版本。通过百度AI的帮助竟然搞定了JDK21
# 在试错的过程中，在怀疑是不是云托管的docker有特殊设置？
# NO，bro。是你想复杂了。要更坚定点哈！
# ===============================
# 1. 构建阶段（Build Stage）
# ===============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# 工作目录
WORKDIR /build

# 先拷贝 pom.xml，利用依赖缓存
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 再拷贝源码
COPY src ./src

# 构建（跳过测试，生产可按需开启）
RUN mvn -B package -DskipTests


# ===============================
# 2. 运行阶段（Runtime Stage）
# ===============================
FROM eclipse-temurin:21-jre-jammy

# 基础环境变量
ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxRAMPercentage=75 \
               -XX:InitialRAMPercentage=50 \
               -XX:+ExitOnOutOfMemoryError"

# 工作目录
WORKDIR /app

# 拷贝 Quarkus fast-jar 产物
COPY --from=builder /build/target/quarkus-app/lib/ ./lib/
COPY --from=builder /build/target/quarkus-app/*.jar ./
COPY --from=builder /build/target/quarkus-app/app/ ./app/
COPY --from=builder /build/target/quarkus-app/quarkus/ ./quarkus/

# 暴露端口
# 此处端口必须与「服务设置」-「流水线」以及「手动上传代码包」部署时填写的端口一致，否则会部署失败。
EXPOSE 80

# 执行启动命令.
# 写多行独立的CMD命令是错误写法！只有最后一行CMD命令会被执行，之前的都会被忽略，导致业务报错。
# 请参考[Docker官方文档之CMD命令](https://docs.docker.com/engine/reference/builder/#cmd)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar quarkus-run.jar"]
