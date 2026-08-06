FROM gradle:9.2.1-jdk21-corretto AS builder
WORKDIR /workspace
RUN dnf install -y libatomic && dnf clean all
COPY . .
RUN ./gradlew :app:webApp:wasmJsBrowserDistribution --no-daemon --console=plain

FROM nginx:1.27-alpine
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /workspace/app/webApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/
EXPOSE 8081
