#!/usr/bin/env bash

if [[ "$#" -lt 1 || "$1" = "--help" ]]; then
	echo "Syntax: gentpl.sh <service name> <other options>"
	echo ""
	exit
fi

FULL_NAME="$1"

# A WTF moment? see this: https://www.linuxjournal.com/article/8919
BASE_NAME="${1%-*}"
VARIANT="${1##*-}"

PULL_POLICY="Never"
DOMAIN=""
TAG="dev"
RUNTIME="$VARIANT"
LAST_ARG=""

for arg in "$@"
do
    if [[ "$LAST_ARG" = "-pp" ]]; then
        PULL_POLICY="$arg"
        LAST_ARG=""
    elif [[ "$LAST_ARG" = "-d" ]]; then
        DOMAIN="$arg/"
        LAST_ARG=""
    elif [[ "$LAST_ARG" = "-t" ]]; then
        TAG="$arg"
        LAST_ARG=""
    elif [[ "$LAST_ARG" = "-r" ]]; then
        RUNTIME="$arg-$VARIANT"
        LAST_ARG=""
    else
        LAST_ARG="$arg"
    fi
done

echo "# BASE_NAME=$BASE_NAME, VARIANT=$VARIANT, RUNTIME=$RUNTIME, PULL_POLICY=$PULL_POLICY, DOMAIN=$DOMAIN"

FULL_NAME="$BASE_NAME-$VARIANT"
IMAGE="${DOMAIN}gos/gos-$FULL_NAME:$TAG"

cat ./deploy/k8s/$BASE_NAME-base.yml \
    | yq w - metadata.labels.version $VARIANT \
    | yq w - metadata.name $FULL_NAME \
    | yq w - spec.selector.matchLabels.version $VARIANT \
    | yq w - spec.template.metadata.labels.version $VARIANT \
    | yq w - spec.template.metadata.labels.runtime $RUNTIME \
    | yq w - spec.template.spec.containers[0].imagePullPolicy $PULL_POLICY \
    | yq w - spec.template.spec.containers[0].image $IMAGE \
    | yq w - spec.template.spec.containers[0].name $FULL_NAME \
    | yq w - "spec.template.spec.containers[0].env.(name==RUNTIME).value" $RUNTIME

echo "---"
