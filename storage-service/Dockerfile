FROM openjdk:17-oracle

EXPOSE 8081

ADD target/storage-service-0.0.1.jar storage-service.jar

ENTRYPOINT ["java","-jar","storage-service.jar"]