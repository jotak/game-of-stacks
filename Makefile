VERSION := 0.0.1

clean:
	mvn clean

build: install

install:
	mvn install -DskipTests

test:
	mvn test

start-ui:
	java -jar ./ui/target/gos-ui-${VERSION}-runner.jar

start-villains:
	java -jar ./villains/target/gos-villains-${VERSION}-runner.jar

start-catapult-vertx:
	java -jar ./catapult-vertx/target/gos-catapult-vertx-${VERSION}-runner.jar

start-ned:
	export shortId="ned" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

start-aria:
	export shortId="aria" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

start:
	make -j5 start-ui start-villains start-catapult-vertx start-ned start-aria
