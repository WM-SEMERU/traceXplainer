import os
import sqlite3

import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
import plotly
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation, ManifoldEntropy
from ds4se.mining.ir import VectorizationType, SimilarityMetric, EntropyMetric

from app import app
from tminer_source import experiment_to_df

# main is the first page users see. They it will do all of the calculations for initial dataframes to pass onto the
# other pages.

def generate_layout():
    db_file = "../test.db"

    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except sqlite3.Error as e:
        print(e)

    cur = conn.cursor()
    sys_q = "select sys_name from system;"
    cur.execute(sys_q,)
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
              Input('store-button', 'n_clicks'),
              State("system_type", 'value'))
def on_click(n_clicks, sys_t,):
    # calculate the df created by w2v and doc to vec
    db_file = "../test.db"

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
        vectors[vectorization[0]+"-"+vectorization[1]] = vectorization[2]
    vect_type = [tup[0] for tup in vect_type]
    link_type = [tup[0] for tup in link_type]
    vec_data = {"vec_type": vect_type, "link_type": link_type}
    print(vectors)
    print(vec_data)

    w2v_path = vectors["word2vec"+"-"+vec_data["link_type"][0]]
    d2v_path = vectors["doc2vec"+"-"+vec_data["link_type"][0]]

    d2v_df = experiment_to_df(d2v_path)
    w2v_df = experiment_to_df(w2v_path)
    params = {
        "system": sys_t,
        "experiment_path_w2v": w2v_path,
        "experiment_path_d2v": d2v_path,
        'system_long': sys_t,
        "corpus": corpus[1]
    }

    data = {"sys_type": sys_t, "w2v": [w2v_path, w2v_df.to_dict()], "d2v": [d2v_path, d2v_df.to_dict()],
            "params": params, "vec_data": vec_data, "vectors": vectors}

    # print(sys)
    return data


