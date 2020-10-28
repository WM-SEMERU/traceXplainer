# database_retrieve.py
#
# This file has utility functions for retrieving artifacts from a mongodb database

import pymongo

'''
Retrieves a list of all the artifacts from a collection. This 
excludes the metrics entry.

parameters:
database -- a reference to an active pymongo Database
timestamp_key -- a string timestamp name of the collection

returns a list of artifacts (as dictionaries) from the collection
'''
def get_artifacts(database, timestamp_key):
    
    collection = database[timestamp_key]
    arts = []

    # iterate through artifacts, ignoring the metrics document,
    # which has the unique key "num_doc"
    for artifact in collection.find({"num_doc":{"$exists":False}}):
        arts.append(artifact)

    return arts

'''
Retrieves the metrics document for a given collection.

parameters:
database -- a reference to an active pymongo Database
timestamp_key -- a string timestamp name of the collection

returns a dictionary holding the metrics information
'''
def get_metrics(database, timestamp_key):
    collection = database[timestamp_key]

    # "num_doc" is a key found in the metrics document but not
    # in the artifacts documents
    return collection.find_one({"num_doc":{"$exists":True}})