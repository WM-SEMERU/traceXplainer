# insert_modified_files.py
#
# Script to run a diff on a given repo and then insert the filenames
# into a local MongoDB server. This will divide them according to
# tags located in the files into requirement files, source files,
# and files not tagged as such.
#
# Requires a command-line argument which is the path to the repository
# in question.

import pymongo
import os
import subprocess
import sys

# connect to database in the standard way
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["TM-filenames"]

print(myclient.list_database_names())


# get repository name from command line
gitRepo = sys.argv[1]
os.chdir(gitRepo)

#Get a list of what files changed between the current commit and the last.
diff = subprocess.run(['git', 'diff', '--name-only', 'HEAD', 'HEAD~1'], stdout=subprocess.PIPE)
diff_list = diff.stdout.decode('utf-8').split()

# get the id of the last commit from the output of a git log call
commit = subprocess.run(['git', 'log', '-1'], stdout=subprocess.PIPE)
commit_name = commit.stdout.decode('utf-8').split()[1]

# collection to insert into
col = db[commit_name]

#initialize an empty dictionary to be filled by the upcoming loop.
fileDict = {}

for i in diff_list:
    #Initialize filetype so that if users don't label theire files we can find out down the road.
    filetype = 'NOTGIVEN'

    file = open(os.path.join(gitRepo, i), 'r')
    
    lines = file.readlines()
    for line in lines:
        #Requirement file
        if 'TMreq' in line:
            filetype = 'TMreq'
            break
        #Source code file
        elif 'TMsrc' in line:
            filetype = 'TMsrc'
            break

    #Add the filename into the dictionary with the key corresponding to its type
    if fileDict[filetype] is None:
      fileDict[filetype] = [i]
    else:
      fileDict[filetype].append(i)

print(fileDict)

#Put the dictionary into the database.
x = col.insert_one(fileDict)
print(x.inserted_id)
