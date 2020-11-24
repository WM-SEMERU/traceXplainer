import pymongo

client = pymongo.MongoClient("mongodb://localhost:27017/")

db = client["thisisatestDB"]
col = db["thisisatestCol"]

doc = col.find_one({"TMreq" : "testreq.txt"})

import pprint
pprint.pprint(doc)
