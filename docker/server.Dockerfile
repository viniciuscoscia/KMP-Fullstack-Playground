FROM gradle:9.2.1-jdk21-corretto AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew :server:bootJar --no-daemon --console=plain

FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/substance-atlas
RUN addgroup -S atlas && adduser -S atlas -G atlas
COPY --from=builder /workspace/server/build/libs/server-*.jar /opt/substance-atlas/server.jar
RUN mkdir -p /opt/substance-atlas/templates /opt/substance-atlas/import && chown -R atlas:atlas /opt/substance-atlas
USER atlas
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/substance-atlas/server.jar"]
