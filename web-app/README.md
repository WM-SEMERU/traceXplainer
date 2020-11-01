# A Traceability Tool (T-Miner) Based on Unsupervised Learners
Project Leads: @danaderp, Carlos
 
Description: Traceability is a fundamental component of modern software development processes that helps to ensure properly functioning, secure software systems. 
Agile workflows tend to emphasize rapid iteration and working prototypes over rigorous documentation, leaving little time to perform intellectually intensive traceability tasks. 
Furthermore, the highly iterative nature of agile development leads to the generation of enormous sets of interconnected artifacts that capture disparate pieces of information about the underlying system. 
Generally, past work on automating the traceability process has involved drawing relationships between development artifacts of interest, such as requirements and code, using some form of textual similarity measure. 
SEMERU lab has been developing automated approaches based on Natural Language Processing that suggest a candidate set of trace links. 
The main goal of this project is to extend the functionality of a Traceability Tool in order to integrate new components based on unsupervised learning. 
You are required to implement and refactor components in the front- and back-end. 
The team is going to be divided into 3 domains: 
- DataBase Integration, 
- Front-End Development, and 
- Back-End Development. 

## Project Description for CSCI 435/535

### Project Goals:

- [ ] Complete the navigation of the web-app 
- [ ] Consume and adapt services from the  library DS4SE. This task will need a fluent communication with the Team of Project#2, which is in charge of the Data Science interfaces, and 
- [ ] Consume and adapt services from the library SecureReqNet. This task will need a fluent communication with the Team of Project#3, which is in charge of the security interfaces


### Project Requirements:

- Required Knowledge Prerequisites: Python, JavaScript, and Git
- Preferred Knowledge Prerequisites: Machine Learning, Statistical Computing

### Recommended Readings:

- An introductory Video for Traceability [link](https://www.youtube.com/watch?v=guSAnWP9zDI&feature=youtu.be)
- A probabilistic approach to Traceability [paper](https://arxiv.org/pdf/2005.09046.pdf)
- The page of the project [link](https://semeru-code-public.gitlab.io/Project-Websites/comet-website/)

# T-Miner Documentation (Team #1)
Project Contributors: Jade Chen, Alex Fantine, John Garst, Chase Jones, Ben Krupka, and Nicholas Write

"We expect that the team documents the architecture, methodology, deployment, components, and navigation of the tool in a markdown file."

## Overview
???

## Diagrams
Processes: 
![Processes Diagram](docs/Processes%20Diagram.png) 

This diagram can also be found [here](https://github.com/WM-SEMERU/Neural-Unsupervised-Software-Traceability/blob/master/web-app/docs/Processes%20Diagram.png)

Components: 
![Components Diagram](docs/Components%20Diagram.png)

This diagram can also be found [here](https://github.com/WM-SEMERU/Neural-Unsupervised-Software-Traceability/blob/master/web-app/docs/Component%20Diagram.png)

*Note: all components are hosted on the Tower1 machine*

## Jenkins
Jenkins notifies the web-application of when a developer commits and pushes changes to the repository. This notification is what triggers an update of the database with the newly updated repository, which will in turn, update the content displayed on the web-application.

Followed Jenkins' official installation [guidelines](https://www.jenkins.io/doc/book/installing/). Setup of Jenkins required installation of Maven and Java JDK 8. The setup of Jenkins required installations of Maven and Java JDK 8. Note that Java JDK 8 must be used.

For more details regarding setup, view `Jenkins Setup.txt` [here](https://github.com/WM-SEMERU/Neural-Unsupervised-Software-Traceability/blob/master/web-app/docs/Jenkins%20Setup.txt)

## MongoDB
MongoDB was chosen for the document-like storage of data. Every artifact in the repo would need to be stored along with analysis results, such as traceability values, whether the artifact is security-related, etc. In this sense, having a dictionary of information per artifact was the most comprehensive structure for the team. MongoDB’s structure of databases and collections also allows for an organization of repository versions as collections and the storage of multiple repositories as different databases.

For more details regarding installation and Mongo Shell commands, view `MongoDB Setup.txt` [here](https://github.com/WM-SEMERU/Neural-Unsupervised-Software-Traceability/blob/dev-branch/web-app/docs/MongoDB%20Setup.txt)

## Database Structure

## Web-App Navigation
