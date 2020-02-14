#!/usr/bin/env bash

if [[ "$1" = "--help" ]]; then
	echo "Syntax: genall.sh <options>"
	echo "Options:"
	echo "  -d  DOMAIN       Domain for image name (e.g. quay.io)"
	echo "  -pp PULL_POLICY  Image pull policy (e.g. IfNotPresent)"
	echo "  -t  TAG          Image tag (e.g. dev)"
	exit
fi

# Forward options
OPTS="$*"

./gentpl.sh web-hotspot -r quarkus $OPTS
./gentpl.sh villains-oj9 -r vertx $OPTS
./gentpl.sh catapult-vertx-hotspot -r vertx $OPTS
./gentpl.sh arrow-native -r quarkus $OPTS
./gentpl.sh arrow-hotspot -r quarkus $OPTS
./gentpl.sh hero-native -r quarkus $OPTS
./gentpl.sh hero-hotspot -r quarkus $OPTS
