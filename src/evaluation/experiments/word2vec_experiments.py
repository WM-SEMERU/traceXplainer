import sys
sys.path.append('../..')

from evaluation.VSM import VSM
from evaluation.Evaluator import Evaluator
from evaluation.word2vec import Word2Vec_IR
from evaluation.Corpus import Corpus



corpus = Corpus.get_preset_corpus('1_1')
vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()

def simple_test_sg():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model = word2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, word2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def simple_test_cbow():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model = word2vec_generator.generate_model(
        parameters={'sg': 0})

    evaluator = Evaluator([vsm_model, word2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def vector_size_test_sg():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model_0 = word2vec_generator.generate_model(
        parameters={'size': 200})
    word2vec_model_1 = word2vec_generator.generate_model(
        parameters={'size': 300})
    word2vec_model_2 = word2vec_generator.generate_model(
        parameters={'size': 400})

    evaluator = Evaluator(
        [vsm_model, word2vec_model_0, word2vec_model_1, word2vec_model_2], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def vector_size_test_cbow():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model_0 = word2vec_generator.generate_model(
        parameters={'size': 200, 'sg': 0})
    word2vec_model_1 = word2vec_generator.generate_model(
        parameters={'size': 300, 'sg': 0})
    word2vec_model_2 = word2vec_generator.generate_model(
        parameters={'size': 400, 'sg': 0})

    evaluator = Evaluator(
        [vsm_model, word2vec_model_0, word2vec_model_1, word2vec_model_2], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def iter_test_sg():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model_0 = word2vec_generator.generate_model(
        parameters={'iter': 25})
    word2vec_model_1 = word2vec_generator.generate_model(
        parameters={'iter': 50})
    word2vec_model_2 = word2vec_generator.generate_model(
        parameters={'iter': 75})
    word2vec_model_3 = word2vec_generator.generate_model(
        parameters={'iter': 100})

    evaluator = Evaluator([vsm_model, word2vec_model_0,
                           word2vec_model_1, word2vec_model_2, word2vec_model_3], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def iter_test_cbow():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model_0 = word2vec_generator.generate_model(
        parameters={'iter': 25, 'sg': 0})
    word2vec_model_1 = word2vec_generator.generate_model(
        parameters={'iter': 50, 'sg': 0})
    word2vec_model_2 = word2vec_generator.generate_model(
        parameters={'iter': 75, 'sg': 0})
    word2vec_model_3 = word2vec_generator.generate_model(
        parameters={'iter': 100, 'sg': 0})

    evaluator = Evaluator([vsm_model, word2vec_model_0,
                           word2vec_model_1, word2vec_model_2, word2vec_model_3], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

def preprocessing_test_sb():
    word2vec_generator0 = Word2Vec_IR(corpus, only_alphnum=False,
        only_alph=False,
        split_camel_case=False,
        split_snake_case=False,
        remove_stop_words=False,
        stem=False)
    word2vec_generator1 = Word2Vec_IR(corpus, only_alphnum=True,
        split_camel_case=True,
        split_snake_case=True,
        remove_stop_words=True,
        stem=True)
    word2vec_model_0 = word2vec_generator0.generate_model()
    word2vec_model_1 = word2vec_generator1.generate_model()
    evaluator = Evaluator(
        [vsm_model, word2vec_model_0, word2vec_model_1], corpus)
    evaluator.precision_recall(show_parameters=False, show_random_model=True)

def preprocessing_test_cbow():
    word2vec_generator0 = Word2Vec_IR(corpus, only_alphnum=False,
        only_alph=False,
        split_camel_case=False,
        split_snake_case=False,
        remove_stop_words=False,
        stem=False)
    word2vec_generator1 = Word2Vec_IR(corpus, only_alphnum=True,
        split_camel_case=True,
        split_snake_case=True,
        remove_stop_words=True,
        stem=True)
    word2vec_model_0 = word2vec_generator0.generate_model(
        parameters={'sg':0})
    word2vec_model_1 = word2vec_generator1.generate_model(
        parameters={'sg':0})
    evaluator = Evaluator(
        [vsm_model, word2vec_model_0, word2vec_model_1], corpus)
    evaluator.precision_recall(show_parameters=False, show_random_model=True)

simple_test_sg()
simple_test_cbow()
vector_size_test_sg()
vector_size_test_cbow()
iter_test_sg()
iter_test_cbow()
preprocessing_test_sb()
preprocessing_test_cbow()
