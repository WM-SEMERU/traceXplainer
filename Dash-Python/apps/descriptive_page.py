import dash_core_components as dcc
import dash_table
import dash_html_components as html
import dash_bootstrap_components as dbc
from dash.dependencies import Input, Output, State
import plotly.express as px
import pandas as pd
import os

from ds4se.ds.description.eval.traceability import ExploratoryDataSoftwareAnalysis

from app import app
from tminer_source import experiment_to_df

shared_infometrics_table = dash_table.DataTable(
    id="infometric_table",
    page_current=0,
    sort_action='native',
    filter_action="native",
    page_size=10,
    style_table={"maxWidth": "50%"},
    style_cell={
        'whiteSpace': 'normal',
        'height': 'auto',
    }, )


@app.callback(
    Output('infometric_table', 'data'),
    Output('infometric_table', 'columns'),
    Input('shared_info_source', 'value'),
    Input('shared_info_target', 'value'),
    Input('vec-type-dropdown', "value"),
    Input('link-type-dropdown', 'value'),
    State('local', 'data'))
def update_infometric_table(src, tgt, vec, link, data):
    df = experiment_to_df(data["vectors"][vec + "-" + link])
    df = df[(df["Source"] == src) & (df["Target"] == tgt)]
    df = df.drop(["Source", "Target"], axis=1)
    table_data = df.to_dict("records")
    columns = [{'id': c, 'name': c} for c in list(df.columns)]
    return table_data, columns


@app.callback(
    Output('file-description-textarea', 'value'),
    Input('link-datatable', 'active_cell'),
    Input("link-datatable", "page_current"),
    Input("link-datatable", "derived_virtual_data"),
    Input("tokenization-dropdown", "value"),
    State("local", 'data'))
def update_text_from_table(active_cell, page_current, derived_virtual_data, display, data):
    sys = ExploratoryDataSoftwareAnalysis(params=data["params"]).df_sys
    if active_cell:
        col = active_cell['column_id']
        row = active_cell['row'] + 10 * page_current
        cell_data = derived_virtual_data[row][col]
        col = active_cell['column_id']
        if (col == "Linked?"):
            return str(sys[sys["filenames"] == derived_virtual_data[row]["Source"]][display].iloc[0])
        row = active_cell['row'] + 10 * page_current
        cell_data = derived_virtual_data[row][col]
        return str(sys[sys["filenames"] == cell_data][display].iloc[0])


