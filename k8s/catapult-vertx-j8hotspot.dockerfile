FROM adoptopenjdk/openjdk8:alpine-slim

EXPOSE 8081 9090

# Copy dependencies
# COPY catapult-vertx/target/dependency/* /deployment/libs/

# Copy classes
# COPY catapult-vertx/target/classes /deployment/classes

# Temp: fat jar
COPY catapult-vertx/target/gos-catapult-vertx-0.0.1-runner.jar /deployment/

RUN chgrp -R 0 /deployment && chmod -R g+rwX /deployment

CMD java -Dvertx.disableDnsResolver=true -jar /deployment/gos-catapult-vertx-0.0.1-runner.jar
