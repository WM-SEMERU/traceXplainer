'''
Daniel McCrystal
August 2018
'''

import sys
from facade import *
from statistics import mean, stdev
from ir.Corpus import Corpus
from empirical import parallelize


def req_2_req_test(corpus_code):
    corpus = Corpus.get_preset_corpus(corpus_code)
    path_to_irs = 'empirical/default_output/' + corpus_code + '/sim/'

    vsm = VSM(corpus, relationship_type=1)
    req2req_model = vsm.generate_model()
    req2req_model.set_default_threshold(0.65)

    req_2_req_dict = dict()
    for source in corpus.get_source_names():
        req_2_req_dict[source] = req2req_model.query(source)

    link_subset = [('RQ21.txt', 'est_proxy.c')]
    target = ['us1005.c', 'us1060.c', 'us2174.c', 'us748.c', 'us893.c', 'us895.c']
    execution_dict = {'est_proxy.c': target}
    models = get_trace_models_with_ltr(
        complexity=3,
        path_to_irs=path_to_irs,
        link_subset=link_subset,
        #req_2_req_dict=req_2_req_dict,
        execution_dict = execution_dict,
        config='ten_ir',
        write_prob_models=False
    )
    #for model in models:
    #    model.write_to_file(model.get_name() + '.tm', path='output_buffer/association_output/' + corpus_code + '/')

def execution_traces(corpus_code):
    corpus = Corpus.get_preset_corpus(corpus_code)
    path_to_irs = 'empirical/default_output/' + corpus_code + '/sim/'

    vsm = VSM(corpus, relationship_type=1)
    req2req_model = vsm.generate_model()
    req2req_model.set_default_threshold(0.65)

    req_2_req_dict = dict()
    for source in corpus.get_source_names():
        req_2_req_dict[source] = req2req_model.query(source)

    link_subset = [('RQ21.txt', 'est_proxy.c')]
    target = ['us1005.c', 'us1060.c', 'us1884.c', 'us2174.c', 'us748.c', 'us893.c', 'us895.c']
    execution_dict = {'est_proxy.c': target}
    models = get_trace_models_with_ltr(
        complexity=3,
        path_to_irs=path_to_irs,
        link_subset=link_subset,
        #req_2_req_dict=req_2_req_dict,
        execution_dict = execution_dict,
        config='ten_ir',
        write_prob_models=False
    )
    #for model in models:
    #    model.write_to_file(model.get_name() + '.tm', path='output_buffer/association_output/' + corpus_code + '/')




if __name__ == '__main__':
    corpus_code = '0_1'
    req_2_req_test(corpus_code)
    #num_cores = 1
    #parallelize.do_process(corpus_code, num_threads=num_cores, experiment_number=5, config='holistic', association_models=['NUTS', 'MAP'])
