FROM openjdk:8-jdk-alpine
MAINTAINER NaveedAmanat
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} reportservice.jar
ENTRYPOINT ["java","-jar","/reportservice.jar"]
EXPOSE 8787