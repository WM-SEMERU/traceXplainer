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
import ds4se.facade as facade

#taken from the DS4SE documentation
TRACE_TECHNIQUES = ["VSM", "LDA", "orthogonal", "LSA", "JS", "word2vec", "doc2vec"]

'''
Computes the traceability values across all techniques for a given source and target file.

Parameters: the raw contents of a source file and those of a target file

Return: a tuple containing the name of the target file and a list of all the traceability values
'''
def caclulate_traceability_value(source_contents_raw, target_file):
    #get target file contents, if possible
    with open(gitRepo + target_file, "r") as target_contents:
        try:
            target_contents_raw = target_contents.read()
        except UnicodeDecodeError as e:
            print("could not decode target file:", target_file)

    trace_val_list = []
    for technique in TRACE_TECHNIQUES:
        trace_val = facade.TraceLinkValue(source_contents_raw, target_contents_raw, technique)
        #adds trace values as a tuple in the form "(technique name, value)"
        trace_val_list.append((technique, trace_val))
    return (target_file, trace_val_list)
'''
Creates a record for a given file in the given collection in the db.

Parameters: the name of the record, the name of the git repo (filepath), the name of the collection

Return: the result from inserting the record into the db 
'''
def create_records(filename, gitRepo, collection):
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

    #build the list of traceability values between this file (source) and all other files (targets)
    trace_target_list = []
    for target_file in all_files:
        if filename != target_file:
            trace_target_list.append(caclulate_traceability_value(artifact_content, target_file))

    #inserts record for the current file into the collection, stored under the timestamp
    result = insert_record_into_collection(
        collection,
        name=filename,
        artifact_type=artifact_type,
        content=artifact_content,
        links=trace_target_list,
        security=0)
    return result


if __name__ == "__main__":
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


    # create records for each file in the repo and insert them into the collection
    for filename in all_files:
        result = create_records(filename, gitRepo, collection)
