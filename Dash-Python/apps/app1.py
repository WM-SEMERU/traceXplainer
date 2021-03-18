import dash
import dash_core_components as dcc
import dash_table
import dash_html_components as html
from dash.dependencies import Input, Output, State
import plotly.express as px
import pandas as pd
import os

from app import app
from tminer_source import experiment_to_df

# have this read in, maybe as a first line of DummyData2.txt that gets ignored
sim_threshold = .23
df = pd.read_table(filepath_or_buffer="DummyData2.txt", names=["src", "tgt", "sim", "security"], sep=" ")
desc = {}
for entry in df.src.unique():
    f = open(os.path.join("artifacts", "req", entry))
    description = ""
    for line in f.readlines():
        description += line
    desc[entry] = description

desc = pd.DataFrame([desc])

fig = px.scatter(df, x="src", y="sim", hover_name="tgt", labels={"color": "Linked"},
                 color=df["sim"] > sim_threshold,
                 color_discrete_sequence=["red", "blue"])
fig.update_layout(legend_traceorder="reversed")


@app.callback(
    Output('rq_datatable', 'data'),
    Output('rq_datatable', 'columns'),
    Input('store-button', 'n_clicks'),
    State("local", 'data'))
def GenerateLinkTable(_, data):
    path = data["w2v"]
    df = experiment_to_df(path)
    print("called")
    print(df.columns)
    return df.to_dict('records'), df.columns


@app.callback(
    Output('textarea-state-example', 'value'),
    Input('rq_datatable', 'active_cell'),
    Input("rq_datatable", "page_current"),
    Input("rq_datatable", "derived_virtual_data"))
def update_text_from_table(active_cell, page_current, derived_virtual_data):
    if active_cell:
        col = active_cell['column_id']
        row = active_cell['row'] + 10 * page_current
        cell_data = derived_virtual_data[row][col]
        if col == "src":
            return desc[cell_data][0]
        if col == "tgt":
            return "Printing tgt src files not implemented yet"
        else:
            return desc[derived_virtual_data[row]["src"]][0]
    return 'no cell selected'


@app.callback(
    Output('sub-graph', 'figure'),
    Input('dropdown', 'value'))
def update_figure(selected_req):
    filtered_df = df[df["src"] == selected_req]
    figure = px.scatter(filtered_df, x="tgt", y="sim", hover_name="tgt", title=selected_req,
                        labels={"color": "Linked"},
                        color=filtered_df["sim"] > sim_threshold,
                        color_discrete_sequence=["red", "blue"])
    figure.update_layout(legend_traceorder="reversed")
    return figure


# was app.layout when it was the main page
layout = html.Div(children=[
    dcc.Tabs([
        dcc.Tab(label='Data Description', children=[
            dcc.Graph(
                id='basic-sim-graph',
                figure=fig,
                style={"marginTop": "50px"}
            ),
            dcc.Graph(
                id='sub-graph',
            ),
            dcc.Dropdown(
                id='dropdown',
                options=[{'label': key, 'value': key} for key in desc.keys()],
                value=desc.keys()[0]
            ),
        ]),
        dcc.Tab(label='Browse Links', children=[
            html.Div(children=[
                dash_table.DataTable(
                    id="rq_datatable",
                    data=df.to_dict('records'),
                    page_current=0,
                    sort_action='native',
                    columns=[{'id': c, 'name': c} for c in df.columns],
                    filter_action="native",
                    page_size=10, ),
                dcc.Textarea(
                    id='textarea-state-example',
                    # value=desc["RQ38.txt"][0],
                    style={'width': '100%', 'height': 200},
                    readOnly=True,
                ),
            ], style={"maxWidth": "50%"}),

        ])
    ])
])


def generateLayout(store_data):
    path = "artifacts/" + store_data["w2v"]
    path = "artifacts/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv"
    df1 = experiment_to_df(path)
    layout = html.Div(children=[
        dcc.Tabs([
            dcc.Tab(label='Data Description', children=[
                dcc.Graph(
                    id='basic-sim-graph',
                    figure=fig,
                    style={"marginTop": "50px"}
                ),
                dcc.Graph(
                    id='sub-graph',
                ),
                dcc.Dropdown(
                    id='dropdown',
                    options=[{'label': key, 'value': key} for key in desc.keys()],
                    value=desc.keys()[0]
                ),
            ]),
            dcc.Tab(label='Browse Links', children=[
                html.Div(children=[
                    dash_table.DataTable(
                        id="rq_datatable",
                        data=df1.to_dict('records'),
                        page_current=0,
                        sort_action='native',
                        columns=[{'id': c, 'name': c} for c in list(df1.columns)],
                        filter_action="native",
                        page_size=10, ),
                    dcc.Textarea(
                        id='textarea-state-example',
                        style={'width': '100%', 'height': 200},
                        readOnly=True,
                    ),
                ], style={"maxWidth": "50%"}),

            ])
        ])
    ])
    return layout
