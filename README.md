<img src="./tnoodle_logo_1024.png" alt="TNoodle Logo" height="128px"/>

# TNoodle-LIB

TNoodle is a software suite that contains the official WCA scramble program. It consists of the core scrambling code (primarily written in Java) as well as a UI and server to generate a fully autonomous JAR file

You are looking at the core scrambling code portion.
This repository hosts an independent build for the essential mechanics that generate Java `String`s representing twisty puzzle scrambles.

If you are interested in the webserver part, look [here](https://github.com/thewca/tnoodle)

[![Build Status](https://github.com/thewca/tnoodle-lib/actions/workflows/gradle.yml/badge.svg)](https://github.com/thewca/tnoodle-lib/actions/workflows/gradle.yml)

## WCA Scramble Program

The official scramble program for the [World Cube Association](https://www.worldcubeassociation.org/) has been part of the TNoodle project since January 1, 2013. It will contain the sole official scramble program for the foreseeable future.

All WCA official competitions must always use the current version of the official scramble program. This is available from <https://www.worldcubeassociation.org/regulations/scrambles/>

Note that only the scramble program part of TNoodle is "official". Other TNoodle projects may be convenient for certain uses (including at official competitions), but do not have any official status.

### "Scramble Program" vs. "Scrambler"

Officially, `TNoodle-lib` is a [scramble program](https://www.worldcubeassociation.org/regulations/#4f), while a [scrambler](https://www.worldcubeassociation.org/regulations/#A2b) is a human. It is fine to refer to TNoodle as a "scrambler" colloquially, but please try to use the official convention wherever possible.

## Project Details

TNoodle is organised as a multi-project [Gradle](https://gradle.com) build. The build files are written in the type-safe `Kotlin` dialect.

Every sub-project has its individual artifact configuration and `build.gradle` file. Furthermore, there is a central `buildSrc` folder,
which is automatically sourced by Gradle. It contains common code and shared configuration setups.

### Overview

Gradle is served through the use of a `Gradle wrapper` available as `gradlew` (UNIX systems) or `gradlew.bat` (DOS systems)
It is recommended to set up an alias to simplify task generation, along the lines of `alias gw='./gradlew --parallel'`.

Get an overview of the core project tasks by executing

    ./gradlew tasks

### Setup

Gradle automagically handles all dependencies for you. You just need an Internet connection upon your first build run!

### WCA Scramble Program

When you're ready to develop, just go ahead and code! There is no UI to this part of TNoodle.
You can always execute the full integration and unit test suite via:

    ./gradlew :scrambles:check

To build a distributable `.jar` file, run:

    ./gradlew :scrambles:assemble

You cannot run the resulting `.jar`, because it is conceived as a Maven artifact.
We recommend using the online distribution [hosted at Maven Central](https://mvnrepository.com/artifact/org.worldcubeassociation.tnoodle)

If you _really_ want to use a local build in your project, execute:

    ./gradlew :scrambles:publishToMavenLocal

and point whatever Maven-style build tool you're using to your local `.m2` repository.

_Important note: You must never use a custom build for any official competitions._ [Contact the WCA Board and the WRC](https://www.worldcubeassociation.org/contact) if you have any questions about this.

### Notes

-   Each project is a fully fledged Gradle project (they each have a `build.gradle.kts` file). Your IDE should be able to import Gradle build structures nowadays. if not, this is a good indicator that your IDE is outdated and should be replaced.
