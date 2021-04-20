import math

import pandas as pd
import plotly.express as px
from statsmodels.tsa.stattools import acf
import plotly.graph_objects as go
import numpy as np


# I will be writing functions here to import into the app's pages


# def experiment_to_df(path):
#     _, vec_type, link_type, unknown_bool, _ = path.split("-")
#     f = open(path)
#     column_names = f.readline().strip().split(" ")
#     l = []
#     if link_type == "LinkType.req2tc":
#         for line in f.readlines():
#             line = line.rstrip().split(" ")[1:]
#             line[0] = line[0].split("requirements/")[1]
#             line[1] = line[1].split("test/")[1]
#             line = line[:2] + list(map(float, line[2:]))
#             l.append(line)
#     df = pd.DataFrame(l, columns=column_names)
#     return df


def graph_lag(df):
    df = pd.concat([df, df.shift(-1)], axis=1).dropna()
    df.columns = ["y(t)", "y(t)+1"]
    fig = px.scatter(df, x="y(t)", y="y(t)+1")
    return fig


def graph_autocorrelation(df):
    df_acf = acf(df, nlags=len(df))
    fig = go.Figure()
    fig.add_trace(go.Scatter(
        x=np.arange(len(df_acf)),
        y=df_acf,
        name='ACF',
    ))
    fig.update_xaxes(rangeslider_visible=True)
    fig.update_layout(
        title="Autocorrelation",
        xaxis_title="Lag",
        yaxis_title="Autocorrelation",
        autosize=True,
        #     width=500,
    )
    fig.add_hline(y=1.96 / math.sqrt(len(df)))
    fig.add_hline(y=2.5758 / math.sqrt(len(df)), line_dash="dash")
    fig.add_hline(y=-1.96 / math.sqrt(len(df)))
    fig.add_hline(y=-2.5758 / math.sqrt(len(df)), line_dash="dash")
    return fig


def filename_to_id(sys, file):

    return list(sys[sys["filenames"] == file]["ids"])[0]


def id_to_filename(sys, id):
    return list(sys[sys["ids"] == id]["filenames"])[0]
