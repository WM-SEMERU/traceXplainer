import dash_core_components as dcc
import dash_html_components as html
import dash_table
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate
import plotly.express as px
import plotly.graph_objects as go

from app import app

import matplotlib.pyplot as plt
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation, ManifoldEntropy
from ds4se.mining.ir import VectorizationType, SimilarityMetric, EntropyMetric

import plotly as pl


def generate_layout(data):
    layout = html.Div([
        dcc.Tabs([
            dcc.Tab(label='Supervised Evaluation', children=[
                dcc.Dropdown(
                    id='vec-dropdown',
                    options=[{'label': key, 'value': key} for key in data["vec_data"]["vec_type"]],
                    value=data["vec_data"]["vec_type"][0]
                ),
                dcc.Dropdown(
                    id='link-dropdown',
                    options=[{'label': key, 'value': key} for key in data["vec_data"]["link_type"]],
                    value=data["vec_data"]["link_type"][0]
                ),
                dcc.Dropdown(
                    id='sim-dropdown',
                ),
                dcc.Graph(
                    id='avg-prec-graph',
                ),
                dcc.Graph(
                    id='roc-graph',
                ),
                dcc.Graph(
                    id='prec-recall-gain-graph',
                ),
            ]),
            dcc.Tab(label="Manifold Entropy",children=[
                dcc.Dropdown(
                    id='mani-vec-dropdown',
                    options=[{'label': key, 'value': key} for key in data["vec_data"]["vec_type"]],
                    value=data["vec_data"]["vec_type"][0]
                ),
                dcc.Dropdown(
                    id='mani-link-dropdown',
                    options=[{'label': key, 'value': key} for key in data["vec_data"]["link_type"]],
                    value=data["vec_data"]["link_type"][0]
                ),
                dcc.Dropdown(
                    id='mani-sim-dropdown',
                ),
                dcc.Dropdown(
                    id='mani-man-dropdown',
                    options=[{'label': str(man), 'value': str(man).split(".")[1]} for man in EntropyMetric],
                    value=str(list(EntropyMetric)[0]).split(".")[1]
                ),
                dcc.Graph(
                    id='min-shared-ent-graph',
                ),
                dcc.Checklist(
                    id='Extropy',
                    options=[{'value': "Extropy", 'label': "Extropy"}],
                    value=[],
                    labelStyle={'display': 'inline-block'}
                ),
                dcc.Graph(
                    id='man-ent-graph',
                ),
            ])
        ])
    ])
    return layout


@app.callback(
    Output('avg-prec-graph', 'figure'),
    Input("vec-dropdown", "value"),
    Input("link-dropdown", "value"),
    State('local', 'data'))
def updateAvgPrecGraph(vec, link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    supervisedEval = SupervisedVectorEvaluation(params=params)
    vec_type = VectorizationType[vec]
    return supervisedEval.Compute_avg_precision_same_plot(vec_type)


@app.callback(
    Output('roc-graph', 'figure'),
    Input("vec-dropdown", "value"),
    Input("link-dropdown", "value"),
    State('local', 'data'))
def updateRocGraph(vec, link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    supervisedEval = SupervisedVectorEvaluation(params=params)
    vec_type = VectorizationType[vec]
    return supervisedEval.Compute_roc_curve(vec_type)


@app.callback(
    Output('prec-recall-gain-graph', 'figure'),
    Input("vec-dropdown", "value"),
    Input("link-dropdown", "value"),
    Input("sim-dropdown", "value"),
    State('local', 'data'))
def updatePrecRecallGainGraph(vec, link, sim, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    supervisedEval = SupervisedVectorEvaluation(params=params)
    vec_type = VectorizationType[vec]
    sim = SimilarityMetric[sim]
    return supervisedEval.Compute_precision_recall_gain(vec_type, sim)


@app.callback(
    Output("sim-dropdown", "options"),
    Output("sim-dropdown", "value"),
    Input('vec-dropdown', "value"),
    Input('link-dropdown', 'value'),
    State('local', 'data'))
def updatePrecRecallGainDropdown(vec, link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    supervisedEval = SupervisedVectorEvaluation(params=params)
    vec_type = VectorizationType[vec]
    # vecTypeVerification will set sim_list
    supervisedEval.vecTypeVerification(vec_type)
    sims = supervisedEval.sim_list

    # Strip SimilarityMetric. from the string
    options = [{'label': str(sim).split(".")[1], 'value': str(sim).split(".")[1]} for sim in sims]
    value = options[0]["value"]
    return options, value


@app.callback(
    Output('min-shared-ent-graph', 'figure'),
    Input("Extropy", "value"),
    Input("mani-link-dropdown", "value"),
    Input("mani-sim-dropdown", "value"),
    State('local', 'data'))
def update_minimum_shared_entropy_graph(extropy, link, sim, data):
    extropy = extropy == ["Extropy"]
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)
    sim = SimilarityMetric[sim]
    return manifoldEntropy.minimum_shared_entropy(dist=sim, extropy=extropy)


@app.callback(
    Output('man-ent-graph', 'figure'),
    Input("mani-link-dropdown", "value"),
    Input("mani-sim-dropdown", "value"),
    Input("mani-man-dropdown", "value"),
    State('local', 'data'))
def update_manifold_entropy_plot(link, sim, man, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)
    sim = SimilarityMetric[sim]
    man = EntropyMetric[man]
    return manifoldEntropy.manifold_entropy_plot(manifold=man, dist=sim)

@app.callback(
    Output("mani-sim-dropdown", "options"),
    Output("mani-sim-dropdown", "value"),
    Input('mani-vec-dropdown', "value"),
    Input('mani-link-dropdown', 'value'),
    State('local', 'data'))
def updateManiSimDropdown(vec, link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    supervisedEval = SupervisedVectorEvaluation(params=params)
    vec_type = VectorizationType[vec]
    # vecTypeVerification will set sim_list
    supervisedEval.vecTypeVerification(vec_type)
    sims = supervisedEval.sim_list

    # Strip SimilarityMetric. from the string
    options = [{'label': str(sim), 'value': str(sim).split(".")[1]} for sim in sims]
    value = options[0]["value"]
    return options, value


@app.callback(
    Output('comp-ent-graph', 'figure'),
    Input("mani-link-dropdown", "value"),
    Input("mani-sim-dropdown", "value"),
    Input("mani-man-dropdown", "value"),
    Input("mani-man_y-dropdown", "value"),
    State('local', 'data'))
def update_composable_entropy_plot(link, man_x, man_y, dist, ground, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)
    man_x = EntropyMetric[man_x]
    man_y = EntropyMetric[man_y]
    
    fig = manifoldEntropy.composable_entropy_plot(
                        manifold_x = EntropyMetric.MI,
                        manifold_y = SimilarityMetric.COS_sim,
                        dist = 'Linked?',
                        ground = True)
    return fig
