import os
import sqlite3
import sys
import re


def vec_insert(vec_folder, s_id, root_path, cursor):
    query = "insert into vec (sys_id, vec_type, link_type, path) values (?, ?, ?, ?);"
    # query = query.replace("?", "{}", 4)

    for r, d, f in os.walk(os.path.join(root_path, vec_folder)):
        for file in f:
            vec_type = re.search(r'(?<=-VectorizationType.)\w+', file).group(0)
            link_type = re.search(r'(?<=-LinkType.)\w+', file).group(0)
            vec_path = os.path.join(root_path, vec_folder, file)
            print(query.format(s_id, vec_type, link_type, vec_path))
            cursor.execute(query, (s_id, vec_type, link_type, vec_path,))


def sys_insert(sys_name, root_path, cursor):
    query = "insert into system (sys_name, corpus) values (?, ?);"
    # query = query.replace("?", "{}", 2)
    for r, d, f in os.walk(os.path.join(root_path, "corpus")):
        for file in f:
            print(query.format(sys_name, os.path.join(root_path, file)))
            cursor.execute(query, (sys_name, os.path.join(root_path, "corpus", file),))
    return cursor.lastrowid


if __name__ == "__main__":
    # This program is expected to be run in the format
    args = sys.argv[:]
    index = args.index("databaseInsertion.py") + 1
    name = args[index]
    path = args[index + 1]
    print(name)
    print(path)

    db_file = "T-Miner.db"

    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except sqlite3.Error as e:
        print(e)

    conn.isolation_level = None
    cur = conn.cursor()
    try:
        cur.execute('BEGIN TRANSACTION')
        sys_id = sys_insert(name, path, cur)
        for root, dirs, files in os.walk(path):
            for folder in dirs:
                if folder == "vectorization":
                    vec_insert(folder, sys_id, root, conn)

        cur.execute('COMMIT')
        print("Success")
    except conn.Error as e:
        print(e)
        print("Insertion Failed")
        cur.execute("rollback")
