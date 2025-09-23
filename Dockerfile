FROM amazoncorretto:11-alpine-jdk
VOLUME /tmp
ADD ./ /code
RUN cd /code && ./mvnw --no-transfer-progress install && cp /code/target/odm-task-dummy-executor.jar /app.jar && cd / && rm -rf /code && rm -rf /root/.m2
ENV JAVA_OPTS=""
EXPOSE 8082
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]