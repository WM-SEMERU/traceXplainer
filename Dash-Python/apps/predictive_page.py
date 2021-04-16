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

figs = []


def generate_layout(data):
    figs.clear()
    # I would like to replace these hardcoded lists with a call to the specific class that uses them That way any
    # given class will only display entropy and similarity metrics it uses similarity_metrics = [
    # SimilarityMetric.WMD_sim,SimilarityMetric.COS_sim,SimilarityMetric.SCM_sim,SimilarityMetric.EUC_sim]
    # entropy_metrics = [EntropyMetric.MSI_X,EntropyMetric.MSI_I]

    layout = html.Div([])
    #     dcc.Tabs([
    # dcc.Tab(label='Supervised Evaluation', children=[
    #     dcc.Dropdown(
    #         id='predictive_dropdown',
    #         options=[{'label': key, 'value': key} for key in eval_graphs],
    #         value=eval_graphs[0]
    #     ),
    #     dcc.Graph(
    #         id='super_eval_graph',
    #         style={"marginTop": "50px"}
    #     ),
    # ]),
    # dcc.Tab(label='Manifold Entropy', children=[
    #     dcc.Dropdown(
    #         id='entropy_metrics_dropdown',
    #         options=[{'label': str(key), 'value': str(key)} for key in entropy_metrics],
    #         # options=[{'label': key, 'value': key} for key in test],
    #         value=str(list(entropy_metrics)[0])
    #     ),
    #     dcc.Dropdown(
    #         id='ME_similarity_metrics_dropdown',
    #         options=[{'label': str(key), 'value': str(key)} for key in similarity_metrics],
    #         value=str(similarity_metrics[0])
    #     ),
    #     dcc.Dropdown(
    #         id='test-dropdown',
    #         options=[{"label": "0","value": 0}, {"label" :"1", "value" : 1}],
    #         value=1
    #     ),
    #     dcc.Graph(
    #         id="test-graph",
    #         style={"marginTop": "50px"}
    # )
    # dcc.Graph(
    #     id='minimum_shared_entropy_graph',
    #     style={"marginTop": "50px"}
    # ),
    # dcc.Graph(
    #     id='composable_entropy_plot',
    #     style={"marginTop": "50px"}
    # )
    #         ]),
    #     ])
    # ])
    return layout


@app.callback(
    Output('test-graph', 'figure'),
    Input('test-dropdown', 'value'),
    State('local', 'data'))
def updateSuperEvalGraph(key, data):
    f = figs[key]
    f = go.Figure(data["predictive_data"]["supervised_graphs"]["Compute_avg_precision doc2vec"])
    return f

# @app.callback(
#     Output('super_eval_graph', 'figure'),
#     Input('predictive_dropdown', 'value'),
#     State('local', 'data'))
# def updateSuperEvalGraph(key, data):
#     f = supervised_eval[0].Compute_avg_precision(VectorizationType.word2vec)
#     # return pl.tools.mpl_to_plotly(f)
#     return data["graph"]

# @app.callback(
#     Output('minimum_shared_entropy_graph', 'figure'),
#     Input('ME_similarity_metrics_dropdown', 'value'),
#     State('local', 'data'))
# def updateMinimumSharedEntropyGraph(key, data):
#     for e in SimilarityMetric:
#         if str(e) == key:
#             enum = e
#     fig = pl.tools.mpl_to_plotly(ManifoldEntropy(params=data["params"]).minimum_shared_entropy(enum))
#     return fig
#
#
# @app.callback(
#     Output('composable_entropy_plot', 'figure'),
#     Input('ME_similarity_metrics_dropdown', 'value'),
#     State('local', 'data'))
# def updateComposableEntropyGraph(sim, data):
#     for e in SimilarityMetric:
#         if str(e) == sim:
#             sim_enum = e
#     fig = pl.tools.mpl_to_plotly(ManifoldEntropy(params=data["params"]).composable_entropy_plot(
#                         manifold_x = EntropyMetric.MI,
#                         manifold_y = sim_enum,
#                         dist = 'Linked?',
#                         ground = True))
#     return fig


# dcc.Graph(
#             id= 'super_eval_graph2',
#             figure= pl.tools.mpl_to_plotly(test),
#             style= {"marginTop": "50px"}
#         ),
#         dcc.Graph(
#             id='super_eval_graph1',
#             figure=pl.tools.mpl_to_plotly(test2),
#             style={"marginTop": "50px"}
#         ),
