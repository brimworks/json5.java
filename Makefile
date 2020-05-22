publish:
	@$(if $(VERSION),,$(error Please provide a version number to publish via VERSION=x.x.x))true
	VERSION=$(VERSION) ./gradlew bintrayUpload
	git tag $(VERSION)
	git push --tags origin $(VERSION)


publishToMavenLocal:
	@$(if $(VERSION),,$(error Please provide a version number to publish via VERSION=x.x.x))true
	VERSION=$(VERSION) ./gradlew publishToMavenLocal
