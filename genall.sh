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

./gentpl.sh web-j11hotspot -r quarkus $OPTS
./gentpl.sh villains-j11oj9 -r vertx $OPTS
./gentpl.sh catapult-vertx-j11hotspot -r vertx $OPTS
./gentpl.sh arrow-native -r quarkus $OPTS
./gentpl.sh arrow-j11hotspot -r quarkus $OPTS
./gentpl.sh hero-native -r quarkus $OPTS
./gentpl.sh hero-j11hotspot -r quarkus $OPTS
