VERSION := 0.0.1

clean:
	mvn clean

install:
	mvn install -DskipTests

test:
	mvn test

start-gm:
	java -jar ./gm/target/gos-gm-${VERSION}-runner.jar

start-ui:
	java -jar ./ui/target/gos-ui-${VERSION}-runner.jar

start-villains:
	java -jar ./villains/target/gos-villains-${VERSION}-runner.jar

start-catapult-vertx:
	java -jar ./catapult-vertx/target/gos-catapult-vertx-${VERSION}-runner.jar

start-hero:
	java -jar ./heroes/target/gos-heroes-${VERSION}-runner.jar

start:
	make -j5 start-gm start-ui start-villains start-catapult-vertx start-heroes