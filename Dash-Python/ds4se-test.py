import os
import sqlite3

import ds4se
import plotly
from ds4se.ds.description.eval.traceability import VectorEvaluation, ExploratoryDataSoftwareAnalysis
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation
from ds4se.mining.ir import VectorizationType, EntropyMetric
from pandas.plotting import scatter_matrix, lag_plot
import pandas as pd
# experiment = 'experiments0.0.x/'
# path_data = '../ds4se/dvc-ds4se/metrics/traceability/' + experiment
import matplotlib.pyplot as plt

def libest_params():
    return {
        "system": 'libest',
        # "experiment_path_w2v": path_data + '[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv',
        # "experiment_path_d2v": path_data + '[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv',
        "experiment_path_w2v": "artifacts/[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv",
        "experiment_path_d2v": "artifacts/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv",
        # 'saving_path': '../dvc-ds4se/se-benchmarking/traceability/testbeds/processed/',
        'saving_path': 'testbeds/processed/',
        'system_long': 'libest',
        'timestamp': 1596063103.098236,
        'language': 'all-corpus'
    }



# params = libest_params()
# supevisedEval = SupervisedVectorEvaluation(params = params) #<---- Parameter
# f = supevisedEval.Compute_avg_precision(VectorizationType.word2vec)
# f = plotly.tools.mpl_to_plotly(f)

params = {"system": "libest",
              "experiment_path_w2v": "libest/vectorization/[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv",
              "experiment_path_d2v": "libest/vectorization/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv",
              "corpus": "libest/corpus/[libest-all-corpus-1596063103.098236].csv"
              }
EDA = ExploratoryDataSoftwareAnalysis(params=params)
print(EDA.similarity_set.describe())
