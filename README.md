# cz3002-be
[![Build Status](https://travis-ci.com/bryanscw/cz3002-be.svg?token=WtBjbJiVVLc1zKyr5kpw&branch=main)](https://travis-ci.com/bryanscw/cz3002-be)

## 1. Introduction
This is the repository used for the development of the backend of CZ3002 Project, titled cogbench.

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

## 3. Dependencies
This project requires the following dependencies:
1. Java 11
2. Maven
3. Build-essential
4. [Docker](https://docs.docker.com/engine/install/)
5. [Docker-compose](https://docs.docker.com/compose/install/)

To install the required dependencies, run the following commands:
```
sudo apt install openjdk-11-jdk maven build-essential
```

## 4. Deployment
This project uses Docker and Docker-compose to deploy the application and the database in stable environments. To deploy, run the following command:
```
sudo make all
```

## 5. Notes
### 5.1. Makefile
The Makefile contains the following options:
```
# Executes stop, run-db, test, stop-db, build and deploy steps
sudo make all

# Executes the test for cogbench application
sudo make test

# Builds the jar file for the application
sudo make build

# Deploys both database and application
sudo make deploy

# Removes both database and application
sudo make stop

# Removes and re-deploys both database and application
sudo make restart

# Cleans up all unused Docker stuff (networks, containers etc)
sudo make clean

# Generates javadocs for the application
sudo make javadocs

# Starts a database for the test to run with
sudo make run-db

# Removes the database used for the test
sudo make stop-db
```

### 5.2. Persistent data
The persistent data can be found [here](src/main/resources/data.sql), with the credentials of their following owners commented above the query.