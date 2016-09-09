FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/app-standalone.jar /nubank-invitation-reward-program/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/nubank-invitation-reward-program/app.jar"]
