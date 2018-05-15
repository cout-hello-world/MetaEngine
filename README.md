# MetaEngine
This program is designed to take the input of multiple UCI engines and to
use that information to act as a UCI engine. For example, it can be used to
combine a neural-network and/or Monte Carlo based engine with a traditional
alpha-beta search engine.

## Build instructions
To build and run this application, you should have at least version 1.8 of
of the Java Development Kit and a reasonably new version of Apache Ant with
Ivy.

### Preparing to build on Debian Stretch
Install a JDK, Ant, and Ivy:
```
sudo apt-get install openjdk-8-jdk ant ivy
```

In a perfect world, the above would be enough, but as of the time of this
writing, you must also create a link to the Ivy jar in a place that Ant can
find it:
```
mkdir -p $HOME/.ant/lib
ln -s /usr/share/java/ivy.jar $HOME/.ant/lib/
```
assuming `ivy.jar` is in `/usr/share/java/`

### Building

Build with the command `ant`. This should
create an executable jar in the `target` directory.

### Building the distribution
Build the distribution with `ant dist`. This will create a `.zip` file in the
`target` directory with an executable jar and all its runtime dependencies.
