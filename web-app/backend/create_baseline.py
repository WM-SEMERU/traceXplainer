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
import requests

from database_insert import insert_record_into_collection, insert_metrics_into_collection
import ds4se.facade as facade
import pandas as pd

#taken from the DS4SE documentation
TRACE_TECHNIQUES = ["VSM", "LDA", "orthogonal", "LSA", "JS", "word2vec", "doc2vec"]

'''
Computes the traceability values across all techniques for a given source and target file.

Parameters: the raw contents of a source file and those of a target file

Return: a tuple containing the name of the target file and a list of all the traceability values or None
        if the file could not be decoded
'''
def calculate_traceability_value(source_contents_raw, target_file):
    #get target file contents, if possible
    with open(gitRepo + target_file.replace('./', '/'), "r") as target_contents:
        try:
            target_contents_raw = target_contents.read()
        except UnicodeDecodeError as e:
            return None
            
    trace_val_list = []
    for technique in TRACE_TECHNIQUES:
        trace_val = facade.TraceLinkValue(source_contents_raw, target_contents_raw, technique)
        #adds trace values as a tuple in the form "(technique name, value)"
        trace_val_list.append((technique, trace_val))
    return (target_file, trace_val_list)

'''
Creates a POST request to SecureReqNet and retrieves whether the file is security related.

Parameters: the raw contents of the file being checked.

Return: a boolean representing whether the file was classified as security related.
'''
def get_security(file_contents):
    #url to SecureReqNet projecy
    url = "http://rocco.cs.wm.edu:8080/securereqnet/models/alpha"

    #contains the text input for the model (the contents of req files)
    data = {'instances': [file_contents]}
    headers = {
      'Content-Type': 'application/json'
    }

    response = requests.post(url, json=data, headers=headers)

    is_security = response.json()['predictions']
    #their predictions returns a list (with a single boolean) so get the boolean
    return is_security[0]

'''
Creates a record for a given file in the given collection in the db.

Parameters: the name of the record, the name of the git repo (filepath), the name of the collection
a list of all the requirement files and a list of all the source code files

Return: the result from inserting the record into the db or None if the file could not be decoded
'''
def create_records(filename, gitRepo, collection, req_list, src_list):
    # make all .txt files requirements and everything else source
    artifact_type = "src"
    if filename[-4:] == ".txt":
        artifact_type = "req"

    # read contents of file as a string
    artifact_content = ""
    with open(gitRepo + filename.replace('./', '/'), "r") as artifact:
        try:
            artifact_content = artifact.read()
        except UnicodeDecodeError as e:
            return None
            

    #add files to their corresponding lists
    #as of now, we are only working with requirement files and source code files; are there more file types?
    if artifact_type == "src":
        src_list.append({'contents':artifact_content})
    else:
        req_list.append({'contents':artifact_content})

    #retrieve security info from SecureReqNet
    if artifact_type == "req":
        is_security = get_security(artifact_content)
    else:
        is_security = "Not a requirements file."

    #build the list of traceability values between this file (source) and all other files (targets)
    trace_target_list = []
    orphan = 1 # assume the file is an orphan (has no links)
    for target_file in all_files:
        if filename != target_file:
            trace_value = calculate_traceability_value(artifact_content, target_file)
            
            # if the link is nonzero, then we know this artifact is not an orphan
            if trace_value and len([link for link in trace_value[1] if link[1] > 0]) > 0:
                orphan = 0
            
            trace_target_list.append(trace_value)

    #inserts record for the current file into the collection, stored under the timestamp
    result = insert_record_into_collection(
        collection,
        name=filename,
        artifact_type=artifact_type,
        content=artifact_content,
        links=trace_target_list,
        security=is_security,
        orphan=orphan)


    return result

'''
Creates a record of all the relevant metrics for the repo.

Params: a Pandas DataFrame containing the files designated as source, and another
with the files designated as target.
Note: source_df should be the requirement files, target_df should be the source code files

Return: the result from inserting the record into the db
'''
def compute_metrics(db_collection, source_df, target_df):
    num_doc_data = facade.NumDoc(source_df, target_df)
    vocab_size_data = facade.VocabSize(source_df, target_df)
    avg_tokens_data = facade.AverageToken(source_df, target_df)
    rec_vocab_data = facade.Vocab(source_df)
    src_vocab_data = facade.Vocab(target_df)
    shared_vocab_data = facade.VocabShared(source_df, target_df)

    result = insert_metrics_into_collection(
        db_collection,
        num_doc= num_doc_data,
        vocab_size = vocab_size_data,
        avg_tokens = avg_tokens_data,
        rec_vocab = rec_vocab_data,
        src_vocab = src_vocab_data,
        shared_vocab = shared_vocab_data)
    return result


if __name__ == "__main__":
    # get repository name and db name from command line
    if len(sys.argv) != 3:
        sys.exit("usage: python create_baseline.py [repo_name] [database_name]")
    script_location = os.path.dirname(sys.argv[0]) # for Jenkins, this is an absolute path
    gitRepo = sys.argv[1]
    db_name = sys.argv[2]
    os.chdir(gitRepo)

    # get a timestamp to use as a collection name
    timestamp = datetime.datetime.now()
    timestamp = timestamp.strftime("%Y-%m-%d- %H:%M:%S")
    print(timestamp)

    # connect to database in the standard way
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    db = client[db_name]
    collection = db[timestamp] # the database collection whose key is the current time

    # gather all the files
    all_files = []
    filetypes_to_ignore = [".png", ".pack", ".jar", ".idx", ".md", ".jpg", ""]
    for dirpath, directories, filenames in os.walk("."):
        if dirpath[0:3] != "./.": # ignore hidden directories
            for filename in filenames:
                extension = os.path.splitext(filename)[1]
                if extension not in filetypes_to_ignore:
                    all_files.append(os.path.join(dirpath, filename[:])) # ignore the "./" in the filenames

    req_list = []
    src_list = []
    # create records for each file in the repo and insert them into the collection
    for filename in all_files:
        result = create_records(filename, gitRepo, collection, req_list, src_list)

    #create Pandas DataFrames of the requirement files/source code files
    req_df = pd.DataFrame(req_list)
    src_df = pd.DataFrame(src_list)

    #compute metrics with requirement files as source and source code files as the target
    compute_metrics(collection, req_df, src_df)


    # Write the database name and the most recent commit timestamp to a file
    path = os.path.join(script_location, "../tminerWebApp/api")
    os.chdir(path)
    with open("repoName_version.txt", "w") as f:
        f.writelines([db_name + "\n", timestamp + "\n"])

