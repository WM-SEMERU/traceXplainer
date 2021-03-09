import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate

from app import app

system_dropdown = dcc.Dropdown(
    id='system_type',
    options=[{"label": "Libest", "value": "libest"}, {"label": "Windows", "value": "windows"}],
    value="libest"
)

w2v_text = dcc.Textarea(
    id="w2v-text",
    value='[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv',
    style={'width': '100%'},
)

d2v_text = dcc.Textarea(
    id="d2v-text",
    value='[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv',
    style={'width': '100%'},
)

time_stamp = dcc.Textarea(
    id="time-stamp",
    value="1596063103.098236",
    style={'width': '100%'},
)

saving_path = dcc.Textarea(
    id="saving-path",
    value='../dvc-ds4se/se-benchmarking/traceability/testbeds/processed/',
    style={'width': '100%'},
)
layout = html.Div([
    html.H3('Main Page'),
    dcc.Link('Go to App 1', href='/apps/app1', style={"marginRight": "15px"}),
    dcc.Link('Go to App 2', href='/apps/app2'),
    html.Br(),
    html.Table([
        html.Tr([html.Td(['Operating System']), system_dropdown]),
        html.Tr([html.Td(['experiment_path_w2v']), w2v_text]),
        html.Tr([html.Td(["experiment_path_d2v"]), d2v_text]),
        html.Tr([html.Td(["saving_path"]), saving_path]),
        html.Tr([html.Td(['timestamp']), time_stamp]),
    ], style={"border": "1px solid black", "width": "50%"}),
    html.Br(),
    html.Button(["Store data"], id="store-button"),
    html.Button(["Display data"], id="display-button"),
    dcc.Textarea(value="", id="test-out")
])


@app.callback(Output("local", 'data'),
              Input('store-button', 'n_clicks'),
              State("system_type", 'value'),
              State("w2v-text", 'value'),
              State("d2v-text", 'value'),
              State("time-stamp", 'value'),
              State("saving-path", 'value'))
def on_click(n_clicks, sys_t, w2v, d2v, time, path):
    if n_clicks is None:
        # prevent the None callbacks is important with the store component.
        # you don't want to update the store for nothing.
        raise PreventUpdate

    # Give a default data dict with 0 clicks if there's no data.
    data = {"sys_type": sys_t, "w2v": w2v, "d2v": d2v, "time": time, "path": path}

    return data


@app.callback(Output("test-out", 'value'),
              Input('display-button', 'n_clicks'),
              State("local", 'data'))
def on_click2(n_clicks, data):
    if n_clicks is None:
        # prevent the None callbacks is important with the store component.
        # you don't want to update the store for nothing.
        raise PreventUpdate

    # Give a default data dict with 0 clicks if there's no data.
    s = ""
    for key in data.keys():
        s += data[key] + "\n"
    return s
