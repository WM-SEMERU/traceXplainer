# MongoDB Setup

## MongoDB Installation on Ubuntu 18.04

Execute these commands in machine's terminal:

1. Import public key used by package management system

    `wget -qO - https://www.mongodb.org/static/pgp/server-4.4.asc | sudo apt-key add -`

2. Create MongoDB list file

    `echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.4.list`

3. Reload local package DB

    `sudo apt-get update`

4. Install MongoDB packages

    `sudo apt-get install -y mongodb-org`

5. Start MongoDB (see [Linux Services](https://github.com/WM-SEMERU/Neural-Unsupervised-Software-Traceability/blob/master/web-app/docs/Linux_Services.md))

    `sudo systemctl start mongod`

6. Verify MongoDB started successfully

    `sudo systemctl status mongodb`

7. Set MongoDB to automatically start after system reboot

    `sudo systemctl enable mongod`


## MongoDB Python Driver - PyMongo Installation

1. Import latest PyMongo version using pip

    `python -m pip install pymongo`

2. *Optional: Update PyMongo*

    `python -m pip install --upgrade pymongo`


## Useful MongoDB Shell Commands

**Accessing MongoDB Shell:**

1.	Open the command line or SSH into the hosting machine

2.	Enter MongoDB Shell by running this command:
    
    `# mongo`


**Viewing and querying entries:**

1.	View all databases

    `> show dbs`

2.	Enter a database

    `> use myDBName`

3.	View all collections

    `> show collections`

4.	View all entries in the collection

    `> db.myColName.find({}).pretty()`

5.	Query entries (search by full field value)

    `> db.myColName.find({ <field1> : <value1>, … }).pretty()`

6.	Query entries (search by substring of field value)

    `> db.myColName.find({ <field1> : { $regex : <subString1> }}).pretty()`


**Count Entries in Collection:**

1.	Enter database

    `> use myDBName`

2.	Count all entries in specified collection

    `> db.myColName.count()`

3.	Count entries by condition in specified collection

    `> db.myColName.count({ <field1> : <value1>, … })`


**Deleting Entries:**

1.	Enter database

    `> use myDBName`

2.	Remove specific entry by ID from specified collection
    
    `> db.mycolName.deleteOne({ _id : ObjectId(“myIDNumber”) })`

3.	Remove entries based on condition from specified collection
    
    `> db.myColName.remove({ <field1> : <value1>, … })`

4.	Remove all entries from specified collection
    
    `> db.myColName.remove({})`
    

**Deleting Collection:**

1.	Enter database
    
    `> use myDBName`

2.	Drop specified collection
    
    `> db.myCollName.drop()`


**Deleting Database:**

1.	Enter database
    
    `> use myDBName`

2.	Drop database
    
    `> db.dropDatabase()`


**Exit MongoDB Shell**
    
    `> quit()`