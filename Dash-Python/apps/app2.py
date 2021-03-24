import dash_core_components as dcc
import dash_html_components as html
import dash_table
from dash.dependencies import Input, Output, State
from dash.exceptions import PreventUpdate

from app import app

from ds4se.ds.description.eval.traceability import VectorEvaluation
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation
import ds4se as ds
from ds4se.mining.ir import VectorizationType


def generateLayout(data):
    params = data["params"]
    v = VectorEvaluation(params)
    shared_info_table = dash_table.DataTable(
        id="shared_info_datatable",
        data=v.sharedInfo.to_dict("records"),
        page_current=0,
        sort_action='native',
        columns=[{'id': c, 'name': c} for c in v.sharedInfo.columns],
        filter_action="native",
        page_size=10, )

    layout = html.Div([
        shared_info_table,
        # dcc.Graph(
        #     id='super_eval_graph1',
        #     figure=fig,
        #     style={"marginTop": "50px"}
        # )
    ])
    return layout
