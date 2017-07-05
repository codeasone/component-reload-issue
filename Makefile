# http://stackoverflow.com/a/26339924/1317031
.PHONY: contract
contract:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | \
	awk -v RS= -F: '/^# File/,/^# Finished Make data base/ \
	{if ($$1 !~ "^[#.]") {print $$1}}' | \
	sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$' | \
	xargs

clean:
	lein clean

build:
	lein uberjar

deps-check:
	lein ancient check

deps-upgrade:
	lein ancient upgrade :interactive

deps-tree:
# lein pom
# mvn dependency:tree
#	lein deps-tree
	lein vizdeps

install: test
	lein install

run:
	lein trampoline run

test: | clean build test-lint test-unit test-integration test-acceptance

test-lint:
	lein kibit

test-unit:
	lein trampoline eftest

test-integration:
	lein trampoline eftest :integration

test-acceptance: clean build
	true

test-coverage: build
	lein cloverage

config:
	lein cprint
