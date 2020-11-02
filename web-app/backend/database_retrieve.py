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


'''
Retrieves a list of all the requirements artifacts from a collection. 

parameters:
database -- a reference to an active pymongo Database
timestamp_key -- a string timestamp name of the collection

returns a list of requirements artifacts (as dictionaries) from the collection
'''
def get_requirement_artifacts(database, timestamp_key):
    
    collection = database[timestamp_key]
    reqs = []

    # iterate through artifacts, ignoring the metrics document,
    # which has the unique key "num_doc"
    for artifact in collection.find({"type":"req"}):
        reqs.append(artifact)

    return reqs


'''
Retrieves a list of all the source artifacts from a collection. 

parameters:
database -- a reference to an active pymongo Database
timestamp_key -- a string timestamp name of the collection

returns a list of source artifacts (as dictionaries) from the collection
'''
def get_source_artifacts(database, timestamp_key):
    
    collection = database[timestamp_key]
    srcs = []

    # iterate through artifacts, ignoring the metrics document,
    # which has the unique key "num_doc"
    for artifact in collection.find({"type":"src"}):
        srcs.append(artifact)

    return srcs

# SEARCH BAR

'''
Retrieves a list of artifacts that match the searched string.

parameters:
database -- a reference to an active database
timestamp_key -- a string timestamp name of the collection
search -- searched string

Returns a list of artifacts (as dictionaries) from the 
collection that contain the searched string 
'''
def search_artifacts(database, timestamp_key, search):
    
    collection = database[timestamp_key]
    arts = []

    # check all artifact entries
    for artifact in collection.find({"num_doc":{"$exists":False}}):
        # check all content per artifact for searched content
        for key in artifact:
            content = artifact[key]
            # convert content to string for check if not already
            if not isinstance(content, str):
                content = str(content)
            # case-insensitive check 
            if search.lower() in content.lower():
                arts.append(artifact)
                # if already one instance of search string found, do 
                # not need to search rest of artifact
                continue

    return arts

# FILTERS

'''
Retrieves a list of requirement artifacts that are 
security-related.

parameters:
database -- a reference to an active database
timestamp_key -- a string timestamp name of the collection

Returns a list of artifacts (as dictionaries) that are
security-related.
'''
def get_security_artifacts(database, timestamp_key):

    collection = database[timestamp_key]
    arts = []

    # retrieve all requirements that are security-related, 
    # ignore the metrics doc
    for artifact in collection.find({"$and": [{"num_doc": {'$exists': False}}, {"security": True}]}):
        arts.append(artifact)

    return arts

'''
Retrieves a list of requirement artifacts that are NOT 
security-related.

parameters:
database -- a reference to an active database
timestamp_key -- a string timestamp name of the collection

Returns a list of artifacts (as dictionaries) that are
NOT security-related.
'''
def get_nonsecurity_artifacts(database, timestamp_key):

    collection = database[timestamp_key]
    arts = []

    # retrieve all requirements that are not security-related, 
    # ignore the metrics doc
    for artifact in collection.find({"$and": [{"num_doc": {'$exists': False}}, {"security": False}]}):
        arts.append(artifact)

    return arts

'''
Retrieves a list of artifacts that are orphans - no positive
traceability values.

parameters:
database -- a reference to an active database
timestamp_key -- a string timestamp name of the collection

Returns a list of artifacts (as dictionaries) that are 
orphans.
'''
def get_orphan_artifacts(database, timestamp_key):

    collection = database[timestamp_key]
    arts = []

    # retrieve all requirements that do not have any
    # traceability links 
    for artifact in collection.find({"$and": [{"num_doc": {'$exists': False}}, {"orphan": True}]}):
        arts.append(artifact)

    return arts

'''
Retrieves a list of artifacts that are not orphans - 
traceability links exist.

parameters:
database -- a reference to an active database
timestamp_key -- a string timestamp name of the collection

Returns a list of artifacts (as dictionaries) that are 
not orphans.
'''
def get_nonorphan_artifacts(database, timestamp_key):

    collection = database[timestamp_key]
    arts = []

    # retrieve all requirements that have traceability
    # links 
    for artifact in collection.find({"$and": [{"num_doc": {'$exists': False}}, {"orphan": False}]}):
        arts.append(artifact)

    return arts
