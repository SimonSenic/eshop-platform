FROM openjdk:17-oracle

EXPOSE 8083

ADD target/payment-service-0.0.1.jar payment-service.jar

ENTRYPOINT ["java","-jar","payment-service.jar"]