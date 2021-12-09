import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State
import plotly.graph_objects as go

from app import app

from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation, ManifoldEntropy
from ds4se.mining.ir import VectorizationType, SimilarityMetric, EntropyMetric, DistanceMetric


def generate_layout(data):
    metrics = [metric for metric in EntropyMetric]
    metrics.remove(EntropyMetric.MSI_X)
    metrics.remove(EntropyMetric.MSI_I)
    opts = [{'label': str(man), 'value': str(man).split(".")[1]} for man in metrics]
    layout = html.Div([
        dcc.Tabs([
            dcc.Tab(label='Supervised Evaluation', children=[
                html.Table([
                    html.Tr([
                        html.Td(["Vectorization Type"]),
                        dcc.Dropdown(
                            id='vec-dropdown',
                            options=[{'label': key, 'value': key} for key in data["vec_data"]["vec_type"]],
                            value=data["vec_data"]["vec_type"][0]
                        )
                    ]),
                    html.Tr([
                        html.Td(["Link Type"]),
                        dcc.Dropdown(
                            id='link-dropdown',
                            options=[{'label': key, 'value': key} for key in data["vec_data"]["link_type"]],
                            value=data["vec_data"]["link_type"][0]
                        ),
                    ])
                ], style={"border": "1px solid black", "width": "100%"}),
                dcc.Graph(
                    id='avg-prec-graph',
                ),
                dcc.Graph(
                    id='roc-graph',
                ),
                html.Div(children=[
                    html.Table([
                        html.Tr([
                            html.Td(["Similarity Metric"]),
                            dcc.Dropdown(
                                id='sim-dropdown',
                            )
                        ]),
                    ], style={"border": "1px solid black", "width": "100%", 'vertical-align': 'top'}),
                    dcc.Graph(
                        id='prec-recall-gain-graph',
                        style={'vertical-align': 'top', "width": "50%", }
                    )
                ], style={'vertical-align': 'top', 'display': 'inline-block'})
            ]),

            dcc.Tab(label="Manifold Entropy", children=[
                html.Table([
                    html.Tr([
                        html.Td(["Vectorization Type"]),
                        dcc.Dropdown(
                            id='mani-vec-dropdown',
                            options=[{'label': key, 'value': key} for key in data["vec_data"]["vec_type"]],
                            value=data["vec_data"]["vec_type"][0]
                        ),
                    ]),
                    html.Tr([
                        html.Td(["Link Type"]),
                        dcc.Dropdown(
                            id='mani-link-dropdown',
                            options=[{'label': key, 'value': key} for key in data["vec_data"]["link_type"]],
                            value=data["vec_data"]["link_type"][0]
                        )
                    ])
                ], style={"border": "1px solid black", "width": "100%"}),
                html.Div([
                    html.Table([
                        html.Tr([
                            html.Td(["Similarity Metric"]),
                            dcc.Dropdown(
                                id='mani-sim-dropdown',
                            ),
                        ]),
                    ], style={"width": "100%"}),
                    dcc.Checklist(
                        id='Extropy',
                        options=[{'value': "Extropy", 'label': "Extropy"}],
                        value=[],
                        labelStyle={'display': 'inline-block'}
                    ),
                    dcc.Graph(
                        id='min-shared-ent-graph',
                    ),
                ], style={"border": "1px solid black"}),
                html.Div([
                    html.Table([
                        html.Tr([
                            html.Td(["Similarity Metric"]),
                            dcc.Dropdown(
                                id='manient-sim-dropdown',
                            ),
                        ]),
                        html.Tr([
                            html.Td(["Entropy Metric"]),
                            dcc.Dropdown(
                                id='manient-man-dropdown',
                                options=opts,
                                value=str(list(EntropyMetric)[2]).split(".")[1]
                            ),
                        ])
                    ], style={"width": "100%"}),
                    dcc.Graph(
                        id='man-ent-graph',
                    ),
                ],style={"border": "1px solid black"}),
                html.Div([
                    html.Table([
                        html.Tr([
                            html.Td(["Manifold x"]),
                            dcc.Dropdown(
                                id='comp-man_x-dropdown',
                            )
                        ]),
                        html.Tr([
                            html.Td(["Manifold x"]),
                            dcc.Dropdown(
                                id='comp-man_y-dropdown',
                            ),
                        ]),
                        html.Tr([
                            html.Td(["Distance"]),
                            dcc.Dropdown(
                                id='comp-dist-dropdown',
                            )
                        ]),
                        html.Tr([
                            html.Td([dcc.Checklist(
                                id='NA',
                                options=[{'value': "NA", 'label': "NA"}],
                                value=[],
                                labelStyle={'display': 'inline-block'}
                            )]),
                        ])
                    ], style={"width": "100%"}),
                    dcc.Graph(
                        id='comp-shared-graph',
                    ),
                    html.Table([
                        html.Tr([
                            html.Td(["Number of NA's"]),
                            html.Td(id="Num_NA")
                        ])
                    ], style={"border": "1px solid black", "width": "100%"})
                ], style={"border": "1px solid black", "width": "100%"})
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
    Input("manient-sim-dropdown", "value"),
    Input("manient-man-dropdown", "value"),
    State('local', 'data'))
def update_manifold_entropy_plot(link, sim, man, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)
    # Remove Nan's
    # display Nan's if there are any.
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
    Output("manient-sim-dropdown", "options"),
    Output("manient-sim-dropdown", "value"),
    Input('mani-vec-dropdown', "value"),
    Input('mani-link-dropdown', 'value'),
    State('local', 'data'))
def updateManiEntSimDropdown(vec, link, data):
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


# @app.callback(
#     Output('comp-ent-graph', 'figure'),
#     Input("mani-link-dropdown", "value"),
#     Input("comp-dist-dropdown", "value"),
#     Input("comp-man_x-dropdown", "value"),
#     Input("comp-man_y-dropdown", "value"),
#     State('local', 'data'))
# def update_composable_entropy_plot(link, dist, man_x, man_y, data):
#     params = {"system": data["params"]["system"],
#               "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
#               "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
#               "corpus": data["params"]["corpus"]
#               }
#     manifoldEntropy = ManifoldEntropy(params=params)
#
#     man_x = string_2_metric(man_x)
#     man_y = string_2_metric(man_y)
#     dist = string_2_metric(dist)
#
#     fig = manifoldEntropy.composable_entropy_plot(
#         manifold_x=man_x,
#         manifold_y=man_y,
#         dist=dist)
#     return fig


@app.callback(
    Output('comp-shared-graph', 'figure'),
    Output('Num_NA', "children"),
    Input("mani-link-dropdown", "value"),
    Input("comp-dist-dropdown", "value"),
    Input("comp-man_x-dropdown", "value"),
    Input("comp-man_y-dropdown", "value"),
    Input("NA", "value"),
    State('local', 'data'))
def update_composable_shared_plot(link, dist, man_x, man_y, na, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)
    man_x = string_2_metric(man_x)
    man_y = string_2_metric(man_y)
    dist = string_2_metric(dist)
    # Only drop NA's if the button is clicked
    print(na)
    na = na != ["NA"]

    try:
        fig, nas = manifoldEntropy.composable_shared_plot(
            manifold_x=man_x,
            manifold_y=man_y,
            dist=dist,
            drop_na=na)
    except ValueError:
        return go.Figure(),  "No Graph"
    return fig, nas


@app.callback(
    Output("comp-dist-dropdown", "options"),
    Output("comp-man_x-dropdown", "options"),
    Output("comp-man_y-dropdown", "options"),
    Output("comp-dist-dropdown", "value"),
    Output("comp-man_x-dropdown", "value"),
    Output("comp-man_y-dropdown", "value"),
    Input('mani-link-dropdown', 'value'),
    State('local', 'data'))
def update_comp_dropdowns(link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link]["path"],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link]["path"],
              "corpus": data["params"]["corpus"]
              }
    manifoldEntropy = ManifoldEntropy(params=params)

    # df = self.df_w2v.dropna(inplace=False)
    # df = pd.concat([df, self.df_d2v.drop(columns=["Linked?"])],axis=1,join="inner")
    df = manifoldEntropy.df_w2v
    df = df.drop(columns=["Source", "Target"])
    options = [{'label': str(val), 'value': str(val)} for val in df.columns]
    return options, options, options, options[1]["value"], options[2]["value"], "Linked?"


def string_2_metric(s):
    s1 = s.split(".")
    if s1[0] == "EntropyMetric":
        return EntropyMetric[s1[1]]
    elif s1[0] == "SimilarityMetric":
        return SimilarityMetric[s1[1]]
    elif s1[0] == "DistantMetric":
        return DistanceMetric[s1[1]]
    else:
        return s
