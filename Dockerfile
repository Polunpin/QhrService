FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder

WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

COPY src ./src
RUN sh ./mvnw -B -Dnative -DskipTests package

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

WORKDIR /work/
COPY --from=builder --chmod=0755 /build/target/*-runner /work/application

ENV QUARKUS_HTTP_HOST=0.0.0.0

EXPOSE 80

USER 0
ENTRYPOINT ["./application"]
