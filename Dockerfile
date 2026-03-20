FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder

WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn ./.mvn

COPY src ./src
RUN sh ./mvnw -B -DskipTests package

FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21

WORKDIR /work/
COPY --from=builder /build/target/quarkus-app/lib/ /work/lib/
COPY --from=builder /build/target/quarkus-app/*.jar /work/
COPY --from=builder /build/target/quarkus-app/app/ /work/app/
COPY --from=builder /build/target/quarkus-app/quarkus/ /work/quarkus/

ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV QUARKUS_HTTP_PORT=80

EXPOSE 80

USER 0
ENTRYPOINT ["java", "-jar", "/work/quarkus-run.jar"]
