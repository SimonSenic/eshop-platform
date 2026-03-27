FROM openjdk:17-oracle

EXPOSE 8084

ADD target/notification-service-0.0.1.jar notification-service.jar

ENTRYPOINT ["java","-jar","notification-service.jar"]