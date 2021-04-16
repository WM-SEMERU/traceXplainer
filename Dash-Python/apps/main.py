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


system_dropdown = dcc.Dropdown(
    id='system_type',
    options=[{"label": "Libest", "value": "libest"}],
    value="libest"
)

w2v_text = dcc.Textarea(
    id="w2v-text",
    value='[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv',
    style={'width': '100%'},
)

d2v_text = dcc.Textarea(
    id="d2v-text",
    value='[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv',
    style={'width': '100%'},
)

time_stamp = dcc.Textarea(
    id="time-stamp",
    value="1596063103.098236",
    style={'width': '100%'},
)

saving_path = dcc.Textarea(
    id="saving-path",
    value=os.path.join("testbeds", "processed", ""),
    style={'width': '100%'},
)

layout = html.Div([
    html.H3('Main Page'),
    html.Br(),
    html.Table([
        html.Tr([html.Td(['System']), system_dropdown]),
        html.Tr([html.Td(['experiment_path_w2v']), w2v_text]),
        html.Tr([html.Td(["experiment_path_d2v"]), d2v_text]),
        html.Tr([html.Td(["saving_path"]), saving_path]),
        html.Tr([html.Td(['timestamp']), time_stamp]),
    ], style={"border": "1px solid black", "width": "50%"}),
    html.Br(),
    html.Button(["Store data"], id="store-button"),
    html.Button(["Display data"], id="display-button"),
    dcc.Textarea(value="", id="test-out")
])


# def create_predictive_data(params):
#     supervised_eval = SupervisedVectorEvaluation(params=params)
#     manifold_entropy = ManifoldEntropy(params=params)
#     supervised_graphs = {}
#     manifold_graphs = {}
#     eval_graphs = ["Compute_avg_precision", "Compute_precision_recall_gain"]
#     eval_vec_types = ["word2vec", "doc2vec"]
#
#     supervised_graphs["Compute_avg_precision doc2vec"] = \
#         plotly.tools.mpl_to_plotly(supervised_eval.Compute_avg_precision(VectorizationType.doc2vec)).to_dict()
#     # supervised_graphs[("Compute_precision_recall_gain", "doc2vec")] = \
#     #     plotly.tools.mpl_to_plotly(supervised_eval.Compute_precision_recall_gain(VectorizationType.doc2vec)).to_dict()
#     supervised_graphs["Compute_avg_precision word2vec"] = \
#         plotly.tools.mpl_to_plotly(supervised_eval.Compute_avg_precision(VectorizationType.word2vec)).to_dict()
#     # supervised_graphs[("Compute_precision_recall_gain", "word2vec")] = \
#     #     plotly.tools.mpl_to_plotly(supervised_eval.Compute_precision_recall_gain(VectorizationType.word2vec)).to_dict()
#
#     # ASK DAVID IF THESE LISTS ARE COMPLETE
#     manifold_entropy_similarity_metrics = \
#         [SimilarityMetric.WMD_sim, SimilarityMetric.SCM_sim, SimilarityMetric.COS_sim, SimilarityMetric.EUC_sim]
#     manifold_entropy_entropy_metrics = [EntropyMetric.MI, EntropyMetric.MSI_I, EntropyMetric.MSI_X]
#
#     for sim_metric in manifold_entropy_similarity_metrics:
#         manifold_graphs[str(sim_metric)] = plotly.tools.mpl_to_plotly(
#             manifold_entropy.minimum_shared_entropy(sim_metric))
#         for entropy_metric in [EntropyMetric.MSI_I, EntropyMetric.MSI_X]:
#             print(str(entropy_metric) + " " + str(sim_metric))
#             manifold_graphs[(str(entropy_metric) + str(sim_metric))] = \
#                 plotly.tools.mpl_to_plotly(manifold_entropy.composable_shared_plot(
#                     manifold_x=entropy_metric,
#                     manifold_y=sim_metric,
#                     dist='Linked?',
#                     ground=True)).to_dict()
#
#     print("done")
#     data = {  # "supervised_eval" : supervised_eval, "manifold_entropy": manifold_entropy,
#         "supervised_graphs": supervised_graphs, "supervised_graphs_keys": [eval_graphs, eval_vec_types], }
#     # "manifold_graphs" : manifold_graphs,
#     # "manifold_graphs_keys" : [map(str,manifold_entropy_entropy_metrics), manifold_entropy_similarity_metrics]}
#     return data


@app.callback(Output("local", 'data'),
              Input('store-button', 'n_clicks'),
              State("system_type", 'value'),
              State("w2v-text", 'value'),
              State("d2v-text", 'value'),
              State("time-stamp", 'value'),
              State("saving-path", 'value')
              )
def on_click(n_clicks, sys_t, w2v, d2v, time, save_path):

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
    vectors = cur.fetchall()
    # Each query returns a list of tuples with the data inside. Strip the information from the tuples
    vect_type = [tup[0] for tup in vect_type]
    link_type = [tup[0] for tup in link_type]
    cur.close()
    conn.close()
    vec_data = {"vec_type":vect_type, "link_type" : link_type}
    print(vectors)
    print("corpus path is: " + corpus[1])

    path = os.path.join("artifacts", d2v)
    d2v_df = experiment_to_df(path)
    path = os.path.join("artifacts", w2v)
    w2v_df = experiment_to_df(path)
    params = {
        "system": 'libest',
        "experiment_path_w2v": os.path.join("artifacts", w2v),
        "experiment_path_d2v": os.path.join("artifacts", d2v),
        'saving_path': save_path,
        'system_long': 'libest',
        'timestamp': time,
        'language': 'all-corpus'
    }
    # predictive_data = create_predictive_data(params)
    # Give a default data dict with 0 clicks if there's no data.
    m = ManifoldEntropy(params=params)
    # predictive_data = create_predictive_data(params)
    data = {"sys_type": sys_t, "w2v": [w2v, w2v_df.to_dict()], "d2v": [d2v, d2v_df.to_dict()], "time": time,
            "path": save_path, "params": params, "vec_data" : vec_data, "vectors":vectors} # "predictive_data": predictive_data}

    # print(sys)
    return data


@app.callback(Output("test-out", 'value'),
              Input('display-button', 'n_clicks'),
              State("local", 'data'))
def on_click2(n_clicks, data):
    if n_clicks is None:
        # prevent the None callbacks is important with the store component.
        # you don't want to update the store for nothing.
        raise PreventUpdate

    return str(data)
