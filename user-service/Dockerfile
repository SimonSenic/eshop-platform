FROM openjdk:17-oracle

EXPOSE 8080

ADD target/user-service-0.0.1.jar user-service.jar

ENTRYPOINT ["java","-jar","user-service.jar"]