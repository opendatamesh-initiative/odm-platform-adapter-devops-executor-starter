FROM amazoncorretto:21-alpine-jdk
VOLUME /tmp
ADD ./ /code
RUN cd /code && ./mvnw --no-transfer-progress package -DskipTests \
    && mkdir -p /app \
    && cp -r /code/target/quarkus-app/. /app/ \
    && cd / && rm -rf /code && rm -rf /root/.m2
ENV JAVA_OPTS=""
EXPOSE 9080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar" ]
