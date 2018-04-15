Before submitting a pull request, please make sure the following is done:

1. Fork [the repository](https://github.com/jenkinsci/postbuildscript-plugin) and create your branch from `master`.
2. If you've fixed a bug or added code that should be tested, please add JUnit tests.
3. Ensure the test suite passes (`mvn clean verify`).
4. Run `mvn hpi:run` and go to http://localhost:8080/jenkins/ to test your changes. Add a job that produces your bug / feature scenario.
