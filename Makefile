GRADLE := ./gradlew

# Dokka's HTML output. Generated, and wiped on every run, so nothing hand-written belongs here — the module and
# package overviews that feed it live in dokka/.
DOCS_DIR := docs

.PHONY: server-docs server-docs-open server-docs-clean server-test server-test-class server-test-method \
	server-publish server-publish-local

server-docs:
	$(GRADLE) dokkaGenerate

server-docs-open: server-docs
	open $(DOCS_DIR)/index.html

server-docs-clean:
	$(GRADLE) clean
	rm -rf $(DOCS_DIR)

server-test:
	$(GRADLE) test

server-test-class:
	$(GRADLE) test --tests "$(TEST_CLASS)"

server-test-method:
	$(GRADLE) test --tests "$(subst #,.,$(TEST_METHOD))"

# Uploads a deployment to Maven Central without releasing it, leaving the last step to the portal. Releasing is
# the release workflow's job, and is irreversible. Needs mavenCentralUsername/mavenCentralPassword and a signing
# key in ~/.gradle/gradle.properties.
server-publish:
	$(GRADLE) publishToMavenCentral

server-publish-local:
	$(GRADLE) publishToMavenLocal
