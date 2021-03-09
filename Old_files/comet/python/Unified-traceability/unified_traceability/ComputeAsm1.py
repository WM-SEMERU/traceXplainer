
import sys
sys.path.append("..")

from facade import *

files = sys.argv[1].split(",")
path = sys.argv[2]
techniques = []

for model in files:
	techniques.append(get_model_from_file(model, path))

association_models = get_trace_models_with_ltr(complexity=1, inference_types=['VI'], similarities=techniques)
for model in association_models:
	model.write_to_file(model.get_name() + '.tm', path)