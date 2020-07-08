'''
Daniel McCrystal
July 2018

'''

import sys
corpus_code = sys.argv[1]
segment_code = sys.argv[2]
compile_bin = sys.argv[3]
experiment_number = sys.argv[4]
config = sys.argv[5]

sys.path.append('./')

import os
unique_compiledir = 'base_compiledir=/scratch/drmccr/.theano/' + corpus_code + '/' + str(compile_bin)
os.environ['THEANO_FLAGS']=unique_compiledir

import subprocess
import atexit
def clean_compiledir():
    print("Exiting subprocess: " + corpus_code + "/" + segment_code)
atexit.register(clean_compiledir)

from facade import get_trace_models_with_ltr, get_all_models_from_folder
from ir.Evaluator import Evaluator
import experiments

print("Starting subprocess: " + corpus_code + "/" + segment_code)

segment_dir = corpus_code + '/' + config + '/' + segment_code + '/'

similarities = get_all_models_from_folder('empirical/buffer/' + segment_dir)

if experiment_number == '1':
    association_models = experiments.experiment_1(similarities)
elif experiment_number == '2':
    association_models = experiments.experiment_2(similarities, corpus_code, "random", float(config))
elif experiment_number == '3':
    association_models = experiments.experiment_3(similarities, corpus_code)
elif experiment_number == '4':
    association_models = experiments.experiment_4(similarities, corpus_code)
elif experiment_number == '5':
    association_models = experiments.experiment_5(similarities, corpus_code)

for model in association_models:
    old_name = model.get_name()
    model.set_name(old_name + '~[' + segment_code + ']')
    model.write_to_file(model.get_name() + '.tm', 'empirical/buffer/' + corpus_code + '/' + config + '/' + old_name + '~/')
