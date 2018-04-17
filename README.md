# MetaEngine
This program is designed to take the input of multiple UCI engines and to
use that information to act as a UCI engine. For example, it can be used to
combine a neural-network and/or Monte Carlo based engine with a traditional
alpha-beta search engine.

## Building
To build and run this application, you should have at least version 1.8 of
of the Java Development Kit and a reasonably new version of Apache Ant.

Build with the command `ant`.
This should give you an executable jar in the `dist` subdirectory. This jar
can be run (from the same directory as the jar) as a UCI engine
with `java -jar MetaEngine.jar`.
