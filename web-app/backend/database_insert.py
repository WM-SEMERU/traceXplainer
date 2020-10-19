# database_insert.py
#
# This file has utility functions for inserting into a mongodb database

import pymongo

"""
Insert an artifact record into a mongodb collection
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
def insert_record_into_collection(db_collection, name, artifact_type, content, links, security):

    record = {}
    record["name"] = name
    record["type"] = artifact_type
    record["content"] = content
    record["links"] = links
    record["security"] = security

    result = db_collection.insert_one(record)

    return result

"""
Insert repo metrics into a mongodb collection
This method assumes an active connection to a mongodb server

parameters:
db_collection -- a reference to a pymongo Collection
num_doc -- [requirement file count, src code file count, diff req -> src, diff src -> req]
vocab_size -- [requirement vocab size, src code vocab size, diff req -> src, diff src -> req]
avg_tokens -- [requirement file avg # tokens, src code avg # tokens, diff req -> src, diff src -> req]
rec_vocab -- three most common tokens in requirement files and their counts/frequencies
src_vocab -- three most common tokens in source code files and their counts/frequencies
shared_vocab -- three most common tokens across both filetypes and their counts/frequencies

returns an InsertOneResult object
"""
def insert_metrics_into_collection(db_collection, num_doc, vocab_size, avg_tokens, rec_vocab, src_vocab, shared_vocab):
    metrics = {}
    metrics["num_doc"] = num_doc
    metrics["vocab_size"] = vocab_size
    metrics["avg_tokens"] = avg_tokens
    metrics["rec_vocab"] = rec_vocab
    metrics["src_vocab"] = src_vocab
    metrics["shared_vocab"] = shared_vocab

    result = db_collection.insert_one(metrics)
    return result
