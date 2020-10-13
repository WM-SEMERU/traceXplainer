# database_insert.py
# 
# This file has utility functions for inserting into a mongodb database

import pymongo 

def insert_record_into_collection(db_collection, name, artifact_type, content, links, security):
    """Insert an artifact record into a mongodb collection

    This method assumes an active connection to a mongodb server

    parameters:
    db_collection -- a reference to a pymongo Collection
    name -- artifact name, usually a path and filename
    artifact_type -- either "src" or "req"
    contents -- the contents of the file as a string
    links -- traceability links of the artifact as integers
    security -- boolean representing security-relatedness of the artifact

    returns an InsertOneResult object
    """
    record = {}
    record["name"] = name
    record["type"] = artifact_type
    record["content"] = content
    record["links"] = links
    record["security"] = security
    
    result = db_collection.insert_one(record)

    return result
