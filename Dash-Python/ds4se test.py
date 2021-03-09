import ds4se
from ds4se.ds.description.eval.traceability import VectorEvaluation

experiment = 'experiments0.0.x/'
path_data = '../ds4se/dvc-ds4se/metrics/traceability/' + experiment


def libest_params():
    return {
        "system": 'libest',
        "experiment_path_w2v": path_data + '[libest-VectorizationType.word2vec-LinkType.req2tc-True-1609292406.653621].csv',
        "experiment_path_d2v": path_data + '[libest-VectorizationType.doc2vec-LinkType.req2tc-True-1609289141.142806].csv',
        'saving_path': '../dvc-ds4se/se-benchmarking/traceability/testbeds/processed/',
        'system_long': 'libest',
        'timestamp': 1596063103.098236,
        'language': 'all-corpus'
    }


params = libest_params()
v = VectorEvaluation(params)
