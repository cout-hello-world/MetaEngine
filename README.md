# MetaEngine
This program is designed to take the input of multiple UCI engines and to
use that information to act as a UCI engine. For example, it can be used to
combine a neural-network and/or Monte Carlo based engine with a traditional
alpha-beta search engine.

## Overview
MetaEngine is configured to run UCI engines as child processes. Some of these
are marked `EVALUATOR`, and some of them are marked `GENERATOR`
(see `example_config.toml` for details on how to do this).
The `GENERATOR`s generate moves and the `EVALUATOR`s determine which moves
lead to the best outcomes. The hope of this project is that by combining
neural network-based chess engines with traditional ones, we can combine
(for example) the positional insight of the neural-network engines with the
tactical sharpness of traditional engines.

## Running the pre-built distribution
All you need to run the distribution is a Java Runtime Environment version 1.8
or greater.

To run the pre-built distribution, (made with `ant dist`) first, download it
from the most recenct release page along with `example_config.toml`; then,
extract the `.zip` file and run with
`java -jar <path_to_MetaEngine_jar> <path_to_config_file>`).
You must edit the configuration file to suit your own installed chess engines
(see "Configuration" section below).

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
mkdir -p "$HOME/.ant/lib/"
ln -s /usr/share/java/ivy.jar "$HOME/.ant/lib/"
```
assuming `ivy.jar` is in `/usr/share/java/`

### Building
Build with the command `ant`. This should
create an executable jar in the `target` directory.

### Building the distribution
Build the distribution with `ant dist`. This will create a `.zip` file in the
`target` directory with an executable jar and all its runtime dependencies.

## Usage
MetaEngine can be run with
`java -jar <path_to_MetaEngine_jar> <path_to_config_file>` where the
library dependencies of MetaEngine are laid out as follows:
```
MetaEngine-$VERSION.jar
lib/
    main/
        MetaEngine-Dependncy1.jar
        MetaEngine-Dependncy2.jar
        ...
```
This is the layout which `ant jar` will create in `target/`.
On POSIX systems, after `ant jar`, MetaEngine can be run with the
`./MetaEngine` shell script.

## Configuration
For an example configuration file, and information on how to configure
MetaEngine, see `example_config.toml`

### Ideas for configuration
The way that I imagine MetaEngine being used (although this certainly isn't the
only way), is to have one `GENERATOR` which is a traditional alpha-beta search
engine, and an other which is a neural network-based engine. Also, the
evaluators would be identical copies of an alpha-beta engine. The neural
network engine would be given a small bias to prevent the alpha-beta engine
from always choosing its own move. These ideas are reflected in
`example_config.toml`.

## Contributing
There are two main ways to contribute to this project. One is to submit patches,
and the other is to test configurations.

### Testing
If you have powerful GPU hardware, your testing of MetaEngine configurations
against top chess engines would be greatly appreciated. The program
[`cutechess`](https://github.com/cutechess/cutechess) can be used for running
engine-engine matches for the purpose of determining the strength of a given
MetaEngine configuration.

### Contributing code
Contribute code by making a pull request on
[this repository](https://github.com/cout-hello-world/MetaEngine).
By contributing code, you agree to license your contribution under the
Apache 2.0 license.

### To do
- Improve performance so engines get more of the time
- Implement a reasonable `go infinite`
- Tweak time settings to increase strength (possibly expose these settings in
  the configuration file)
- Print useful `info` messages
- Other improvements for UCI support
- Improve test coverage
- Improve code documentation
- Other things you might think need to be done...
