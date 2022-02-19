import dash_core_components as dcc
import dash_html_components as html
import dash_table
import pandas as pd
from ds4se.ds.description.eval.traceability import ExploratoryDataSoftwareAnalysis
import numpy as np


def generate_layout(data):
    EDA = ExploratoryDataSoftwareAnalysis(params=data["params"])

    # case 0
    maximum = EDA.df_w2v[EDA.df_w2v['EntropyMetric.MSI_I'] == EDA.df_w2v['EntropyMetric.MSI_I'].max()][
        ['Source', 'Target', 'EntropyMetric.MSI_I', 'Linked?']]
    ## MIN MSI_I
    minimum = EDA.df_w2v[EDA.df_w2v['EntropyMetric.MSI_I'] == EDA.df_w2v['EntropyMetric.MSI_I'].min()][
        ['Source', 'Target', 'EntropyMetric.MSI_I', 'Linked?']]
    df_nan_msi = EDA.df_w2v[np.isnan(EDA.df_w2v['EntropyMetric.MSI_I'])]
    df_nan_msi_desc = df_nan_msi.describe()
    df_nan_msi_desc.insert(0, "Statistic", ["count", "mean", "std", "min", "25%", "50%", "75%", "max"])

    # case 1
    max_tgt = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_tgt'] == EDA.df_w2v['EntropyMetric.Entropy_tgt'].max()][
        ['Target']].values
    max_tgt = np.unique(max_tgt)
    max_tgt_df = pd.DataFrame(max_tgt, columns=['Maximum Self Information'])

    min_tgt = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_tgt'] == EDA.df_w2v['EntropyMetric.Entropy_tgt'].min()][
        ['Target']].values
    min_tgt = np.unique(min_tgt)
    min_tgt_df = pd.DataFrame(min_tgt, columns=['Maximum Self Information'])
    q99_max = np.unique(
        EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_tgt'] >= EDA.df_w2v['EntropyMetric.Entropy_tgt'].quantile(.99)][
            ['Target']].values)
    q99_max_df = pd.DataFrame(q99_max, columns=['.99 Quantile'])

    max_src = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_src'] == EDA.df_w2v['EntropyMetric.Entropy_src'].max()][
        ['Source']].values
    max_src = np.unique(max_src)
    max_src_df = pd.DataFrame(max_src, columns=['Maximum Self Information'])

    min_src = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_src'] == EDA.df_w2v['EntropyMetric.Entropy_src'].min()][
        ['Source']].values
    min_src = np.unique(min_src)
    min_src_df = pd.DataFrame(min_src, columns=['Minimum Self Information'])

    q99_min = np.unique(
        EDA.df_w2v[EDA.df_w2v['EntropyMetric.Entropy_src'] >= EDA.df_w2v['EntropyMetric.Entropy_src'].quantile(.99)][
            ['Source']])
    q99_min_df = pd.DataFrame(q99_min, columns=['.99 Quantile'])

    # case 2
    max_loss = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Loss'] == EDA.df_w2v['EntropyMetric.Loss'].max()][
        ['Source', 'Target', 'EntropyMetric.Loss', 'Linked?']]
    min_loss = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Loss'] == EDA.df_w2v['EntropyMetric.Loss'].min()][
        ['Source', 'Target', 'EntropyMetric.Loss', 'Linked?']]
    q99_loss = EDA.df_w2v['EntropyMetric.Loss'].quantile(.99)
    q99_links = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Loss'] >= EDA.df_w2v['EntropyMetric.Loss'].quantile(.99)][
        ['Source', 'Target', 'EntropyMetric.Loss', 'Linked?']]

    # case 3
    max_noise = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Noise'] == EDA.df_w2v['EntropyMetric.Noise'].max()][
        ['Source', 'Target', 'EntropyMetric.Noise', 'Linked?']]
    min_noise = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Noise'] == EDA.df_w2v['EntropyMetric.Noise'].min()][
        ['Source', 'Target', 'EntropyMetric.Noise', 'Linked?']]
    q99_noise = EDA.df_w2v['EntropyMetric.Noise'].quantile(.99)
    q99_noise_links = EDA.df_w2v[EDA.df_w2v['EntropyMetric.Noise'] >= EDA.df_w2v['EntropyMetric.Noise'].quantile(.99)][
        ['Source', 'Target', 'EntropyMetric.Noise', 'Linked?']]

    layout = html.Div([
        dcc.Tabs([
            dcc.Tab(label='Case 0', children=[
                html.P(['Overlapping Information']),
                html.P(["This study shows potential overlaps on artifacts by measures the Minimum Shared Information \
                        Entropy(MSI_I).Ideally, lower values of MSI means that the link should not exist. \
                        Conversely, highervalues of MSI means that there are enough conditions for the link to exist."]),
                html.P(['Maximum']),
                dash_table.DataTable(
                    data=maximum.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(maximum.columns)],
                    page_size=10,
                    style_table={'overflowX': 'scroll'},
                ),
                html.P(['Minimum']),
                dash_table.DataTable(
                    data=minimum.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(minimum.columns)],
                    page_size=10,
                    style_table={'overflowX': 'scroll'},
                ),
                html.P(["Nan Cases of MSI"]),
                html.P(["These cases are particularly relevant because show potential links that do not \
                    share any information at all"]),
                dash_table.DataTable(
                    data=df_nan_msi.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(df_nan_msi.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                dash_table.DataTable(
                    data=df_nan_msi_desc.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(df_nan_msi_desc.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
            ]),
            dcc.Tab(label='Case 1', children=[
                html.P(['Edge Cases of Self-Information']),
                html.P(["This study highlights the imbalance of information between the source and target artifacts."]),
                html.P(['Target Artifacts']),
                html.P(['Maximum Self Information']),
                dash_table.DataTable(
                    data=max_tgt_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(max_tgt_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                html.P(['Minimum Self Information']),
                dash_table.DataTable(
                    data=min_tgt_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(min_tgt_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                html.P(['.99 quantile for the Target Artifacts']),
                dash_table.DataTable(
                    data=q99_max_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(q99_max_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                html.P(['Source Artifacts']),
                html.P(['Maximum Self Information']),
                dash_table.DataTable(
                    data=max_src_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(max_src_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                html.P(['Minimum Self Information']),
                dash_table.DataTable(
                    data=min_src_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(min_src_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                html.P(['.99 quantile for the Target Artifacts']),
                dash_table.DataTable(
                    data=q99_min_df.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(q99_min_df.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
            ]),
            dcc.Tab(label='Case 2', children=[
                dcc.Markdown('''## Case 2: Minimum and maximum loss
This study presents edge cases for the entropy loss. This is useful to detect poorly documented target artifacts.'''),
                dcc.Markdown('''Max loss'''),
                dash_table.DataTable(
                    data=max_loss.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(max_loss.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                dcc.Markdown('''Min loss'''),
                dash_table.DataTable(
                    data=min_loss.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(min_loss.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                dcc.Markdown('''Quantile .99'''),
                html.P([str(q99_loss)]),
                dcc.Markdown('''Quantile .99 links'''),
                dash_table.DataTable(
                    data=q99_links.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(q99_links.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
            ]),
            dcc.Tab(label='Case 3', children=[
                dcc.Markdown('''## Case 3: Minimum and maximum noise
This study presents edge cases for the entropy noise. This is useful to detect poorly documented source artifacts.'''),
                dcc.Markdown('''Max Noise'''),
                dash_table.DataTable(
                    data=max_noise.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(max_noise.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                dcc.Markdown('''Min Noise'''),
                dash_table.DataTable(
                    data=min_noise.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(min_noise.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
                dcc.Markdown('''Quantile .99'''),
                html.P([str(q99_noise)]),
                dcc.Markdown('''Quantile .99 links'''),
                dash_table.DataTable(
                    data=q99_noise_links.to_dict("records"),
                    columns=[{'id': c, 'name': c} for c in list(q99_noise_links.columns)],
                    style_table={'overflowX': 'scroll'},
                    page_size=10,
                ),
            ])
        ])
    ])

    return layout
