# database_insert.py
# 
# This file has utility functions for inserting into a mongodb database

import pymongo 

def insert_record_into_collection(db_collection, name, artifact_type, content, links, security):
    """Insert an artifact record into a mongodb collection

    This method assumes an active connection to a mongodb server, and is
    used for every artifact per commit

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

def insert_analytics_record_into_collection(db_collection, num_docs, vocab_size, av_tokens):
    """Insert an analysis data record into a mongodb collection

    This method assumes an active connection to a mongodb server, and is
    only used once per commit

    parameters:
    db_collection -- a reference to a pymongo Collection
    num_docs -- number of documents in repo
    vocab_size -- vocabulary count in repo
    av_num_tokens -- average number of tokens in the repo

    returns an InsertOneResult object
    """

    record = {}
    record["name"] = "analytics_record"
    record["document_count"] = num_docs
    record["vocab_size"] = vocab_size
    record["average_tokens"] = av_tokens
    
    result = db_collection.insert_one(record)

    return result