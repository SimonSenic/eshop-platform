FROM openjdk:17-oracle

EXPOSE 8082

ADD target/order-service-0.0.1.jar order-service.jar

ENTRYPOINT ["java","-jar","order-service.jar"]