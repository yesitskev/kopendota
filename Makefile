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

# Needs a GitHub token carrying write:packages, as gpr.user/gpr.key in ~/.gradle/gradle.properties or as
# GITHUB_ACTOR/GITHUB_TOKEN in the environment.
server-publish:
	$(GRADLE) publishMavenPublicationToGitHubPackagesRepository

server-publish-local:
	$(GRADLE) publishToMavenLocal
