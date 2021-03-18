import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc
from dash.dependencies import Input, Output, State

from app import app
from apps import app1, app2, main, fileUploadPage

# the style arguments for the sidebar. We use position:fixed and a fixed width
SIDEBAR_STYLE = {
    "position": "fixed",
    "top": 0,
    "left": 0,
    "bottom": 0,
    "width": "16rem",
    "padding": "2rem 1rem",
    "background-color": "#f8f9fa",
}

# the styles for the main content position it to the right of the sidebar and
# add some padding.
CONTENT_STYLE = {
    "margin-left": "18rem",
    "margin-right": "2rem",
    "padding": "2rem 1rem",
}

sidebar = html.Div(
    [
        html.H2("Sidebar", className="display-4"),
        html.Hr(),
        html.P(
            "A simple sidebar layout with navigation links", className="lead"
        ),
        dbc.Nav(
            [
                dbc.NavLink("Home", href="/", active="exact"),
                dbc.NavLink("Page 1", href="/apps/app1", active="exact"),
                dbc.NavLink("Page 2", href="/apps/app2", active="exact"),
            ],
            vertical=True,
            pills=True,
        ),
    ],
    style=SIDEBAR_STYLE,
)

content = html.Div(id="page-content", style=CONTENT_STYLE)

app.layout = html.Div([dcc.Store(id='local', storage_type='local'),dcc.Location(id="url"), sidebar, content])

# app.layout = html.Div([
#     dcc.Store(id='local', storage_type='local'),
#     dcc.Location(id='url', refresh=False),
#     html.Div(id='page-content')
# ])


@app.callback(Output('page-content', 'children'),
              Input('url', 'pathname'),
              State('local', 'data'))
def display_page(pathname,data):
    if pathname == '/apps/app1':
        return app1.generateLayout(data)
    elif pathname == '/apps/app2':
        return app2.layout
    elif pathname == '/apps/fileUploadPage':
        return fileUploadPage.layout
    else:
        return main.layout


if __name__ == '__main__':
    app.run_server(debug=True)