FROM maven:3.8.7-eclipse-temurin-11-alpine

VOLUME ~/.m2:/root/.m2
WORKDIR /app
COPY . .
#COPY ./src/main/resources/schema /app
#COPY pom.xml /app

RUN mvn clean package -DskipTests

EXPOSE 7001
ENTRYPOINT ["java","-jar","target/s4e-warehouse.jar"]