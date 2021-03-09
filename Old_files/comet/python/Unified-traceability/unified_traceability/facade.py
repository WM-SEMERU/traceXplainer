'''
Daniel McCrystal
June 2018

'''

def warn(*args, **kwargs):
    pass
import warnings
warnings.warn = warn

import numpy as np
import traceback
import os
#from statistics import *

from causalityFacade import *
from DatastoreManager import DatastoreManager

from ir.Corpus import Corpus
from ir.Evaluator import Evaluator
from ir.Orthogonal_Evaluator import Orthogonal_Evaluator

from ir.IR_Method import IR_Method
from ir.Trace_Model import Trace_Model

from ir.Orthogonal_IR import Orthogonal_IR

from ir.VSM import VSM
from ir.LSI import LSI
from ir.JensenShannon import JensenShannon
from ir.Topic_Models import Topic_Models
from ir.LDA import LDA
from ir.NMF import NMF

##Logger -danaderp
import logging
logger = logging.getLogger('ltr_app')
hdlr = logging.FileHandler('ltr_app.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.DEBUG) #When activated, this is the level of logging


def get_new_corpus_object(name, root, source_path, target_path, truth_path, languages=['english'], filetype_whitelist=None, filetype_blacklist=None):
    return Corpus(name, root, source_path, target_path, truth_path, filetype_whitelist, filetype_blacklist)

def get_new_vsm_model(corpus, similarity_metric=None, smooth=None, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    vsm = VSM(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    parameters = dict()
    if similarity_metric is not None:
        parameters['similarity_metric'] = similarity_metric
    if smooth is not None:
        parameters['smooth'] = smooth

    return vsm.generate_model(parameters=parameters)

def get_new_lsi_model(corpus, n_components=None, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    lsi = LSI(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    parameters = dict()
    if n_components is not None:
        parameters['n_components'] = n_components

    return lsi.generate_model(parameters=parameters)

def get_new_js_model(corpus, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    js = JensenShannon(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    return js.generate_model()

def get_new_lda_model(corpus, n_topics=None, similarity_metric=None, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    lda = LDA(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    parameters = dict()
    if similarity_metric is not None:
        parameters['similarity_metric'] = similarity_metric
    if n_topics is not None:
        parameters['n_topics'] = n_topics

    return lda.generate_model(parameters=parameters)

def get_new_nmf_model(corpus, n_topics=None, similarity_metric=None, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    nmf = NMF(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    parameters = dict()
    if similarity_metric is not None:
        parameters['similarity_metric'] = similarity_metric
    if n_topics is not None:
        parameters['n_topics'] = n_topics

    return nmf.generate_model(parameters=parameters)

def get_new_orthogonal_model(corpus, model_A, model_B, lambda_value=0.5, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    orth = Orthogonal_IR(corpus, only_alphnum=only_alphnum, only_alph=only_alph, split_camel_case=split_camel_case, split_snake_case=split_snake_case, remove_stop_words=remove_stop_words, stem=stem)

    parameters = dict()
    parameters['lambda'] = lambda_value
    return orth.generate_model(model_A, model_B, parameters=parameters)

def get_all_models(corpus, only_alphnum=False, only_alph=True,
    split_camel_case=True, split_snake_case=True, remove_stop_words=True,
    stem=True):

    vsm = get_new_vsm_model(corpus)
    lsi = get_new_lsi_model(corpus)
    js = get_new_js_model(corpus)
    lda = get_new_lda_model(corpus)
    nmf = get_new_nmf_model(corpus)

    jslda = get_new_orthogonal_model(corpus, js, lda)
    jsnmf = get_new_orthogonal_model(corpus, js, nmf)
    vsmjs = get_new_orthogonal_model(corpus, vsm, js)
    vsmlda = get_new_orthogonal_model(corpus, vsm, lda)
    vsmnmf = get_new_orthogonal_model(corpus, vsm, nmf)

    return [vsm, lsi, js, lda, nmf, jslda, jsnmf, vsmjs, vsmlda, vsmnmf]

def get_all_default_models_from_file(corpus_code, adjuster=''):
    model_ids = ['js', 'jslda', 'jsnmf', 'lda', 'lsi', 'nmf', 'vsm', 'vsmjs', 'vsmlda', 'vsmnmf']

    models = []
    for model_id in model_ids:
        path = adjuster + 'empirical/default_output/' + corpus_code + '/sim/'
        filename = 'd_' + model_id + '_sim_' + corpus_code + '.tm'
        models.append(Trace_Model.get_instance_from_file(filename, path=path))

    return models

def get_model_from_file(filename, path, name=None, corpus_name=None):
    return Trace_Model.get_instance_from_file(filename, path, name, corpus_name)

def get_all_models_from_folder(path, names=None, corpus_name=None):
    return Trace_Model.get_all_models_from_folder(path, names, corpus_name)

def get_blank_model(name, corpus_name, source_ids, target_ids, parameters=None):
    return Trace_Model(name, corpus_name, source_ids, target_ids, parameters=parameters)

def get_new_evaluator_object(models, corpus):
    return Evaluator(models, corpus)

def get_ltr_for_link(source, target, complexity, inference_types, confirmed_links,
                     empirical_sim=None, domain_conf=None, execution_traces=None, transitive_req_list=None,
                     sim_req=None, check_for_existing=False, data_manager=None):
    #print("complexity: " + str(complexity))
    #print("inference_keys: " + str(inference_keys))
    logger.info('confirmed_links'+ str(confirmed_links))
    logger.info('empirical_sim: '+ str(empirical_sim))
    #print("domain_conf: " + str(domain_conf))
    #print("execution_traces: " + str(execution_traces))

    inference_mapping = {'NUTS': 'a', 'VI': 'b', 'MAP': 'c'}
    inference_keys = [inference_mapping[inf_type] for inf_type in inference_types]

    prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL if empirical_sim is None else PriorChoice.SPECIFIC_INFORMATIVE
    prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL if domain_conf is None else PriorChoice.SPECIFIC_INFORMATIVE

    pf = PriorFacade(prior_linkage=prior_linkage, prior_domain=prior_domain, empirical_sim=empirical_sim, domain_conf=domain_conf)
    params = ParametersFacade()

    prob_models = []

    for inf_type in inference_types:
        if check_for_existing:
            if data_manager is None:
                logger.info("No data manager provided")
            elif data_manager.file_exists(source, target, inf_type):
                logger.info("Using existing pkl file for " + inf_type + "-(" + source + ", " + target + ")")
                prob_model = data_manager.load_from_datastore(source, target, inf_type)
                prob_models.append(prob_model)
                continue
            else:
                logger.info("No existing file found for " + inf_type + "-(" + source + ", " + target + ")")

        inference_key = inference_mapping[inf_type]
        cf = CausalityFacadeAssociation(confirmed_links, ir_asm_prior=pf, hyper_params=params, progressbar=False)
        cf.apply_latent_traceability_recovery(ass_complexity=complexity, inference_key=inference_key,
                                                execution_traces=execution_traces,
                                                transitive_req_list=transitive_req_list, sim_req=sim_req)
        prob_model = cf.get_ltr(complexity)

        #Plotting Latent Model
        prob_model.get_ltr().get_trace_plot(2500) #.show_trace_plot(2500)
        #Summary of Latent Model
        prob_model.show_summary(2500)

        prob_models.append(prob_model)

    return prob_models

def get_trace_models_with_ltr(
        complexity=1,
        path_to_irs=None,
        corpus=None,
        similarities=None,
        pre_normalized=False,
        informative_similarities=True,
        inference_types=['NUTS'],
        link_subset=None,
        domain_conf_dict=None,
        default_domain_confidence=None,
        execution_dict=None,
        req_2_req_dict=None,
        check_for_existing=False,
        write_prob_models=False,
        config='default',
        path_save_model=''):

    # Determine which method of providing IR values the user gave
    if similarities is None:
        if path_to_irs is not None:
            similarities = Trace_Model.get_all_models_from_folder(path_to_irs)
        elif corpus is not None:
            similarities = get_all_models(corpus)
        else:
            logger.error("You must supply a path, a corpus, or a list of models")
            raise ValueError("You must supply a path, a corpus, or a list of models")

    # Generate the links based on the models' default thresholds
    link_models = [model.get_link_model() for model in similarities]

    # If the similarity values have not been normalized, normalize them
    if not pre_normalized:
        normalized_similarity_models = [model.get_normalized_values_with_sigmoid() for model in similarities]
    else:
        normalized_similarity_models = [model._model for model in similarities]

    # Get consistent metadata for new trace models
    corpus_name = similarities[0].get_corpus_name()
    sources = similarities[0].get_source_names()
    targets = similarities[0].get_target_names()

    # Generate blank trace models to populate with probabilities
    parameters = {'complexity': complexity}

    if config != 'default':
        parameters['config'] = str(config)

    ltr_trace_models = []
    for inf_type in inference_types:
        ltr_trace_models.append(get_blank_model(inf_type, corpus_name, sources, targets, parameters=parameters))

    # Determine which links should actually be computed
    active_links = similarities[0].get_links()
    if link_subset is not None:
        active_links = link_subset

    data_manager = DatastoreManager(corpus_name, complexity, config=config, location=path_save_model)

    datastore_reader = DatastoreManager(corpus_name, complexity=1, config='ten_ir')

    # For each link that we have decided to compute...
    for i, link in enumerate(active_links):
        source, target = link

        adjusted_complexity = complexity

        # Generate the binary vector from the link models
        confirmed_links = np.array([model.get_value(source, target) for model in link_models])

        # Generate the normalized similarity vector only if we are not using weakly informative
        empirical_sim = np.array([model[source][target] for model in normalized_similarity_models]) if informative_similarities else None

        # complexity 2 - domain knowledge
        domain_conf = default_domain_confidence

        if domain_conf is None:
            if complexity == 2 or complexity == 0:
                if domain_conf_dict is not None:
                    if source in domain_conf_dict and target in domain_conf_dict[source]:
                        domain_conf = domain_conf_dict[source][target]
                    else:
                        logger.info("No developer feedback provided for this link")
                        adjusted_complexity = 1
                else:
                    logger.error("Complexity " + str(complexity) + " requested but no domain confidence dictionary provided")
                    raise ValueError("Complexity " + str(complexity) + " requested but no domain confidence dictionary provided")

        # complexity 3 - execution traces
        execution_traces = None
        if complexity == 3: #or complexity == 0:
            if execution_dict is not None:
                linked_artifacts = execution_dict[target]
                execution_traces = []

                if len(linked_artifacts) > 0:
                    for linked_artifact in linked_artifacts:
                        ltr = data_manager.load_from_datastore(source, linked_artifact, 'NUTS', complexity=1,
                                                               actualComplexity=complexity)
                        execution_trace = ltr

                        execution_traces.append(execution_trace)
                else:
                    adjusted_complexity = 1
            else:
                logger.error("Complexity " + str(complexity) + " requested but no execution trace dictionary provided")
                raise ValueError("Complexity " + str(complexity) + " requested but no execution trace dictionary provided")

        # complexity 4 - req to req
        transitive_req_list = None
        sim_req = None
        if complexity == 4 or complexity == 0:
            if req_2_req_dict is not None:
                linked_reqs = [x[0] for x in req_2_req_dict[source]]
                sim_req = [x[1] for x in req_2_req_dict[source]]

                print(linked_reqs)
                print(sim_req)

                if len(linked_reqs) > 0:
                    logger.info('Transitive Link has Complementary Links: ')
                    transitive_req_list = []
                    for req in linked_reqs:
                        logger.info('Req_2_Req: '+str(req))
                        #This is the part where retrieving lower complexity complementary values
                        ltr = data_manager.load_from_datastore(req, target, 'NUTS', complexity=1)
                        logger.info("Retrieved lower complexity links for req2req:" + str(ltr))
                        transitive_req = ltr
                        transitive_req_list.append(transitive_req)
                else:
                    logger.info('Transitive Link has NOT Complementary Links')
                    adjusted_complexity = 1
            else:
                logger.error("Complexity " + str(complexity) + " requested but no complementary links provided")
                raise ValueError("Complexity " + str(complexity) + " requested but no complementary links provided")

        # Holistic model missing information adjusting
        if complexity == 0:
            # no domain confidence given
            if domain_conf is None:
                # no complementary links
                if transitive_req_list is None:
                    adjusted_complexity = 1
                else:
                    adjusted_complexity = 4
            else:
                if transitive_req_list is None:
                    adjusted_complexity = 2
                else:
                    adjusted_complexity = 0

        logger.info("Generating level " + str(adjusted_complexity) + " probabilistic models for: " + source + " - " + target)

        ltrs = get_ltr_for_link(source, target, adjusted_complexity, inference_types, confirmed_links, empirical_sim,
                                domain_conf,
                                execution_traces=execution_traces,
                                transitive_req_list=transitive_req_list,
                                sim_req=sim_req,
                                check_for_existing=check_for_existing,
                                data_manager=data_manager)

        for i, inf_type in enumerate(inference_types):
            ltr = ltrs[i]
            if inf_type == 'VI' or inf_type == 'NUTS':
                link_value = ltr.get_ltr().get_expected_linkage_value()
            elif inf_type == 'MAP':
                link_value = ltr.get_map_linkage_value()

            if write_prob_models and inf_type != 'MAP':
                data_manager.write_to_datastore(source, target, inf_type, ltr)

            logger.info(inf_type + " probability found: " + str(link_value))
            ltr_trace_models[i].set_value(source, target, link_value)

    return ltr_trace_models


def write_default_ir_models(corpus):
    corpus_code = corpus.get_corpus_code()

    vsm = VSM(corpus)
    lsi = LSI(corpus)
    js = JensenShannon(corpus)
    lda = LDA(corpus)
    nmf = NMF(corpus)

    orth = Orthogonal_IR(corpus)

    # Write similarities
    vsm_model = vsm.generate_model(parameters={'similarity_metric': 'cosine'})
    lsi_model = lsi.generate_model(parameters={'n_components': 30})
    js_model = js.generate_model()
    lda_model = lda.generate_model(n_trials=30, parameters={'similarity_metric': 'hellinger', 'n_topics': 40})
    nmf_model = nmf.generate_model(parameters={'similarity_metric': 'divergence', 'n_topics': 30})

    orth_lda_model = lda.generate_model(n_trials=30, parameters={'similarity_metric': 'hellinger', 'n_topics': 5})
    orth_nmf_model = nmf.generate_model(parameters={'similarity_metric': 'divergence', 'n_topics': 40})

    vsm_lda_model = orth.generate_model(vsm_model, orth_lda_model)
    js_lda_model = orth.generate_model(js_model, orth_lda_model)
    vsm_nmf_model = orth.generate_model(vsm_model, orth_nmf_model)
    js_nmf_model = orth.generate_model(js_model, orth_nmf_model)
    vsm_js_model = orth.generate_model(vsm_model, js_model)

    models = [vsm_model, lsi_model, js_model, lda_model, nmf_model,
              vsm_lda_model, js_lda_model, vsm_nmf_model, js_nmf_model, vsm_js_model]

    model_names = ['vsm', 'lsi', 'js', 'lda', 'nmf', 'vsmlda', 'jslda', 'vsmnmf', 'jsnmf', 'vsmjs']

    for i, model in enumerate(models):
        name = model_names[i]
        model.write_to_file('d_' + name + '_sim_' + corpus_code +'.tm', path='output_buffer/default_output/' + corpus_code + '/sim/')

    # Show precision recall curves
    evaluator1 = Evaluator(models[:5], corpus)
    evaluator2 = Evaluator(models[5:], corpus)

    evaluator1.precision_recall(filename='output_buffer/default_output/' + corpus_code + '/precision_recall_' + corpus_code + '.png')
    evaluator2.precision_recall(filename='output_buffer/default_output/' + corpus_code + '/orthogonal_precision_recall' + corpus_code + '.png')

def evaluate_association_model(corpus, experiment, inference_types=['VI', 'NUTS', 'MAP']):
    corpus_code = corpus.get_corpus_code()

    path = 'empirical/experiments/' + experiment[0] + '/' + experiment[1:] + '/'

    association_models = []
    for inf_type in inference_types:
        model = get_model_from_file('a_' + inf_type + '_' + corpus_code + '.tm', path=path)
        association_models.append(model)

    all_models = get_all_default_models_from_file(corpus_code)
    evaluator = Evaluator(all_models[0], corpus)
    best_ir = evaluator.get_best_model(models=all_models)
    best_ir.set_name(best_ir.get_name() + ' (Best IR)')

    models = association_models + [best_ir]
    evaluator = Evaluator(models, corpus)
    evaluator.roc_curve(show_parameters=True)

    evaluator = Evaluator(association_models, corpus)
    evaluator.precision_recall_variance(variance_models=all_models, show_parameters=True)

def analyze_parameters(corpus, model_type, parameter_trials=[5, 10, 20, 30, 40, 50]):
    if model_type == 'lda':
        model_generator = get_new_lda_model
    elif model_type == 'nmf':
        model_generator = get_new_nmf_model
    elif model_type == 'lsi':
        model_generator = get_new_lsi_model
    else:
        raise ValueError("Unrecognized model type: " + model_type)

    models = []
    for param in parameter_trials:
        models.append(model_generator(corpus, param))

    evaluator = get_new_evaluator_object(models, corpus)
    #return [evaluator.get_average_precision(model) for model in models]
    path = 'empirical/graphs/baseline/' + model_type + '_configs/'
    if not os.path.exists(path):
        os.makedirs(path)
    filename_base = model_type + '_' + corpus.get_corpus_code() + '_'
    evaluator.roc_curve(show_parameters=True, filename=path+filename_base+'roc.png')
    evaluator.precision_recall(show_parameters=True, filename=path+filename_base+'pr.png')

def analyze_parameters_orthogonal(corpus, model_A_type, model_B_type, parameter_trials=[5, 10, 20, 30, 40, 50]):

    if model_A_type == 'js':
        model_A = get_new_js_model(corpus)
    elif model_A_type == 'vsm':
        model_A = get_new_vsm_model(corpus)
    else:
        raise ValueError("Unrecognized static model type: " + model_A_type)

    if model_B_type == 'lda':
        model_B_generator = get_new_lda_model
    elif model_B_type == 'nmf':
        model_B_generator = get_new_nmf_model
    else:
        raise ValueError("Unrecognized parametrized model type: " + model_B_type)

    models = []
    for param in parameter_trials:
        models.append(get_new_orthogonal_model(corpus, model_A, model_B_generator(corpus, param)))

    evaluator = get_new_evaluator_object(models, corpus)
    #return [evaluator.get_average_precision(model) for model in models]
    path = 'empirical/graphs/baseline/' + model_A_type + model_B_type + '_configs/'
    if not os.path.exists(path):
        os.makedirs(path)
    filename_base = model_A_type + model_B_type + '_' + corpus.get_corpus_code() + '_'
    evaluator.roc_curve(show_parameters=True, keys={model_B_type.upper(): 'n_topics'}, filename=path+filename_base+'roc.png')
    evaluator.precision_recall(show_parameters=True, keys={model_B_type.upper(): 'n_topics'}, filename=path+filename_base+'pr.png')

def test_for_optimal_parameters(corpus_codes):
    corpora = [Corpus.get_preset_corpus(corpus_code) for corpus_code in corpus_codes]
    parameter_trials = [5, 10, 20, 30, 40, 50]
    model_types = ['lda', 'nmf']
    static_model_types = ['vsm', 'js']

    with open('model_parameters_orth.txt', 'w+') as output_file:
        for model_type in model_types:
            for static_model_type in static_model_types:
                observations = []
                for corpus in corpora:
                    observations.append(analyze_parameters_orthogonal(corpus, static_model_type, model_type, parameter_trials=parameter_trials))

                output_file.write(static_model_type + "+" + model_type + '\n')
                output_file.write("============\n")
                output_file.write("\tMean\t\tStdev\n")
                output_file.write("\t----\t\t-----\n")
                for i, param in enumerate(parameter_trials):
                    values = [observation[i] for observation in observations]
                    output_file.write(str(param) + "\t" + str(mean(values)) + "\t" + str(stdev(values)) + "\n")
                output_file.write('\n')
