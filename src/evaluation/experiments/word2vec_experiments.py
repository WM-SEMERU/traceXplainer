import sys
sys.path.append('../..')

from evaluation.VSM import VSM
from evaluation.Evaluator import Evaluator
from evaluation.word2vec import Word2Vec_IR
from evaluation.Corpus import Corpus



corpus = Corpus.get_preset_corpus('1_1')
vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()

def simple_test():
    word2vec_generator = Word2Vec_IR(corpus)
    word2vec_model = word2vec_generator.generate_model()

    evaluator = Evaluator([vsm_model, word2vec_model], corpus)
    evaluator.precision_recall(show_parameters=True, show_random_model=True)

simple_test()
