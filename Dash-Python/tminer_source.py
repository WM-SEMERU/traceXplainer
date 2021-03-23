import pandas as pd

# This file is a testing ground for reading data from link csv's to pd dataframes
# I will be writing functions here to import into the app's pages

file_name = "artifacts/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv"


def experiment_to_df(path):
    _, vec_type, link_type, unknown_bool, _ = path.split("-")
    f = open(path)
    column_names = f.readline().strip().split(" ")
    l = []
    if link_type == "LinkType.req2tc":
        for line in f.readlines():
            line = line.rstrip().split(" ")[1:]
            line[0] = line[0].split("requirements/")[1]
            line[1] = line[1].split("test/")[1]
            line = line[:2] + list(map(float,line[2:]))
            l.append(line)
    df = pd.DataFrame(l, columns=column_names)
    return df


df = (experiment_to_df(file_name))

