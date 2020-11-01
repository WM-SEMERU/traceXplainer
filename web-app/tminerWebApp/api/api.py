from flask import Flask
from pymongo import MongoClient
from flask_cors import CORS, cross_origin
from database_retrieve import get_artifacts

client = MongoClient("mongodb://localhost:27017/")

# retrieve user input of repo name
    # only existing repos in DB would be displayed as options
# search for repoName_version.txt 
    # created/updated when database is updated
# read in contents
# database = client["repo name selected by user"]
# collection = database["content in repoName_version.txt"]
# some loop to go through contents and display on screen?


dbNameFile = open('/home/semeru/Neural-Unsupervised-Software-Traceability/web-app/tminerWebApp/api/repoName_version.txt', 'r')
database = dbNameFile.readline().rstrip()
collection = dbNameFile.readline().rstrip()

mydb = client[database]
mycol = mydb[collection]


app = Flask(__name__)
CORS(app)


@app.route('/tminer/api/getdb')
def get_db_item():
    x = get_artifacts(mydb, collection)
    content = mycol.find({})
    for document in content:
        print(document["name"])
    retStr = ""
    for i in range(5):
        retStr += x[i]["name"] + " " + str(x[i]["type"][1])
    return retStr

#Return the contents of the files directly from the database
#The way we deal with files is specific to the libest repo. 
#In the future, there should be an option to change the path
#To src files or something, as this is not reliable often.
@app.route('/tminer/api/getdb/<type>/<id>')
def get_db_item_content(type, id):
    print("\n " + id + " " + type + "\n")
    if type == 'src':
        type = 'src/est'
    content = mycol.find_one({"name":"./"+type+"/"+id})["content"]
    return str(content)

app.run(port=5000)


def get_artifacts(database, timestamp_key):

    collection = database[timestamp_key]
    arts = []

    # iterate through artifacts, ignoring the metrics document,
    # which has the unique key "num_doc"
    for artifact in collection.find({"num_doc":{"$exists":False}}):
        arts.append(artifact)

    return arts

