[![Verify Build Workflow](https://github.com/Apitomy/apitomy-data-models/actions/workflows/verify.yaml/badge.svg)](https://github.com/Apitomy/apitomy-data-models/actions/workflows/verify.yaml?query=branch%3Amain)
[![Automated Release Notes by gren](https://img.shields.io/badge/%F0%9F%A4%96-release%20notes-00B2EE.svg)](https://github-tools.github.io/github-release-notes/)

# Apitomy Data Models (Java & Typescript)

## What is it?
This project is a library (Java and JavaScript compatible) to implement data models for OpenAPI and
AsyncAPI.

You can use this library to read an OpenAPI or AsyncAPI document, resulting in an instance of a data
model. The data model can then be read or manipulated. It can also be validated.

The data model can be accessed directly, but there is also a robust visitor pattern available for more
advanced analysis or transformation of the model.

This project is licensed under the [Apache License 2.0](LICENSE).

## Project Structure
This is a multi-module Maven project containing the following modules:

| Module | Description |
|---|---|
| `generator` | Core code generator that produces unified Java data models from specification meta-models |
| `generator-maven-plugin` | Maven plugin wrapper for the code generator |
| `generator-maven-plugin-tests` | Integration tests for the Maven plugin (optional, behind `plugin-tests` profile) |
| `data-models` | The data models library itself (Java + TypeScript via JSweet transpilation) |

## Usage
For details on how to use the library, see the documentation included with the library on
npmjs.com:

  [https://www.npmjs.com/package/@apitomy/data-models](https://www.npmjs.com/package/@apitomy/data-models)

This documentation can also be found in this repository here:

  [./data-models/src/main/ts/README.md](https://github.com/Apitomy/apitomy-data-models/blob/main/data-models/src/main/ts/README.md)

## Project Goals

### Purpose
The primary goal of this project is to be the official library to perform operations on OpenAPI and 
AsyncAPI documents.  Both for the purpose of analyzing/processing (including validation) a document 
as well as making changes to it.

The library should be usable as broadly as possible, both as a dependency of other Java, Typescript or
Javascript projects, as well as usable in a Java, Node.js, or browser based application.

### Usage Scenarios
Some example usage scenarios for this library include:
 
* An editor which can be used to create new documents (or modify existing ones).
* Auto-generate documentation from a document.
* Transform documents into other versions/formats.
* Auto-generate API clients.

### Dependencies
An important design decision is that this project does not have any runtime dependencies.  This means that 
you can use the library directly without including any other libraries. It also means you can use the 
project as a dependency in your projects without worrying about conflicts or dependency bloat.

## Project Status
Currently, the data model fully supports the following formats:

* OpenAPI 2.0, 3.0.x, 3.1.x, 3.2.x
* AsyncAPI 2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 3.0, 3.1
* OpenRPC 1.3, 1.4
* JSON Schema Draft 4, Draft 6, Draft 7, 2019-09, 2020-12

Barring oversights or bugs, the full document can be read and written successfully. In addition, the
visitor pattern has been fully implemented (along with both up and down traversing).

Validation of the OpenAPI/AsyncAPI specification exists, with all of the rules outlined in the spec having 
been implemented.

Please refer to the github repository's Issues and other resources for more information on the
current status of the project.

## Supported Specification Versions

One of the design goals of this library is to simultaneously support multiple versions of various API
specifications. As new versions are released, the library will be updated to include support for them.

### OpenAPI
* [OpenAPI 2.0](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/2.0.md)
* [OpenAPI 3.0.x](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md)
* [OpenAPI 3.1.x](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md)
* [OpenAPI 3.2.x](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.2.0.md)

### AsyncAPI
* [AsyncAPI 2.0 - 2.6](https://www.asyncapi.com/docs/reference/specification/v2.6.0)
* [AsyncAPI 3.0 - 3.1](https://www.asyncapi.com/docs/reference/specification/v3.0.0)

### OpenRPC
* [OpenRPC 1.3 - 1.4](https://spec.open-rpc.org/)

### JSON Schema
* Draft 4, Draft 6, Draft 7, 2019-09, 2020-12

## Building the Library
This section explains how to build, package, test, and publish the library.  If you are a developer
looking to make changes, this is a great place to start.

### Pre-Requisites
In order to build the library you will need to install the following tools:

* Java 17+
* Maven 3.8+
* Node.js 20+ (only needed for the `transpilation` profile)

### Clone and Configure
The first thing to do (obviously) is clone the repository.  Once you've cloned the git repository,
you can use maven to build the library into its two forms:  a Java JAR and a javascript library module.
This library is written using Java, but is then [transpiled from Java into Typescript using jsweet](http://www.jsweet.org/)
and then compiled and bundled using typescript and webpack.  All of this is done as part of the standard
maven build process:

```bash
git clone https://github.com/Apitomy/apitomy-data-models.git
cd apitomy-data-models
mvn clean package -Ptranspilation
```

**Note**: if using more recent versions of Java, there may be problems during the transpilation phase.  In that case,
you can try using the `build.sh` script provided at the root of the project.

### Test the Library
All testing is integrated into the maven build, so the standard `mvn clean package -Ptranspilation` will also execute
all unit tests.  If the maven build succeeds, then you know everything worked!


## Contribute Fixes and Features
This project is open source, and we welcome anybody who wants to participate and contribute!

### Get the code
The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/)
of this repository, and then clone your fork:

```bash
$ git clone git@github.com:<you>/apitomy-data-models.git
$ cd apitomy-data-models
$ git remote add upstream git://github.com/Apitomy/apitomy-data-models.git
```

At any time, you can pull changes from the upstream and merge them onto your master:

```bash
$ git checkout master       # switches to the 'master' branch
$ git pull upstream master  # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
$ git push origin           # pushes all the updates to your fork, which should be in-sync with 'upstream'
```

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

### Track Your Change
If you want to fix a bug or make any changes, please log an issue in the github 
[Issue Tracker](https://github.com/Apitomy/apitomy-data-models/issues) describing the bug or new 
feature. Then we highly recommend making the changes on a topic branch named with the issue 
number. For example, this command creates a branch for issue #7:

```bash
$ git checkout -b apitomy-data-models-7
```

After you're happy with your changes and all unit tests run successfully, commit your changes 
on your topic branch. Then it's time to check for and pull any recent changes that were made in
the official repository since you created your branch:

```bash
$ git checkout master         # switches to the 'master' branch
$ git pull upstream master    # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
$ git checkout apitomy-data-models-7  # switches to your topic branch
$ git rebase master           # reapplies your changes on top of the latest in master
                              # (i.e., the latest from master will be the new base for your changes)
```

If the pull grabbed a lot of changes, you should rerun the tests to make sure your changes are 
still good.  You should then push your changes to your fork, and then 
[generate a pull-request](http://help.github.com/pull-requests/) to submit your contribution:

```bash
$ git push origin apitomy-data-models-7         # pushes your topic branch into your public fork
```


## Links

- [Documentation](https://www.apitomy.io/projects/data-models/docs/)
- [Maven Central](https://central.sonatype.com/artifact/io.apitomy/apitomy-data-models)
- [npm Package](https://www.npmjs.com/package/@apitomy/data-models)
- [GitHub Repository](https://github.com/Apitomy/apitomy-data-models)
- [Apitomy Website](https://www.apitomy.io)
