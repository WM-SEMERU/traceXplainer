from evaluation.Corpus import Corpus
from evaluation.doc2vec import Doc2Vec_IR
from evaluation.Evaluator import Evaluator
from evaluation.VSM import VSM

corpus = Corpus.get_preset_corpus('3_0')

doc2vec_generator = Doc2Vec_IR(corpus)
doc2vec_model = doc2vec_generator.generate_model()

vsm_generator = VSM(corpus)
vsm_model = vsm_generator.generate_model()

evaluator = Evaluator([doc2vec_model, vsm_model], corpus)
evaluator.precision_recall(show_parameters=True, show_random_model=True)
