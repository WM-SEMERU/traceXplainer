from flask import Flask
from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")

# retrieve user input of repo name
    # only existing repos in DB would be displayed as options
# search for repoName_version.txt 
    # created/updated when database is updated
# read in contents
# database = client["repo name selected by user"]
# collection = database["content in repoName_version.txt"]
# some loop to go through contents and display on screen?

mydb = client["test"]
mycol = mydb["inventory"]

x = mycol.find_one({},{ "_id": 0, "item": 1, "qty": 1 })
print(x)

app = Flask(__name__)

@app.route('/api/getdb')
def get_db_item():
    return {'item': x}

app.run(port=5000)
