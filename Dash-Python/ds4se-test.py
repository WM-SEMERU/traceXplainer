import math
import os
import sqlite3

import ds4se
import numpy as np
import plotly
from ds4se.ds.description.eval.traceability import VectorEvaluation, ExploratoryDataSoftwareAnalysis
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation
from ds4se.mining.ir import VectorizationType, EntropyMetric
from pandas.plotting import scatter_matrix, lag_plot
import pandas as pd
# experiment = 'experiments0.0.x/'
# path_data = '../ds4se/dvc-ds4se/metrics/traceability/' + experiment
import matplotlib.pyplot as plt
import plotly.express as px


# from tminer_source import experiment_to_df


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

# params = {"system": "ebt",
#           "experiment_path_w2v": "ebt/vectorization/[ebt-VectorizationType.word2vec-LinkType.req2src-True-1609293224.89927].csv",
#           "experiment_path_d2v": "ebt/vectorization/[ebt-VectorizationType.doc2vec-LinkType.req2src-True-1610632393.757948].csv",
#           "corpus": "ebt/corpus/[ebt-all-corpus-1609221582.171744].csv"
#           }
# EDA = ExploratoryDataSoftwareAnalysis(params=params)  # <---- Parameter


# df = experiment_to_df("libest/vectorization/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv")
# print(df)
#
# from statsmodels.tsa.stattools import acf
# import plotly.graph_objects as go
#
# df = EDA.entropy_set[['EntropyMetric.MI']]
# df_pacf = acf(df, nlags=len(df))
# fig = go.Figure()
# fig.add_trace(go.Scatter(
#     x=np.arange(len(df_pacf)),
#     y=df_pacf,
#     name='ACF',
# ))
# fig.update_xaxes(rangeslider_visible=True)
# fig.update_layout(
#     title="Autocorrelation",
#     xaxis_title="Lag",
#     yaxis_title="Autocorrelation",
#     autosize=True,
#     #     width=500,
# )
# fig.add_hline(y=1.96/math.sqrt(len(df)))
# fig.add_hline(y=2.5758/math.sqrt(len(df)), line_dash="dash")
# fig.add_hline(y=-1.96/math.sqrt(len(df)))
# fig.add_hline(y=-2.5758/math.sqrt(len(df)), line_dash="dash")
# fig.show()
#
# pd.plotting.autocorrelation_plot(EDA.entropy_set[['EntropyMetric.MI']])
# plt.show()

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
            line = line[:2] + list(map(float, line[2:]))
            l.append(line)
    df = pd.DataFrame(l, columns=column_names)
    return df


# df = pd.read_csv("ebt/vectorization/[ebt-VectorizationType.doc2vec-LinkType.req2src-True-1610632393.757948].csv",
#                  sep=" ")
# print(df.head())
# sys = EDA.df_sys
# # df = pd.read_csv("")
# print(sys.head())
# i = 0
# 
# print(df["Source"])
# print(sys["filenames"])
# record = sys[sys["filenames"]=="RQ107.txt"]
# print(record["filenames"])
# print(record["ids"])




def filename_to_id(sys, file):

    return list(sys[sys["filenames"] == file]["ids"])[0]


def id_to_filename(sys, id):
    return list(sys[sys["ids"] == id]["filenames"])[0]

# sys = EDA.df_sys
df = pd.read_csv(
    "/home/roger/Desktop/T-Miner/Dash-Python/systems/etour/vectorization/[etour-VectorizationType.doc2vec-LinkType.uc2src-True-1610634014.42428].csv", sep=" ", index_col=0)
print(df.columns)
# print(len(sys))
# print(len(df))
# l = list(set(df["Target"]))
#
# df = pd.merge(sys, df, left_on='ids', right_on='Target')
# print(len(df))
# l2 = []
# for id in l:
#     s= id_to_filename(sys,id)
#     l2.append(s)
# l2.sort()
# print(l2)
# df2 = sys[["ids","filenames"]]
# #print(df2.to_dict())
# df["Target"] = pd.DataFrame(df.apply(lambda row: id_to_filename(sys, row["Target"]), axis=1))
# print(df["Target"])