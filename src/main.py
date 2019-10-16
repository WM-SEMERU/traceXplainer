from evaluation.Corpus import Corpus
from evaluation.doc2vec import Doc2Vec_IR
from evaluation.Evaluator import Evaluator
from evaluation.VSM import VSM

corpus = Corpus.get_preset_corpus('0_1')

doc2vec_generator0 = Doc2Vec_IR(corpus)
doc2vec_generator1 = Doc2Vec_IR(corpus, only_common_vocab=False)
doc2vec_model_0 = doc2vec_generator0.generate_model()
doc2vec_model_1 = doc2vec_generator1.generate_model()


vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()

evaluator = Evaluator([vsm_model, doc2vec_model_0, doc2vec_model_1], corpus)
evaluator.precision_recall(show_parameters=True, show_random_model=True)
