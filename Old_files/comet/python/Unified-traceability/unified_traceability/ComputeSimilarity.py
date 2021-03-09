
import sys
sys.path.append("..")

from facade import *

'''
For reference, this is the directory structure of the LibEST datasets. Other datasets in this project follow a consistent structure.

LibEST_semeru_format/
    requirements/
        RQ1.txt
        RQ2.txt
        ...
    source_code/
        est_client_http.c
        est_client_proxy.c
        ...
    test/
        us748.c
        us893.c
        ...
    req_to_code_ground.txt
    req_to_test_ground.txt

'''

# The name of the dataset
dataset = sys.argv[1]#'LibEST'

# Where the folder containing the dataset is located, releative to this file
corpus_root = ''#'../../../../datasets/LibEST_semeru_format/'

# The path from the corpus root to the file or folder containing the source artifacts. In this case, requirements.
source_path = sys.argv[2]#'requirements'

# The path from the corpus root to the file or folder containing the target artifacts. In this case, source_code.
target_path = sys.argv[3]#'source_code'

# The path from the corpus root to the file containing the ground truth.
#truth_path = sys.argv[4]#'req_to_code_ground.txt'

# Optional corpus parameters:

# The natural and programming languages used in this dataset. For each language, the matching stop words list and/or stemmer will be used, if it exists.
languages = ['english']
languages.append(sys.argv[4])
# Specific filetypes in the dataset can be whitelisted or blacklisted, but it is not necessary in this example because we want to consider all files.
# filetype_whitelist = ['txt', 'h', 'c']
# filetype_blacklist = None

# Get the list of techniques separated by a coma
techniques = sys.argv[5]
# Get the output path for the models
output_path = sys.argv[6]

corpus = get_new_corpus_object(dataset, corpus_root, source_path, target_path, truth_path=None, languages=languages, filetype_whitelist=None, filetype_blacklist=None)

# Get generate models using corpus
models = []
vsm_model = None
lsi_model = None
js_model = None
lda_model = None
nmf_model = None

# VSM optional parameters:
#   similarity_metric ('cosine' or 'euclidian'), default='cosine'
#   smooth (bool), default=False
if "vsm" in techniques:
    vsm_model = get_new_vsm_model(corpus)
    vsm_model.write_to_file("vsm_model.tm",output_path)
    models.append(vsm_model)

# LSI optional parameters:
#   n_components (int or 'max'), default='max'
if "lsi" in techniques:
    lsi_model = get_new_lsi_model(corpus)
    lsi_model.write_to_file("lsi_model.tm",output_path)
    models.append(lsi_model)

if "js" in techniques:
    js_model = get_new_js_model(corpus)
    js_model.write_to_file("js_model.tm",output_path)
    models.append(js_model)

# LDA optional parameters:
#   similarity_metric ('hellinger' or 'euclidian'), default='hellinger'
#   n_topics (int), default=10
if "lda" in techniques:
    lda_model = get_new_lda_model(corpus)
    lda_model.write_to_file("lda_model.tm",output_path)
    models.append(lda_model)

# NMF optional parameters:
#   similarity_metric ('divergence' or 'euclidian'), default='divergence'
#   n_topics (int), default=10
if "nmf" in techniques:
    nmf_model = get_new_nmf_model(corpus)
    nmf_model.write_to_file("nmf_model.tm",output_path)
    models.append(nmf_model)

if "vsm_lda" in techniques:
    vsm_lda = get_new_orthogonal_model(corpus, vsm_model, lda_model)
    vsm_lda.write_to_file("vsm_lda.tm",output_path)
    models.append(vsm_lda)

if "vsm_js" in techniques:
    vsm_js = get_new_orthogonal_model(corpus, vsm_model, js_model)
    vsm_js.write_to_file("vsm_js.tm",output_path)
    models.append(vsm_js)

if "vsm_nmf" in techniques:
    vsm_nmf = get_new_orthogonal_model(corpus, vsm_model, nmf_model)
    vsm_nmf.write_to_file("vsm_nmf.tm",output_path)
    models.append(vsm_nmf)

if "js_lda" in techniques:
    js_lda = get_new_orthogonal_model(corpus, js_model, lda_model)
    js_lda.write_to_file("js_lda.tm",output_path)
    models.append(js_lda)

if "js_nmf" in techniques:
    js_nmf = get_new_orthogonal_model(corpus, js_model, nmf_model)
    js_nmf.write_to_file("js_nmf.tm",output_path)
    models.append(js_nmf)

# You can combine two IR models to create an orthogonal model
#js_lda_model = get_new_orthogonal_model(corpus, js_model, lda_model, lambda_value=0.5)

# If the similarities already exist in a file, you can get a model instance without needing to recompute anything. Example:

# filename = 'vsm_sim.txt'
# name = 'VSM'
# corpus_name = 'LibEST'
# path = 'path/to/folder/' (relative to this file)
# vsm_model = get_model_from_file(filename, name, corpus_name, path)

# Once you have all the models, you can create an evaluator object
#models = [models]
##evaluator = get_new_evaluator_object(models, corpus)

# The evaluator object can perform several types of analyses.
# This example will produce a precision recall curve.
##evaluator.precision_recall()

# To save the plot to file instead of displaying, pass in a filename. Example:
# evaluator.precision_recall(filename='image.png')

# We can also see how different threshold generation techniques perform
##evaluator.compare_threshold_techniques()
