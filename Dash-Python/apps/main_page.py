import sqlite3
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
from dash.dependencies import Input, Output, State
from ds4se.ds.description.eval.traceability import ExploratoryDataSoftwareAnalysis
from tminer_source import id_to_filename
from app import app


# main is the first page users see. They it will do all of the calculations for initial dataframes to pass onto the
# other pages.

def generate_layout():
    db_file = "./T-Miner.db"

    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except sqlite3.Error as e:
        print(e)

    cur = conn.cursor()
    sys_q = "select sys_name from system;"
    cur.execute(sys_q, )
    systems = cur.fetchall()
    cur.close()
    conn.close()

    systems = [sys[0] for sys in systems]
    sys_o = [{"label": sys, "value": sys} for sys in systems]

    layout = html.Div([
        html.H3('Main Page'),
        html.Br(),
        html.Table([
            html.Tr([html.Td(['System']), dcc.Dropdown(
                id='system_type',
                options=sys_o,
                value=sys_o[0]["value"]
            )]),
        ], style={"border": "1px solid black", "width": "50%"}),
        html.Br(),
        html.Button(["Load System"], id="store-button"),
    ])
    return layout


@app.callback(Output("local", 'data'),
              Output("sidebar_text", "children"),
              Output('store-button', 'n_clicks'),
              Input('store-button', 'n_clicks'),
              State("system_type", 'value'),
              State("local", "data"))
def on_click(n_clicks, sys_t, data):
    # calculate the df created by w2v and doc to vec
    print(n_clicks)
    if n_clicks == 0 or n_clicks is None:
        return data, data["sys_type"], 0
    db_file = "./T-Miner.db"

    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except sqlite3.Error as e:
        print(e)

    cur = conn.cursor()
    corpus_q = "select sys_id, corpus from system where sys_name = ?;"
    cur.execute(corpus_q, (sys_t,))
    corpus = cur.fetchone()
    vec_q = "select distinct vec_type from vec where sys_id = ?;"
    cur.execute(vec_q, (corpus[0],))
    vect_type = cur.fetchall()
    link_q = "select distinct link_type from vec where sys_id = ?;"
    cur.execute(link_q, (corpus[0],))
    link_type = cur.fetchall()
    vect_q = "select distinct vec_type, link_type, path from vec where sys_id = ?;"
    cur.execute(vect_q, (corpus[0],))
    vecs = cur.fetchall()
    # Each query returns a list of tuples with the data inside. Strip the information from the tuples
    cur.close()
    conn.close()

    vectors = {}
    for vectorization in vecs:
        d = {"path": vectorization[2]}
        vectors[vectorization[0] + "-" + vectorization[1]] = d
    vect_type = [tup[0] for tup in vect_type]
    link_type = [tup[0] for tup in link_type]
    vec_data = {"vec_type": vect_type, "link_type": link_type}

    w2v_path = vectors["word2vec" + "-" + vec_data["link_type"][0]]["path"]
    d2v_path = vectors["doc2vec" + "-" + vec_data["link_type"][0]]["path"]

    params = {
        "system": sys_t,
        "experiment_path_w2v": w2v_path,
        "experiment_path_d2v": d2v_path,
        'system_long': sys_t,
        "corpus": corpus[1]
    }
    print(params)
    EDA = ExploratoryDataSoftwareAnalysis(params=params)
    sys = EDA.df_sys
    print(sys.head())

    for key in vectors:
        df = pd.read_csv(vectors[key]["path"], index_col=0, sep=" ")
        df["Source_filename"] = pd.DataFrame(df.apply(lambda row: id_to_filename(sys, row["Source"]), axis=1))
        df["Target_filename"] = pd.DataFrame(df.apply(lambda row: id_to_filename(sys, row["Target"]), axis=1))
        vectors[key]["dict"] = df.to_dict()

    data = {"sys_type": sys_t, "params": params, "vec_data": vec_data, "vectors": vectors}

    return data, sys_t, 0
