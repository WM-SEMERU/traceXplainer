# insert_modified_files.py
#
# Script to enter a repo into the t-miner database as a baseline. Since
# we are not worrying about commits with the baseline, this uses the repo
# as it is, with the timestamp of running this script as the collection key.
# This creates a collection, visits each file within the repo and creates
# and inserts a record for each file.
#
# Requires a command-line argument which is the path to the repository
# in question.

import pymongo
import os
import subprocess
import sys
import datetime

from database_insert import insert_record_into_collection

# get repository name and db name from command line
if len(sys.argv) != 3:
  sys.exit("usage: python create_baseline.py [repo_name] [database_name]")
gitRepo = sys.argv[1]
db_name = sys.argv[2]
os.chdir(gitRepo)

# get a timestamp to use as a collection name. This will be different than the commit timestamp that will be used
timestamp = datetime.datetime.now()
timestamp = timestamp.strftime("%Y-%m-%d %H:%M:%S")
print(timestamp)

# connect to database in the standard way
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client[db_name]
collection = db[timestamp] # the database collection whose key is the current time

# gather all the files
all_files = []
for dirpath, directories, filenames in os.walk("."):
  if dirpath[0:3] != "./.": # ignore hidden directories
    for filename in filenames:
      all_files.append(os.path.join(dirpath, filename[:])) # ignore the "./" in the filenames


# create records and insert them into the collection

for filename in all_files:
  # make all .txt files requirements and everything else source
  artifact_type = "src"
  if filename[-4:] == ".txt":
    artifact_type = "req"
 
  # read contents of file as a string

  artifact_content = ""
  with open(gitRepo + filename, "r") as artifact:
    try:
      artifact_content = artifact.read()
    except UnicodeDecodeError as e:
      print("could not decode file:", filename)

  
  # insert into the database
  result = insert_record_into_collection(
        collection, 
        name=filename, 
        artifact_type=artifact_type,
        content=artifact_content, 
        links=0, 
        security=0)

