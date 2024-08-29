# Etapa 1: Construção da aplicação
FROM --platform=$BUILDPLATFORM maven:3-amazoncorretto-17 AS build

# Criação do diretório de trabalho
RUN mkdir -p /workspace
WORKDIR /workspace

# Copia as dependências
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml /workspace
COPY src /workspace/src

# Compila o projeto e gera o JAR
RUN mvn -B -f pom.xml clean package -DskipTests

# Etapa 2: Criação da imagem final com suporte multi-arquitetura
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jdk

# Copia o JAR gerado para o contêiner final
COPY --from=build /workspace/target/*.jar /workspace/app.jar

# Expõe a porta para a aplicação
#EXPOSE 8002

# Comando de entrada para iniciar a aplicação
ENTRYPOINT ["java","-jar","/workspace/app.jar"]


#OLD
# FROM maven:3-amazoncorretto-17 AS build
# RUN mkdir -p /workspace
# WORKDIR /workspace
# COPY settings.xml /root/.m2/settings.xml
# COPY pom.xml /workspace
# COPY src /workspace/src
# RUN mvn -B -f pom.xml clean package -DskipTests
#
# FROM openjdk:17
# COPY --from=build /workspace/target/*.jar app.jar
# EXPOSE 8002
#
# ADD ./docker-entrypoint.sh /
# RUN ["chmod", "+x", "/docker-entrypoint.sh"]
# ENTRYPOINT ["/docker-entrypoint.sh"]
# ENTRYPOINT ["java","-jar","/workspace/app.jar"]