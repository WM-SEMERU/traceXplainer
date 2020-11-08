# Jenkins Download and Setup

1. `wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -`

2. `sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ >  /etc/apt/sources.list.d/jenkins.list'`

3. `sudo apt-get update`

4. `sudo apt-get install jenkins`

Running this, then starting the service with systemctl runs the server on port 8080 by default, which is where
we will keep it, at least for now.









## How to set up Jenkins for this project (turns out this is outdated)

*Note: This was done on a windows machine. The process may be similar but probably will not be the same
on other operating systems.*

### Download and Setup

1. Download [Maven](https://maven.apache.org/download.cgi)

2. Download [Java JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
*Note: The JDK MUST be version 8 or jenkins will not work.*

3. Run the java installer, which will put the JDK in the correct location, then extract Maven to your
Program Files directory (e.g. C:\Program Files\Maven xxx). This isn't entirely necessary but makes
life a lot easier.

4. Add the \bin folder of Maven to your PATH system environment variable, then create a new environment variable
called JAVA_HOME and point it at the JDK (Not the \bin folder, but the parent folder). If you already have a
JAVA_HOME variable, edit the one you've got. Maven looks at that variable for its JDK and will break if it sees
the wrong version.

### Create Jenkins project

Now that all of that is setup, open a CLI and navigate to a new folder for the Jenkins project. Type

`mvn -U archetype:generate -Dfilter="io.jenkins.archetypes:"`

This will open an interactive project creator. I'm going to be honest, I don't know what's going on in there.
The tutorial, however, suggested I select option 4, then 5, then name it demo, then leave version blank, then
confirm the changes. So that's what I did. this will create a new project in a new folder. Go into that folder
and run `mvn verify`. assuming that completes correctly, run `mvn hpi:run`, which will boot the server. you can
now access it through localhost:8080!

In the Jenkins dashboard, create a new project, give it a name, call it a freestyle project, then add a hello
world action and saev the changes. Running a build should work, then look at the CLI output through jenkins and
verify that it did in fact say hello world!

**Relevant tutorial: https://www.jenkins.io/doc/developer/tutorial**
