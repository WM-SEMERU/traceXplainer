import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc
from dash.dependencies import Input, Output, State

from app import app
from apps import descriptive_page, predictive_page, main_page, cases_page

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
        html.H2("T-Miner", className="display-4"),
        html.Hr(),
        html.P(
            children="A description of T-Miner", className="lead", id="sidebar_text"
        ),
        dbc.Nav(
            [
                dbc.NavLink("Home", href="/", active="exact"),
                dbc.NavLink("Descriptive Analysis", href="/apps/descriptive_page", active="exact"),
                dbc.NavLink("Predictive Analysis", href="/apps/predictive_page", active="exact"),
                dbc.NavLink("Case Analysis", href="/apps/cases", active="exact"),
            ],
            vertical=True,
            pills=True,
        ),
    ],
    style=SIDEBAR_STYLE,
)

content = html.Div(id="page-content", style=CONTENT_STYLE)

app.layout = html.Div([dcc.Store(id='local', storage_type='local'), dcc.Location(id="url"), sidebar, content])


@app.callback(Output('page-content', 'children'),
              Input('url', 'pathname'),
              State('local', 'data'))
def display_page(pathname, data):
    if pathname == '/apps/descriptive_page':
        return descriptive_page.generate_layout(data)
    elif pathname == '/apps/predictive_page':
        return predictive_page.generate_layout(data)
    elif pathname == '/apps/cases':
        return cases_page.generate_layout(data)
    else:
        return main_page.generate_layout()


if __name__ == '__main__':
    app.run_server(debug=True)