def generate_layout(store_data):
    """Generate layout is called everytime the descriptive page is selected. It returns the layout for the page to
    display, after performing calculations to generate the necessary tables and data sources."""

    EDA = ExploratoryDataSoftwareAnalysis(params=store_data["params"])
    sys = EDA.df_sys
    file_list = list(set(sys[sys["type"] == "req"]["filenames"]))
    file_list.sort()
    df = pd.DataFrame.from_dict(store_data["d2v"][1])
    fig = px.scatter(df, x="Source", y="Target", hover_name="Target", labels={"color": "Linked"},
                     color=df["Linked?"] == 1,
                     color_discrete_sequence=["red", "blue"])
    fig.update_layout(legend_traceorder="reversed")

    df1 = df[["Source", "Target", "Linked?"]]

    layout = html.Div(children=[
        dcc.Tabs([
            dcc.Tab(label='Data Description', children=[
                dcc.Dropdown(
                    id='vec-type-dropdown',
                    options=[{'label': key, 'value': key} for key in store_data["vec_data"]["vec_type"]],
                    value=store_data["vec_data"]["vec_type"][0]
                ),
                dcc.Dropdown(
                    id='link-type-dropdown',
                    options=[{'label': key, 'value': key} for key in store_data["vec_data"]["link_type"]],
                    value=store_data["vec_data"]["link_type"][0]
                ),
                dcc.Dropdown(
                    id='metric-dropdown',
                ),
                dcc.Graph(
                    id='one-requirement-graph',
                ),
                dcc.Dropdown(
                    id='file-select-dropdown',
                    options=[{'label': key, 'value': key} for key in file_list],
                    value=list(sys[sys["type"] == "req"]["filenames"])[0]
                ),
                html.Div(children=[
                    html.P(["Shared Infometrics"]),
                    html.Div([
                        dbc.Row(
                            [
                                dbc.Col(dcc.Dropdown(
                                    id='shared_info_source',
                                    options=[{'label': key, 'value': key} for key in set(df["Source"])],
                                    value=df["Source"][0],
                                    style={"Width": "50%"}
                                )),
                                dbc.Col(dcc.Dropdown(
                                    id='shared_info_target',
                                    style={"width": "100%"}
                                ))
                            ])
                    ]),
                    shared_infometrics_table
                ], style={"maxWidth": "40%"})
            ]),
            dcc.Tab(label='Browse Links', children=[
                html.Div(children=[
                    html.Div(children=[
                        dash_table.DataTable(
                            id="link-datatable",
                            data=df1.to_dict("records"),
                            page_current=0,
                            sort_action='native',
                            columns=[{'id': c, 'name': c} for c in list(df1.columns)],
                            filter_action="native",
                            page_size=10, ),
                        dcc.Graph(
                            id='basic-sim-graph',
                            figure=fig,
                            style={"marginTop": "50px"}
                        ),
                    ], style={"width": "100%", 'display': 'inline-block', "verticalAlign": "top"}),
                    html.Div(children=[
                        dcc.Textarea(
                            id='file-description-textarea',
                            style={'width': '100%', 'height': 400},
                            contentEditable=False,
                            readOnly=False
                        )
                    ], style={'width': '100%', 'display': 'inline-block', "verticalAlign": "top"}),
                    dcc.Dropdown(
                        id="tokenization-dropdown",
                        options=[{'label': "text", 'value': "text"}, {'label': "conv", 'value': "conv"},
                                 {'label': "bpe128k", 'value': "bpe128k"}, {'label': "bpe32k", 'value': "bpe32k"},
                                 {'label': "bpe8k", 'value': "bpe8k"}],
                        value="text",
                        style={"width": "100%"}
                    )
                ], style={'columnCount': 2})
            ]),
            dcc.Tab(label='Graphs', children=[
                dcc.Graph(
                    id="metric-hist"
                ),
                html.Div([
                    dbc.Row(
                        [
                            dbc.Col(dcc.Dropdown(
                                id='sim-entropy-dropdown',
                                options=[{'label': "similarity", 'value': "similarity_set"},
                                         {'label': "entropy", 'value': "entropy_set"},
                                         {'label': "shared_set", 'value': "shared_set"}],
                                value="similarity_set",
                                style={"Width": "50%"}
                            )),
                            dbc.Col(dcc.Dropdown(
                                id='hist-metric-dropdown',
                                style={"width": "100%"}
                            ))
                        ])
                ]),
                dcc.Graph(
                    id="metric-box"
                ),
                dcc.Checklist(
                    id='group-by-linked',
                    options=[{'value': "Linked?", 'label': "Group by Linked?"},
                             {'value': "Outliers", 'label': "Outliers"}],
                    value=[],
                    labelStyle={'display': 'inline-block'}
                ),
            ])
        ])
    ], style={'width': '100%'})
    return layout


@app.callback(
    Output('one-requirement-graph', 'figure'),
    Input('file-select-dropdown', 'value'),
    Input('vec-type-dropdown', "value"),
    Input('link-type-dropdown', 'value'),
    Input('metric-dropdown', 'value'),
    State('local', 'data'))
def updateOneRequirementGraph(selected_req, vec_type, link_type, metric, store_data):
    df = experiment_to_df(store_data["vectors"][vec_type + "-" + link_type])
    filtered_df = df[df["Source"] == selected_req]
    figure = px.scatter(filtered_df, x="Target", y=metric, hover_name="Target", title=selected_req,
                        labels={"color": "Linked"},
                        color=filtered_df["Linked?"] == 1,
                        color_discrete_sequence=["red", "blue"])
    figure.update_layout(legend_traceorder="reversed")
    return figure


# Link type is still set on the first tab.
@app.callback(
    Output('metric-hist', 'figure'),
    Input("hist-metric-dropdown", "value"),
    Input('sim-entropy-dropdown', "value"),
    Input('link-type-dropdown', 'value'),
    State('local', 'data'))
