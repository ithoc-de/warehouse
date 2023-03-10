ARG DOCKER_PROXY=${DOCKER_PROXY}
FROM ${DOCKER_PROXY}eclipse-temurin:11-jdk-alpine as builder
VOLUME ~/.m2:/root/.m2
WORKDIR /app

COPY target/warehouse.jar .

EXPOSE 7001

ENV DB_HOST=${DB_HOST}
ENV DB_PORT=${DB_PORT}
ENV EPAGES_API_KEY=${EPAGES_API_KEY}
ENV KEYCLOAK_HOST=${KEYCLOAK_HOST}
ENV KEYCLOAK_PORT=${KEYCLOAK_PORT}

ENTRYPOINT ["java","-jar","warehouse.jar"]