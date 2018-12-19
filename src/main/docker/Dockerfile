FROM openjdk:8-jdk

ARG APP_JAR=${project.artifactId}-${project.version}.jar

ENV DEPLOY_DIR /app

EXPOSE 8443

USER 0

VOLUME /tmp

RUN mkdir ${DEPLOY_DIR}

COPY /maven/$APP_JAR $DEPLOY_DIR/app.jar

RUN find $DEPLOY_DIR -name '*.jar' -exec chmod a+x {} +

CMD java -Djava.security.egd=file:/dev/./urandom -jar ${DEPLOY_DIR}/app.jar

USER 1000