import dash_core_components as dcc
import dash_table
import dash_html_components as html
import dash_bootstrap_components as dbc
from dash.dependencies import Input, Output, State
import plotly.express as px
import pandas as pd
import os

from app import app

# have this read in, maybe as a first line of DummyData2.txt that gets ignored
sim_threshold = .23
# Replace this with information stored in the database. Only  load the data in as it is needed
df_dum = pd.read_table(filepath_or_buffer="DummyData2.txt", names=["src", "tgt", "sim", "security"], sep=" ")

desc = {}
for entry in df_dum.src.unique():
    f = open(os.path.join("artifacts", "req", entry))
    description = ""
    for line in f.readlines():
        description += line
    desc[entry] = description

desc = pd.DataFrame([desc])

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
    Input("link-datatable", "derived_virtual_data"))
def update_text_from_table(active_cell, page_current, derived_virtual_data):
    if active_cell:
        col = active_cell['column_id']
        row = active_cell['row'] + 10 * page_current
        cell_data = derived_virtual_data[row][col]
        if col == "Source":
            return desc[cell_data][0]
        if col == "Target":
            return "Printing tgt src files not implemented yet"
        else:
            return desc[derived_virtual_data[row]["Source"]][0]
    return 'no cell selected'


def generateLayout(store_data):
    """Generate layout is called everytime the descriptive page is selected. It returns the layout for the page to
    display, after performing calculations to generate the necessary tables and data sources."""
    df = pd.DataFrame.from_dict(store_data["d2v"][1])
    fig = px.scatter(df, x="Source", y="SimilarityMetric.EUC_sim", hover_name="Target", labels={"color": "Linked"},
                     color=df["Linked?"] == 1,
                     color_discrete_sequence=["red", "blue"])
    fig.update_layout(legend_traceorder="reversed")
    df1 = df[["Source", "Target", "Linked?"]]
    layout = html.Div(children=[
        dcc.Tabs([
            dcc.Tab(label='Data Description', children=[
                dcc.Graph(
                    id='one-requirement-graph',
                ),
                dcc.Dropdown(
                    id='dropdown',
                    options=[{'label': key, 'value': key} for key in desc.keys()],
                    value=desc.keys()[0]
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
                                    options=[{'label': key, 'value': key} for key in set(df["Target"])],
                                    value=df["Target"][0],
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
                ], style={'columnCount': 2})
            ])
        ])
    ], style={'width': '100%'})
    return layout


@app.callback(
    Output('one-requirement-graph', 'figure'),
    Input('dropdown', 'value'),
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
