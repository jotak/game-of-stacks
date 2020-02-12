VERSION := 0.0.1
STRIMZI_VERSION := 0.16.0
# List of all services (for image building / deploying)
SERVICES ?= web-hotspot villains-oj9 catapult-vertx-hotspot arrow-native arrow-hotspot hero-native hero-hotspot
# Kube's CLI (kubectl or oc)
K8S_BIN ?= $(shell which kubectl 2>/dev/null || which oc 2>/dev/null)
# OCI CLI (docker or podman)
OCI_BIN ?= $(shell which podman 2>/dev/null || which docker 2>/dev/null)
OCI_BIN_SHORT = $(shell if [[ ${OCI_BIN} == *"podman" ]]; then echo "podman"; else echo "docker"; fi)
# Tag for docker images
OCI_TAG ?= dev
# Set MINIKUBE=true if you want to deploy to minikube (using registry addons)
MINIKUBE ?= true
ifeq ($(MINIKUBE),true)
OCI_DOMAIN ?= "$(shell minikube ip):5000"
else
OCI_DOMAIN ?= quay.io
endif

.ensure-yq:
	@command -v yq >/dev/null 2>&1 || { echo >&2 "yq is required. Grab it on https://github.com/mikefarah/yq"; exit 1; }

DOCKER_ENV = ""

clean:
	mvn clean

build: install

install:
	mvn install -DskipTests

build-native:
	mvn package -f hero/pom.xml -Pnative -Dquarkus.native.container-build=true -DskipTests -Dnative-image.xmx=5g -Dquarkus.native.container-runtime=${OCI_BIN_SHORT}; \
	mvn package -f arrow/pom.xml -Pnative -Dquarkus.native.container-build=true -DskipTests -Dnative-image.xmx=5g -Dquarkus.native.container-runtime=${OCI_BIN_SHORT};

test:
	mvn test

docker:
	for svc in ${SERVICES} ; do \
		eval $$(minikube docker-env) ; \
		docker build -t gos/gos-$$svc:${OCI_TAG} -f ./k8s/$$svc.dockerfile ./ ; \
	done

podman:
	for svc in ${SERVICES} ; do \
		podman build -t ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} -f ./k8s/$$svc.dockerfile ./ ; \
	done

deploy-kafka:
	${K8S_BIN} create namespace kafka
	curl -L https://github.com/strimzi/strimzi-kafka-operator/releases/download/${STRIMZI_VERSION}/strimzi-cluster-operator-${STRIMZI_VERSION}.yaml | sed 's/namespace: .*/namespace: kafka/'   | ${K8S_BIN} apply -f - -n kafka
	${K8S_BIN} apply -f https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/${STRIMZI_VERSION}/examples/kafka/kafka-persistent-single.yaml -n kafka

deploy-minikube: .ensure-yq
	for svc in ${SERVICES} ; do \
		cat k8s/$$svc.yml \
			| kubectl apply -f - ; \
	done

deploy-minikube-podman: .ensure-yq
	for svc in ${SERVICES} ; do \
		podman tag ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} localhost:5000/gos/gos-$$svc:${OCI_TAG} ; \
		podman push --tls-verify=false ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} ; \
		cat k8s/$$svc.yml \
			| yq w - spec.template.spec.containers[0].imagePullPolicy Always \
			| yq w - spec.template.spec.containers[0].image localhost:5000/gos/gos-$$svc:${OCI_TAG} \
			| kubectl apply -f - ; \
	done

deploy-other:
	for svc in ${SERVICES} ; do \
		${K8S_BIN} apply -f k8s/$$svc.yml ; \
	done

ifneq ($(MINIKUBE),true)
deploy: deploy-other
else ifeq ($(OCI_BIN_SHORT),podman)
deploy: deploy-minikube-podman
else
deploy: deploy-minikube
endif

reset:
	kubectl scale deployment hero-native --replicas=0; \
	kubectl scale deployment hero-hotspot --replicas=0; \
	kubectl scale deployment arrow-native --replicas=0; \
	kubectl scale deployment villains-oj9 --replicas=0;

arrow-scaling-hero-native-vs-hotspot--native: reset
	kubectl scale deployment hero-native --replicas=5; \
	kubectl scale deployment arrow-native --replicas=1; \
	kubectl scale deployment villains-oj9 --replicas=1;

arrow-scaling-hero-native-vs-hotspot--hotspot: reset
	kubectl scale deployment hero-hotspot --replicas=5; \
	kubectl scale deployment arrow-native --replicas=1; \
	kubectl scale deployment villains-oj9 --replicas=1;

arrow-scaling-hero-native-vs-hotspot--mixed: reset
	kubectl scale deployment hero-native --replicas=2; \
	kubectl scale deployment hero-hotspot --replicas=2; \
	kubectl scale deployment arrow-native --replicas=1; \
	kubectl scale deployment villains-oj9 --replicas=1;

scale-service:
	kubectl scale deployment $$svc --replicas=$(count)

expose:
	@echo "URL: http://localhost:8081/"
	${K8S_BIN} port-forward svc/web 8081:8081

undeploy-go:
	${K8S_BIN} delete all -l project=gos -l type=game-object

undeploy:
	${K8S_BIN} delete all -l project=gos

restart-pods-go:
	${K8S_BIN} delete pods -l project=gos -l type=game-object

start-villains:
	export WAVES_DELAY=15 WAVES_SIZE=30 WAVES_COUNT=5 && java -jar ./villains/target/gos-villains-${VERSION}-runner.jar

start-catapult-vertx:
	export Y="150" && java -jar ./catapult-vertx/target/gos-catapult-vertx-${VERSION}-runner.jar

start-catapult-quarkus:
	export Y="350" && java -jar ./catapult-quarkus/target/gos-catapult-quarkus-${VERSION}-runner.jar

start-ned:
	export Y="150" SPEED="70" NAME="ned-stark" USE_BOW="true" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

dev-web:
	cd web && mvn compile quarkus:dev

start-web:
	java -jar ./web/target/gos-web-${VERSION}-runner.jar

start-aria:
	export Y="350" SPEED="70" NAME="aria-stark" USE_BOW="true" && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar

start-random-hero:
	unset X Y SPEED ID USE_BOW && java -jar ./hero/target/gos-hero-${VERSION}-runner.jar;

start-arrow:
	java -jar ./arrow/target/gos-arrow-${VERSION}-runner.jar

start-kafka:
	cd kafka; docker-compose up

start:
	make -j7 start-arrow start-villains start-catapult-vertx start-catapult-quarkus start-aria start-ned start-random-hero
