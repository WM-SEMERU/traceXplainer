import sys
sys.path.append('../..')

from evaluation.Corpus import Corpus
from evaluation.doc2vec import Doc2Vec_IR
from evaluation.Evaluator import Evaluator
from evaluation.VSM import VSM
from evaluation.Preprocessor import Preprocessor


corpus = Corpus.get_preset_corpus('0_1')
preprocessor = Preprocessor.get_default_preprocessor_instance()
vsm_generator = VSM(corpus, preprocessor)
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
    doc2vec_generator = Doc2Vec_IR(corpus, preprocessor)
    doc2vec_model = doc2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)


def simple_test_bpe():
    doc2vec_generator = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=2000)
    doc2vec_model = doc2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def bpe_vocab_test():
    doc2vec_generator0 = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=1000)
    doc2vec_generator1 = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=2000)
    doc2vec_generator2 = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=3000)
    doc2vec_generator3 = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=4000)
    doc2vec_generator4 = Doc2Vec_IR(corpus, bpe=True, bpe_vocab_size=5000)
    
    doc2vec_model0 = doc2vec_generator0.generate_model()
    doc2vec_model1 = doc2vec_generator1.generate_model()
    doc2vec_model2 = doc2vec_generator2.generate_model()
    doc2vec_model3 = doc2vec_generator3.generate_model()
    doc2vec_model4 = doc2vec_generator4.generate_model()

    evaluator = Evaluator([vsm_model, doc2vec_model0, doc2vec_model1, doc2vec_model2, doc2vec_model3, doc2vec_model4], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def pretrained_test():
    doc2vec_generator = Doc2Vec_IR(corpus, bpe=True)
    doc2vec_model = doc2vec_generator.generate_model(parameters={'use_negatives': False})
    doc2vec_model2 = doc2vec_generator.generate_model(parameters={'use_negatives': True})

    evaluator = Evaluator([vsm_model, doc2vec_model, doc2vec_model2], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

# pretrained_test()
#simple_test_bpe()
# bpe_vocab_test()
simple_test()
# use_negative_test()
# preprocessing_test()
# vector_size_test()
# epochs_test()
# shared_vocab_test()
# similarity_metric_test()
