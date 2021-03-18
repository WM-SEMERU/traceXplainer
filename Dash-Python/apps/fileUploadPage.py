import os
import sqlite3
from sqlite3 import Error

import base64
from dash.dependencies import Input, Output, State
import dash_core_components as dcc
import dash_html_components as html

import pandas as pd

from app import app


uploadDir = "uploadedData"


layout = html.Div([
    dcc.Upload(
        id='upload-data',
        children=html.Div([
            'Drag and Drop or ',
            html.A('Select Files')
        ]),
        style={
            'width': '100%',
            'height': '60px',
            'lineHeight': '60px',
            'borderWidth': '1px',
            'borderStyle': 'dashed',
            'borderRadius': '5px',
            'textAlign': 'center',
            'margin': '10px'
        },
        # Allow multiple files to be uploaded
        multiple=True
    ),
    html.Div(id='output-data-upload'),
])


def save_file(name, content):
    """Decode and store a file uploaded with Plotly Dash."""
    print(name)
    print(content)
    data = content.encode("utf8").split(b";base64,")[1]
    with open(os.path.join(uploadDir, name), "wb") as fp:
        fp.write(base64.decodebytes(data))


@app.callback(Output('output-data-upload', 'children'),
              Input('upload-data', 'contents'),
              State('upload-data', 'filename'),
              State('upload-data', 'last_modified'))
def update_output(list_of_contents, list_of_names, list_of_dates):
    if list_of_contents is not None:
        children = [
            save_file(n, c) for c, n, d in
            zip(list_of_contents, list_of_names, list_of_dates)]
        return children


try:
    conn = sqlite3.connect("../test.db")
except Error as e:
    print(e)

