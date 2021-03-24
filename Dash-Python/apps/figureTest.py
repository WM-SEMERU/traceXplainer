from app import app
import dash_html_components as html
from ds4se.mining.ir import VectorizationType
from ds4se.ds.prediction.eval.traceability import SupervisedVectorEvaluation


def libest_params():
    return {
        "system": 'libest',
        # "experiment_path_w2v": path_data + '[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv',
        # "experiment_path_d2v": path_data + '[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv',
        "experiment_path_w2v": "artifacts/[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv",
        "experiment_path_d2v": "artifacts/[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv",
        # 'saving_path': '../dvc-ds4se/se-benchmarking/traceability/testbeds/processed/',
        'saving_path': 'testbeds/processed/',
        'system_long': 'libest',
        'timestamp': 1596063103.098236,
        'language': 'all-corpus'
    }



#params = libest_params()
#s = SupervisedVectorEvaluation(params)
#s.Compute_avg_precision(VectorizationType.word2vec)

layout = html.Div([
    html.H2("fig_testPage", className="display-4"),
])