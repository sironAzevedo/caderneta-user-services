FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN test -r /usr/share/zoneinfo/America/Sao_Paulo \
    && ln -snf /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime \
    && echo "America/Sao_Paulo" > /etc/timezone

ENV TZ=America/Sao_Paulo
ENV LANG=C.UTF-8

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ADD https://repo.maven.apache.org/maven2/co/elastic/apm/elastic-apm-agent/1.50.0/elastic-apm-agent-1.50.0.jar /app/elastic-apm-agent.jar
RUN chmod 644 /app/elastic-apm-agent.jar

ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "/app/app.jar"]
