import os
import json

# for the remote computer
#os.chdir('C:\\Users\\User\\Desktop\\TestRepo')

# For the local computer
os.chdir('C:\\Users\\elkel\\Desktop\\Semeru')

listOfFiles = str(os.system('git diff --name-only HEAD HEAD~1')).split()
listOfFilesALPHA = []

for i in listOfFiles:
    if i.isalpha():
        print(i)
        listOfFilesALPHA.append(i)


json.dumps(listOfFilesALPHA)