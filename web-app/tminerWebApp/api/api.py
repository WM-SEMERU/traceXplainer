from flask import Flask
from pymongo import MongoClient
client = MongoClient("mongodb://localhost:27017/")
mydb = client["test"]
mycol = mydb["inventory"]

x = mycol.find_one({},{ "_id": 0, "item": 1, "qty": 1 })
print(x)

app = Flask(__name__)

@app.route('/api/getdb')
def get_db_item():
    return {'item': x}