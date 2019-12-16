FROM adoptopenjdk/openjdk8-openj9:alpine-slim

EXPOSE 8081 9090

# Copy dependencies
# COPY ui/target/dependency/* /deployment/libs/

# Copy classes
# COPY ui/target/classes /deployment/classes

# Temp: fat jar
COPY ui/target/gos-ui-0.0.1-runner.jar /deployment/

RUN chgrp -R 0 /deployment && chmod -R g+rwX /deployment

CMD java -Dvertx.disableDnsResolver=true -classpath /deployment/gos-ui-0.0.1-runner.jar demo.gos.ui.UIVerticle
