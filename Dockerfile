FROM java:8-alpine
MAINTAINER Thiago Lima <t.augusto@gmail.com>

ADD target/reward-system-standalone.jar /reward-system/app.jar
ADD resources/input.txt /reward-system/input.txt
ADD resources/README.md /reward-system/README.md

EXPOSE 8080

CMD ["java", "-jar", "/reward-system/app.jar", "/reward-system/input.txt"]
