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
    State('local', 'data'))
def update_infometric_table(src, tgt, data):
    df = pd.DataFrame.from_dict(data["d2v"][1])
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
    Input("tokenization-dropdown","value"),
    State("local", 'data'))
def update_text_from_table(active_cell, page_current, derived_virtual_data,display,data):
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


def generate_layout(store_data, ):
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
    print(type(fig))
    df1 = df[["Source", "Target", "Linked?"]]

    layout = html.Div(children=[
        dcc.Tabs([
            dcc.Tab(label='Data Description', children=[
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
                        dcc.Dropdown(
                            id="vectorization-type",
                            options=[{'label': "text", 'value': "text"}, {'label': "conv", 'value': "conv"},
                                     {'label': "bpe128k", 'value': "bpe128k"}, {'label': "bpe32k", 'value': "bpe32k"},
                                     {'label': "bpe8k", 'value': "bpe8k"}],
                            value="text",
                            style={"width": "100%"}
                        ),
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
                        id = "tokenization-dropdown",
                        options=[{'label': "text", 'value': "text"},{'label': "conv", 'value': "conv"},
                                 {'label': "bpe128k", 'value': "bpe128k"},{'label': "bpe32k", 'value': "bpe32k"},
                                 {'label': "bpe8k", 'value': "bpe8k"}],
                        value="text",
                        style={"width": "100%"}
                    )
                ], style={'columnCount': 2})
            ])
        ])
    ], style={'width': '100%'})
    return layout


@app.callback(
    Output('one-requirement-graph', 'figure'),
    Input('file-select-dropdown', 'value'),
    State('local', 'data'), )
def updateOneRequirementGraph(selected_req, store_data):
    df = pd.DataFrame.from_dict(store_data["d2v"][1])
    filtered_df = df[df["Source"] == selected_req]
    figure = px.scatter(filtered_df, x="Target", y="SimilarityMetric.EUC_sim", hover_name="Target", title=selected_req,
                        labels={"color": "Linked"},
                        color=filtered_df["Linked?"] == 1,
                        color_discrete_sequence=["red", "blue"])
    figure.update_layout(legend_traceorder="reversed")
    return figure

# @app.callback(
#     Output("shared_info_target", "value"),
#     Output("shared_info_target", "options")
# )
# def update_shared_info_target():
#     options = [{'label': key, 'value': key} for key in set(df["Target"])],
#     value = df["Target"][0],

# v = VectorEvaluation(params)
#     shared_info_table = dash_table.DataTable(
#         id="shared_info_datatable",
#         data=v.sharedInfo.to_dict("records"),
#         page_current=0,
#         sort_action='native',
#         columns=[{'id': c, 'name': c} for c in v.sharedInfo.columns],
#         filter_action="native",
#         page_size=10, )
