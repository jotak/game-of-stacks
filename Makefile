VERSION := 0.0.1
STRIMZI_VERSION := 0.16.0
# List of all services (for image building / deploying)
SERVICES ?= web-j11hotspot villains-j11oj9 catapult-vertx-j11hotspot arrow-j11hotspot hero-native hero-j11hotspot
# Kube's CLI (kubectl or oc)
K8S_BIN ?= $(shell which kubectl 2>/dev/null || which oc 2>/dev/null)
# OCI CLI (docker or podman)
OCI_BIN ?= $(shell which podman 2>/dev/null || which docker 2>/dev/null)
OCI_BIN_SHORT = $(shell if [[ ${OCI_BIN} == *"podman" ]]; then echo "podman"; else echo "docker"; fi)
# Tag for docker images
OCI_TAG ?= dev
# Set MINIKUBE=true if you want to deploy to minikube (using registry addons)
MINIKUBE ?= true

ifeq ($(MINIKUBE),false)
OCI_DOMAIN ?= quay.io
OCI_DOMAIN_IN_CLUSTER ?= quay.io
PULL_POLICY ?= "IfNotPresent"
else ifeq ($(OCI_BIN_SHORT),podman)
OCI_DOMAIN ?= "$(shell minikube ip):5000"
OCI_DOMAIN_IN_CLUSTER ?= localhost:5000
PULL_POLICY ?= "Always"
else
OCI_DOMAIN ?= ""
OCI_DOMAIN_IN_CLUSTER ?= ""
PULL_POLICY ?= "Never"
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
	mvn package -f hero/pom.xml -Pnative -Dquarkus.native.container-build=true -DskipTests -Dnative-image.xmx=5g -Dquarkus.native.container-runtime=${OCI_BIN_SHORT};
#	mvn package -f arrow/pom.xml -Pnative -Dquarkus.native.container-build=true -DskipTests -Dnative-image.xmx=5g -Dquarkus.native.container-runtime=${OCI_BIN_SHORT};

test:
	mvn test

docker-eval:
	for svc in ${SERVICES} ; do \
		eval $$(minikube docker-env) ; \
		docker build -t gos/gos-$$svc:${OCI_TAG} -f ./k8s/$$svc.dockerfile ./ ; \
	done

docker:
	for svc in ${SERVICES} ; do \
		${OCI_BIN_SHORT} build -t ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} -f ./k8s/$$svc.dockerfile ./ ; \
		${OCI_BIN_SHORT} tag ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} localhost:5000/gos/gos-$$svc:${OCI_TAG} ; \
		${OCI_BIN_SHORT} push ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} ; \
	done

podman:
	for svc in ${SERVICES} ; do \
		${OCI_BIN_SHORT} build -t ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} -f ./k8s/$$svc.dockerfile ./ ; \
		${OCI_BIN_SHORT} tag ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} localhost:5000/gos/gos-$$svc:${OCI_TAG} ; \
		${OCI_BIN_SHORT} push --tls-verify=false ${OCI_DOMAIN}/gos/gos-$$svc:${OCI_TAG} ; \
	done

deploy-kafka:
	${K8S_BIN} create namespace kafka
	curl -L https://github.com/strimzi/strimzi-kafka-operator/releases/download/${STRIMZI_VERSION}/strimzi-cluster-operator-${STRIMZI_VERSION}.yaml | sed 's/namespace: .*/namespace: kafka/'   | ${K8S_BIN} apply -f - -n kafka
	${K8S_BIN} apply -f https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/${STRIMZI_VERSION}/examples/kafka/kafka-persistent-single.yaml -n kafka

deploy: .ensure-yq
	./genall.sh -pp ${PULL_POLICY} -d "${OCI_DOMAIN_IN_CLUSTER}" -t ${OCI_TAG} | ${K8S_BIN} apply -f -

reset:
	${K8S_BIN} scale deployment hero-native --replicas=0; \
	${K8S_BIN} scale deployment hero-j11hotspot --replicas=0; \
	${K8S_BIN} scale deployment arrow-j11hotspot --replicas=0; \
	${K8S_BIN} scale deployment villains-j11oj9 --replicas=0;

arrow-scaling-hero-native-vs-hotspot--native: reset
	${K8S_BIN} scale deployment hero-native --replicas=5; \
	${K8S_BIN} scale deployment arrow-j11hotspot --replicas=1; \
	${K8S_BIN} scale deployment villains-j11oj9 --replicas=1;

arrow-scaling-hero-native-vs-hotspot--hotspot: reset
	${K8S_BIN} scale deployment hero-j11hotspot --replicas=5; \
	${K8S_BIN} scale deployment arrow-j11hotspot --replicas=1; \
	${K8S_BIN} scale deployment villains-j11oj9 --replicas=1;

start-mixed:
	${K8S_BIN} delete pods -l type=game-object
	${K8S_BIN} scale deployment hero-native --replicas=2; \
	${K8S_BIN} scale deployment hero-j11hotspot --replicas=2; \
	${K8S_BIN} scale deployment arrow-j11hotspot --replicas=1; \
	${K8S_BIN} scale deployment villains-j11oj9 --replicas=1;

more-villains:
	./gentpl.sh villains-j11oj9 -pp ${PULL_POLICY} -d "${OCI_DOMAIN_IN_CLUSTER}" -t ${OCI_TAG} \
    	| yq w --tag '!!str' - "spec.template.spec.containers[0].env.(name==WAVES_SIZE).value" 10 \
    	| yq w --tag '!!str' - "spec.template.spec.containers[0].env.(name==WAVES_COUNT).value" 4 \
		| ${K8S_BIN} apply -f -

much-more-villains:
	./gentpl.sh villains-j11oj9 -pp ${PULL_POLICY} -d "${OCI_DOMAIN_IN_CLUSTER}" -t ${OCI_TAG} \
    	| yq w --tag '!!str' - "spec.template.spec.containers[0].env.(name==WAVES_SIZE).value" 30 \
    	| yq w --tag '!!str' - "spec.template.spec.containers[0].env.(name==WAVES_COUNT).value" 4 \
		| ${K8S_BIN} apply -f -

scale-service:
	${K8S_BIN} scale deployment $$svc --replicas=$(count)

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
