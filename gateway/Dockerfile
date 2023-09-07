FROM openjdk:17-oracle

EXPOSE 9090

ADD target/gateway-0.0.1.jar gateway.jar

ENTRYPOINT ["java","-jar","gateway.jar"]