def update_hist_graph(metric, set_type, link_type, store_data):
    params = {"system": store_data["params"]["system"],
              "experiment_path_w2v": store_data["vectors"]["word2vec" + "-" + link_type],
              "experiment_path_d2v": store_data["vectors"]["doc2vec" + "-" + link_type],
              "corpus": store_data["params"]["corpus"]
              }
    EDA = ExploratoryDataSoftwareAnalysis(params=params)
    df = None
    if set_type == "similarity_set":
        df = EDA.similarity_set
    elif set_type == "entropy_set":
        df = EDA.entropy_set
    elif set_type == "shared_set":
        df = EDA.shared_set
    if df is None:
        return
    figure = px.histogram(df, x=metric, color_discrete_sequence=["blue"], nbins=50, opacity=0.5)
    return figure


@app.callback(
    Output('metric-box', 'figure'),
    Input("hist-metric-dropdown", "value"),
    Input('sim-entropy-dropdown', "value"),
    Input('link-type-dropdown', 'value'),
    Input('group-by-linked',"value"),
    State('local', 'data'))
def update_box_graph(metric, set_type, link_type, group, store_data):
    params = {"system": store_data["params"]["system"],
              "experiment_path_w2v": store_data["vectors"]["word2vec" + "-" + link_type],
              "experiment_path_d2v": store_data["vectors"]["doc2vec" + "-" + link_type],
              "corpus": store_data["params"]["corpus"]
              }
    EDA = ExploratoryDataSoftwareAnalysis(params=params)
    df = None
    if set_type == "similarity_set":
        df = EDA.similarity_set
    elif set_type == "entropy_set":
        df = EDA.entropy_set
    elif set_type == "shared_set":
        df = EDA.shared_set
    if df is None:
        return
    box = "Outliers" in group
    if box:
        box = "outliers"
    if "Linked?" in group:
        figure = px.box(df, y=metric, color_discrete_sequence=["blue","red"], color="Linked?", points=box)
    else:
        figure = px.box(df, y=metric, color_discrete_sequence=["blue", "red"], points=box)

    return figure


@app.callback(
    Output("shared_info_target", "options"),
    Output("shared_info_target", "value"),
    Input('vec-type-dropdown', "value"),
    Input('link-type-dropdown', 'value'),
    State('local', 'data'))
def update_shared_info_target(vec, link, data):
    df = experiment_to_df(data["vectors"][vec + "-" + link])

    options = [{'label': str(key), 'value': str(key)} for key in list(set(df["Target"]))]
    value = list(set(df["Target"]))[0]
    return options, value


@app.callback(
    Output("metric-dropdown", 'options'),
    Output("metric-dropdown", 'value'),
    Input("vec-type-dropdown", 'value'),
    Input("link-type-dropdown", "value"),
    State('local', 'data'))
def update_metric_dropdown(vec, link, data):
    df = experiment_to_df(data["vectors"][vec + "-" + link])
    cols = df.drop(columns=["Source", "Target", "Linked?"]).columns
    return [{"label": sim, "value": sim} for sim in cols], cols[0]


@app.callback(
    Output("hist-metric-dropdown", "options"),
    Output("hist-metric-dropdown", "value"),
    Input('sim-entropy-dropdown', "value"),
    Input("link-type-dropdown", "value"),
    State('local', 'data'))
def update_shared_info_target(set_type, link, data):
    params = {"system": data["params"]["system"],
              "experiment_path_w2v": data["vectors"]["word2vec" + "-" + link],
              "experiment_path_d2v": data["vectors"]["doc2vec" + "-" + link],
              "corpus": data["params"]["corpus"]
              }
    EDA = ExploratoryDataSoftwareAnalysis(params=params)
    df = None
    if set_type == "similarity_set":
        df = EDA.similarity_set
    elif set_type == "entropy_set":
        df = EDA.entropy_set
    elif set_type == "shared_set":
        df = EDA.shared_set

    options = [{'label': str(key), 'value': str(key)} for key in list(set(df.columns))]
    value = options[0]["value"]
    return options, value
