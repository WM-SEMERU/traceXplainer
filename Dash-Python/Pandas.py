import random
f = open("DummyData")
f2 =  open("DummyData2.txt", "w")
for line in f.readlines():
    f2.write(line.rstrip()+" "+str(random.randint(0,1))+"\n")