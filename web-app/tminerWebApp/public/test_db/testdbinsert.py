import pymongo

myclient = pymongo.MongoClient("mongodb://localhost:27017/")

mydb = myclient["thisisatestDB"]

print(myclient.list_database_names())

mycol = mydb["thisisatestCol"]

mydict = { "name": "hello", "message": "hello there" }

x = mycol.insert_one(mydict) #this returns an InsertOneResult object which has the id of the inserted object!

print(x.inserted_id)