FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -Dnative -DskipTests package

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

WORKDIR /work/
COPY --from=builder --chmod=0755 /build/target/*-runner /work/application

ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV QUARKUS_HTTP_PORT=80
EXPOSE 80

USER 1001
ENTRYPOINT ["./application"]
