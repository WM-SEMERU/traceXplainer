import dash_core_components as dcc
import dash_html_components as html
import dash_table
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate

from app import app

from ds4se.ds.description.eval.traceability import VectorEvaluation


def libest_params():
    return {
        # set name and upload system, use libest for now
        "system": 'libest',
        #uploaded by user
        "experiment_path_w2v": "artifacts/[libest-VectorizationType.word2vec-LinkType.req2tc-True-1608690009.09251].csv",
        # uploaded by user
        "experiment_path_d2v": "artifacts/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv",
        'saving_path': 'testbeds/processed/',
        'system_long': 'libest',
        # uploaded by user
        'timestamp': 1596063103.098236,
        # selected by user in the future, for now it will be hard coded
        'language': 'all-corpus'
    }


params = libest_params()
v = VectorEvaluation(params)

drop_down2 = dcc.Dropdown(
        id='app-2-dropdown',
        options=[
            {'label': 'App 2 - {}'.format(i), 'value': i} for i in [
                'NYC', 'MTL', 'LA'
            ]
        ]
    )

sharedInfoTable = dash_table.DataTable(
    id="shared_info_datatable",
    data=v.sharedInfo.to_dict("records"),
    page_current=0,
    sort_action='native',
    columns=[{'id': c, 'name': c} for c in v.sharedInfo.columns],
    filter_action="native",
    page_size=10, )


@app.callback(
    Output('app-2-display-value', 'children'),
    Input('app-2-dropdown', 'value'))
def display_value(value):
    return 'You have selected "{}"'.format(value)


@app.callback(Output("test-out2", 'value'),
              Input('display-button2', 'n_clicks'),
              State("local", 'data'))
def on_click3(n_clicks, data):
    if n_clicks is None:
        # prevent the None callbacks is important with the store component.
        # you don't want to update the store for nothing.
        raise PreventUpdate

    # Give a default data dict with 0 clicks if there's no data.
    s = ""
    for key in data.keys():
        s += str(data[key]) + "\n"
    return s


layout = html.Div([
    html.H3('App 2'),
    drop_down2,
    html.Div(id='app-2-display-value'),
    dcc.Link('Go to App 1', href='/apps/app1'),
    html.Button(["Display data"], id="display-button2"),
    dcc.Textarea(value="", id="test-out2"),
    sharedInfoTable
])
