FROM adoptopenjdk/openjdk11-openj9:alpine-slim

EXPOSE 8081 9090

# Copy dependencies
# COPY villains/target/dependency/* /deployment/libs/

# Copy classes
# COPY villains/target/classes /deployment/classes

# Temp: fat jar
COPY villains/target/gos-villains-0.0.1-runner.jar /deployment/

RUN chgrp -R 0 /deployment && chmod -R g+rwX /deployment

CMD java -Dvertx.disableDnsResolver=true -jar /deployment/gos-villains-0.0.1-runner.jar
