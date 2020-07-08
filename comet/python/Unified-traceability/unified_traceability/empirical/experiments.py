'''
Daniel McCrystal
August 2018
'''

import sys
from facade import *
from statistics import mean, stdev
from ir.Corpus import Corpus
from empirical import parallelize
from ir.Trace_Model import Trace_Model
import random

def experiment_1(ir_models):

    ltr_models = get_trace_models_with_ltr(similarities=ir_models, complexity=1, inference_types=['VI', 'NUTS', 'MAP'], config='ten_ir', write_prob_models=True, pre_normalized=True)

    return ltr_models

def experiment_2(ir_models, corpus_code, sampling_type, error):
    """
    Note: This operates under the assumption that the links have already been sampled,
    and the given IR models only contain those sampled links
    """

    corpus = Corpus.get_preset_corpus(corpus_code)
    all_links = list(ir_models[0].get_links())

    domain_conf_dict = dict()
    for source, target in all_links:
        if source not in domain_conf_dict:
            domain_conf_dict[source] = dict()

        if corpus.get_truth_value(source, target) == 1:
            domain_conf_dict[source][target] = 0.9
        else:
            domain_conf_dict[source][target] = 0.1

    num_wrong = int(len(all_links) * error)
    random.seed(12345)
    all_links = sorted(all_links)
    wrong_links = random.sample(all_links, num_wrong)
    for source, target in wrong_links:
        if domain_conf_dict[source][target] == 0.9:
            domain_conf_dict[source][target] = 0.1
        else:
            domain_conf_dict[source][target] = 0.9

    config = sampling_type + '_' + str(int(error * 100)) + '%_error'
    ltr_models = get_trace_models_with_ltr(similarities=ir_models, domain_conf_dict=domain_conf_dict, complexity=2, inference_types=['NUTS', 'MAP'], config=config, pre_normalized=True)

    return ltr_models

def experiment_3(ir_models, corpus_code):
    """
    Note: This operates under the assumption that the complexity 1 models have already
    been generated for the requirement -> test case links. AND that those files
    are in the same corpus folder in the datastore. If the source code and test cases come from
    two different corpora, this will not be the case by default, and the files will need to be copied manually.
    """

    corpus = Corpus.get_preset_corpus(corpus_code)

    execution_dict = dict()
    for target in ir_models[0].get_target_names():
        execution_dict[target] = corpus.get_execution_trace(target)

    ltr_models = get_trace_models_with_ltr(similarities=ir_models, execution_dict=execution_dict, complexity=3, inference_types=['NUTS', 'MAP'], pre_normalized=True)

    return ltr_models

def experiment_4(ir_models, corpus_code):

    corpus = Corpus.get_preset_corpus(corpus_code)

    vsm = VSM(corpus, relationship_type=1)
    req2req_model = vsm.generate_model()
    req2req_model.set_default_threshold(0.65)

    req_2_req_dict = dict()
    for source in corpus.get_source_names():
        req_2_req_dict[source] = req2req_model.query(source)

    link_subset = [('RQ37.txt', 'est_server.c')]

    ltr_models = get_trace_models_with_ltr(similarities=ir_models, link_subset=link_subset, req_2_req_dict=req_2_req_dict, complexity=4, inference_types=['NUTS', 'MAP'], pre_normalized=True)

    return ltr_models

def experiment_5(ir_models, corpus_code):
    corpus = Corpus.get_preset_corpus(corpus_code)
    random.seed(12345)

    all_links = list(ir_models[0].get_links())
    all_links = sorted(all_links)
    dev_feedback_links = random.sample(all_links, int(len(all_links) * 0.1))

    domain_conf_dict = dict()
    for source, target in dev_feedback_links:
        if source not in domain_conf_dict:
            domain_conf_dict[source] = dict()

        if corpus.get_truth_value(source, target) == 1:
            domain_conf_dict[source][target] = 0.9
        else:
            domain_conf_dict[source][target] = 0.1

    error = 0.25
    num_wrong = int(len(dev_feedback_links) * error)

    wrong_links = random.sample(dev_feedback_links, num_wrong)
    for source, target in wrong_links:
        if domain_conf_dict[source][target] == 0.9:
            domain_conf_dict[source][target] = 0.1
        else:
            domain_conf_dict[source][target] = 0.9

    config = 'holistic'

    vsm = VSM(corpus, relationship_type=1)
    req2req_model = vsm.generate_model()
    req2req_model.set_default_threshold(0.65)

    req_2_req_dict = dict()
    for source in corpus.get_source_names():
        req_2_req_dict[source] = req2req_model.query(source)

    ltr_models = get_trace_models_with_ltr(similarities=ir_models, req_2_req_dict=req_2_req_dict, domain_conf_dict=domain_conf_dict, complexity=0, inference_types=['NUTS', 'MAP'], pre_normalized=True)

    return ltr_models
