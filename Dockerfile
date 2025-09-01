# Etapa 1: Construção da aplicação
FROM --platform=$BUILDPLATFORM maven:3-amazoncorretto-17 AS build

# Criação do diretório de trabalho
RUN mkdir -p /workspace
WORKDIR /workspace

# Copia as dependências
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml /workspace
COPY src /workspace/src

# Monta o GITHUB_TOKEN como um secret e executa o build do Maven
# O secret fica disponível em /run/secrets/GITHUB_TOKEN
RUN --mount=type=secret,id=GITHUB_TOKEN \
    mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>github</id><username>dummy</username><password>$(cat /run/secrets/GITHUB_TOKEN)</password></server></servers></settings>" > /root/.m2/settings.xml && \
    mvn -B -f pom.xml clean package -DskipTests

# Etapa 2: Criação da imagem final com suporte multi-arquitetura
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jdk

# Copia o JAR gerado para o contêiner final
COPY --from=build /workspace/target/*.jar /workspace/app.jar

# Expõe a porta para a aplicação
#EXPOSE 8002

# Comando de entrada para iniciar a aplicação
ENTRYPOINT ["java","-jar","/workspace/app.jar"]