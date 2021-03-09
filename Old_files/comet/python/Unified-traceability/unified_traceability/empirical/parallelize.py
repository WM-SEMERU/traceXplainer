'''
Daniel McCrystal
July 2018

'''

PROCESSES = []
buffer_folder = 'empirical/buffer/'

import subprocess
import atexit
def clean_sub_processes():
    print("Cleaning sub processes")
    for p in PROCESSES:
        if p is not None:
            p.kill()
    #subprocess.call("rm -rf " + buffer_folder, shell=True)
    subprocess.call(["echo", "done"])
atexit.register(clean_sub_processes)

import sys
sys.path.append('./')


from facade import *
from ir.Trace_Model import Trace_Model
from ir.Corpus import Corpus
import empirical.experiments


import traceback

def do_process(corpus_code, num_threads, experiment_number, config, association_models=None, sample=False, sampling_ratio=None, sampling_number=None):

    if association_models is None:
        association_models = ['VI', 'NUTS', 'MAP']

    try:
        models = get_all_default_models_from_file(corpus_code)

        if sample:
            sampled_links = list(models[0].get_sampled_model(ratio=sampling_ratio, num_links=sampling_number).get_links())

        for model in models:
            normalized_dict, normalized_threshold = model.get_normalized_values_with_sigmoid(include_threshold=True)
            for source, target in model.get_links():
                model.set_value(source, target, normalized_dict[source][target])
            model.set_default_threshold(normalized_threshold)

            if sample:
                model = model.get_sampled_model(links=sampled_links)

            broken_down_models = model.get_broken_down()
            for i, bdm in enumerate(broken_down_models):
                bdm.write_to_file(bdm.get_name() + '.tm', buffer_folder + corpus_code + '/' + config + '/' + str(i) + '/')

        num_segments = len(broken_down_models)
        first_process = 0

        for i in range(first_process):
            PROCESSES.append(None)
        last_process = first_process - 1
        num_processes_running = 0
        while last_process < num_segments - 1:
            if num_processes_running < num_threads:
                num_processes_running += 1
            else:
                print("Waiting for process [" + str(first_process) + "] to complete")
                PROCESSES[first_process].wait()
                first_process += 1

            last_process += 1
            print("Attempting to start process [" + str(last_process) + "]")
            p = subprocess.Popen(["python", "empirical/sub_process.py", corpus_code, str(last_process), str(last_process % num_threads), str(experiment_number), config])
            PROCESSES.append(p)

        for p in PROCESSES:
            p.wait()

        for model in association_models:
            #fragment_models = get_all_models_from_folder(buffer_folder + corpus_code + '/' + config + '/' + model + '~/')
            #merged_model = Trace_Model.merge_models(fragment_models)
            Trace_Model.merge_sampled_models_to_file(buffer_folder + corpus_code + '/' + config + '/' + 'MAP' + '~/','output_buffer/association_output/' + corpus_code + '/' + 'MAP.tm')
            Trace_Model.merge_sampled_models_to_file(buffer_folder + corpus_code + '/' + config + '/' + 'NUTS' + '~/','output_buffer/association_output/' + corpus_code + '/' + 'NUTS.tm')
            #merged_model.write_to_file('a_' + model + '_' + corpus_code + '.tm', path='output_buffer/association_output/' + corpus_code + '/')

    except:
        #print(sys.exc_info())
        traceback.print_exc()
        #clean_sub_processes()
