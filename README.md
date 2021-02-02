# cz3002-be
[![Build Status](https://travis-ci.com/bryanscw/cz3002-be.svg?token=WtBjbJiVVLc1zKyr5kpw&branch=main)](https://travis-ci.com/bryanscw/cz3002-be)

## 1. Introduction
This is the repository used for the development of the backend of CZ3002 Project, titled cogbench.
For dependencies/deployment/testing steps, refer to the [README.md](src/README.md) in cogbench.

## 2. Contributing
### 2.1 Style Guide - Java

We conform to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Maven can helpfully take care of that for you before you commit:

```text
$ mvn spotless:apply
```

Formatting will be checked automatically during the `verify` phase. This can be skipped temporarily:

```text
$ mvn spotless:check  # Check is automatic upon `mvn verify`
$ mvn verify -Dspotless.check.skip
```

If you're using IntelliJ, you can import [these code style settings](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml) if you'd like to use the IDE's reformat function as you develop.