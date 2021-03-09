
import sys
sys.path.append("..")

from facade import *

pairs = sys.argv[1].split("#")
files = sys.argv[2].split(",")
path = sys.argv[3]

link_subset = []
dic = {}
for link in pairs:
	splitLink = link.split("::")
	source = splitLink[0]
	target = splitLink[1]
	feedback = float(splitLink[2])
	isThere = dic.get(source)

	if(isThere):
		dic[source][target] = feedback
	else:
		dic[source]= {target:feedback}

	link_subset.append((source, target))

techniques = []

for model in files:
	techniques.append(get_model_from_file(model, path))

association_models = get_trace_models_with_ltr(complexity=2, similarities=techniques, domain_conf_dict=dic, link_subset=link_subset)
for model in association_models:
	model.write_to_file(model.get_name() + '.tm', path)