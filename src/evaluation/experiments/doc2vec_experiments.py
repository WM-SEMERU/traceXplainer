import sys
sys.path.append('../..')

from evaluation.VSM import VSM
from evaluation.Evaluator import Evaluator
from evaluation.doc2vec import Doc2Vec_IR
from evaluation.Corpus import Corpus



corpus = Corpus.get_preset_corpus('1_1')
vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()


def vector_size_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model_0 = doc2vec_generator.generate_model(
        parameters={'vector_size': 200})
    doc2vec_model_1 = doc2vec_generator.generate_model(
        parameters={'vector_size': 300})
    doc2vec_model_2 = doc2vec_generator.generate_model(
        parameters={'vector_size': 400})

    evaluator = Evaluator(
        [vsm_model, doc2vec_model_0, doc2vec_model_1, doc2vec_model_2], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


def epochs_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model_0 = doc2vec_generator.generate_model(
        parameters={'epochs': 25})
    doc2vec_model_1 = doc2vec_generator.generate_model(
        parameters={'epochs': 50})
    doc2vec_model_2 = doc2vec_generator.generate_model(
        parameters={'epochs': 75})
    doc2vec_model_3 = doc2vec_generator.generate_model(
        parameters={'epochs': 100})

    evaluator = Evaluator([vsm_model, doc2vec_model_0,
                           doc2vec_model_1, doc2vec_model_2, doc2vec_model_3], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


def shared_vocab_test():
    doc2vec_generator0 = Doc2Vec_IR(corpus)
    doc2vec_generator1 = Doc2Vec_IR(corpus, only_common_vocab=False)
    doc2vec_model_0 = doc2vec_generator0.generate_model()
    doc2vec_model_1 = doc2vec_generator1.generate_model()

    evaluator = Evaluator(
        [vsm_model, doc2vec_model_0, doc2vec_model_1], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


def use_negative_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model_0 = doc2vec_generator.generate_model(
        parameters={'use_negatives': False})
    doc2ved_model_1 = doc2vec_generator.generate_model(
        parameters={'use_negatives': True})

    evaluator = Evaluator(
        [vsm_model, doc2vec_model_0, doc2ved_model_1], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def preprocessing_test():
    doc2vec_generator0 = Doc2Vec_IR(corpus, only_alphnum=False,
        only_alph=False,
        split_camel_case=False,
        split_snake_case=False,
        remove_stop_words=False,
        stem=False)
    doc2vec_generator1 = Doc2Vec_IR(corpus, only_alphnum=True,
        split_camel_case=True,
        split_snake_case=True,
        remove_stop_words=True,
        stem=True)
    doc2vec_model_0 = doc2vec_generator0.generate_model()
    doc2vec_model_1 = doc2vec_generator1.generate_model()
    evaluator = Evaluator(
        [vsm_model, doc2vec_model_0, doc2vec_model_1], corpus)
    evaluator.precision_recall(show_parameters=False, show_random_model=True)




def simple_test():
    doc2vec_generator = Doc2Vec_IR(corpus)
    doc2vec_model = doc2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def simple_test_bpe():
    doc2vec_generator = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=2000)
    doc2vec_model = doc2vec_generator.generate_model()

    # evaluator = Evaluator([vsm_model, doc2vec_model], corpus)
    # evaluator.precision_recall(show_parameters=True, show_random_model=True)

simple_test_bpe()
# simple_test()
# use_negative_test()
# preprocessing_test()
# vector_size_test()
# epochs_test()
# shared_vocab_test()
# similarity_metric_test()
