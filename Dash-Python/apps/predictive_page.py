import dash_core_components as dcc
import dash_html_components as html
import dash_table
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate

from app import app

import matplotlib.pyplot as plt
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation, ManifoldEntropy
from ds4se.mining.ir import VectorizationType, SimilarityMetric

import plotly as pl

graphs = {}


def GenerateLayout(data):
    params = data["params"]
    supervisedEval = SupervisedVectorEvaluation(params=params)  # <---- Parameter
    manifoldEntropy = ManifoldEntropy(params=params)
    # Generate the plots to display
    test = supervisedEval.Compute_avg_precision(VectorizationType.word2vec)
    test2 = manifoldEntropy.minimum_shared_entropy(dist=SimilarityMetric.WMD_sim)


    graphs["avg_precision_w2v"] = supervisedEval.Compute_avg_precision(VectorizationType.word2vec)


    graphs["avg_precision_d2v"] = supervisedEval.Compute_avg_precision(VectorizationType.doc2vec)
    # Compute_precision_recall_gain generates a list of plots. I'm not certain if it will only be two
    # graphs["prec_recall_w2v"] = supervisedEval.Compute_precision_recall_gain(VectorizationType.word2vec)[0]
    # graphs["prec_recall_d2v"] = supervisedEval.Compute_precision_recall_gain(VectorizationType.doc2vec)[0]

    layout = html.Div([
        dcc.Graph(
            id= 'super_eval_graph2',
            figure= pl.tools.mpl_to_plotly(test),
            style= {"marginTop": "50px"}
        ),
        dcc.Graph(
            id='super_eval_graph1',
            figure=pl.tools.mpl_to_plotly(test2),
            style={"marginTop": "50px"}
        ),
    ])
    return layout
