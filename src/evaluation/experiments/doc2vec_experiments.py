import sys
sys.path.append('../..')

from evaluation.Corpus import Corpus
from evaluation.doc2vec import Doc2Vec_IR
from evaluation.Evaluator import Evaluator
from evaluation.VSM import VSM

corpus = Corpus.get_preset_corpus('0_1')
vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()

def vector_size_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model_0 = doc2vec_generator.generate_model(parameters={'vector_size': 200})
    doc2vec_model_1 = doc2vec_generator.generate_model(parameters={'vector_size': 300})
    doc2vec_model_2 = doc2vec_generator.generate_model(parameters={'vector_size': 400})

    evaluator = Evaluator([vsm_model, doc2vec_model_0, doc2vec_model_1, doc2vec_model_2], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def epochs_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model_0 = doc2vec_generator.generate_model(parameters={'epochs': 25})
    doc2vec_model_1 = doc2vec_generator.generate_model(parameters={'epochs': 50})
    doc2vec_model_2 = doc2vec_generator.generate_model(parameters={'epochs': 75})
    doc2vec_model_3 = doc2vec_generator.generate_model(parameters={'epochs': 100})

    evaluator = Evaluator([vsm_model, doc2vec_model_0, doc2vec_model_1, doc2vec_model_2, doc2vec_model_3], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def shared_vocab_test():
    doc2vec_generator0 = Doc2Vec_IR(corpus)
    doc2vec_generator1 = Doc2Vec_IR(corpus, only_common_vocab=False)
    doc2vec_model_0 = doc2vec_generator0.generate_model()
    doc2vec_model_1 = doc2vec_generator1.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model_0, doc2vec_model_1], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


def simple_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model = doc2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


simple_test()
#vector_size_test()
#epochs_test()
#shared_vocab_test()
#similarity_metric_test()