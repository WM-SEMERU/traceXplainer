
'''
Sandeep Kanchiraju, Jack Liegey
October 2019

'''
import sys
sys.path.append('../..')

import gensim.models as gm
from gensim.models import Word2Vec
from gensim.similarities import WmdSimilarity
from .IR_Method import IR_Method

class Word2Vec_IR(IR_Method):
    """
    Implementation of word2vec as an IR method
    """

    def generate_model(self, parameters=None, subtitle=None):

        print("Generating new word2vec model")

        default_parameters = dict()
        default_parameters['use_pretrained_model'] = True
        default_parameters['fine_tune'] = True
        default_parameters['train_type'] = 'sg'
        default_parameters['vector_size'] = 300
        default_parameters['min_count'] = 2
        default_parameters['epochs'] = 40

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized word2vec parameter [" + str(key) + "]")

        parameters = default_parameters

        trace_model = self._new_model("word2vec" + (": {}".format(subtitle) if subtitle is not None else ""), parameters=parameters)

        if parameters['use_pretrained_model']:
            print("Loading pretrained word2vec model")
            word2vec_model = Word2Vec.load(
                '../../../data/pretrained_models/word2vec/w2v_{}_vectorSize{}_minCount{}_BPEvocabSize{}'.format(
                    parameters['train_type'],
                    parameters['vector_size'],
                    parameters['min_count'],
                    2000
                )
            )
            print("Done loading pretrained word2vec model")
            if parameters['fine_tune']:
                print("Fine tuning pretrained word2vec model to corpus")
                w2v_corpus = self._processed_sources + self._processed_targets
                word2vec_model.train(
                    w2v_corpus,
                    total_examples=len(w2v_corpus),
                    epochs=parameters['epochs']
                )
                print("Done fine tuning pretrained word2vec model to corpus")

        else:
            print("Training word2vec model on corpus")
            w2v_corpus = self._processed_sources + self._processed_targets
            word2vec_model = Word2Vec(
                w2v_corpus, 
                sg=1 if parameters['train_type'] == 'sg' else 0,
                size=parameters['vector_size'],
                min_count=parameters['min_count'],
                iter=parameters['epochs'])
            print("Done training word2vec model on corpus")


        sources = trace_model.get_source_names()
        targets = trace_model.get_target_names()

        instance = WmdSimilarity(self._processed_targets, word2vec_model, num_best=len(targets))

        print("Populating trace models")
        for i in range(len(sources)):
            doc = self._processed_sources[i]
            similarity = instance[doc]
            print("Populating traces for source {}/{}".format(i+1, len(sources)))
            for target_sim in similarity:
                j, similarity_score = target_sim
                source = sources[i]
                target = targets[j]
                # print("{} - {} : {}".format(source, target, similarity_score))
                trace_model.set_value(source, target, similarity_score / len(targets))

        trace_model.set_default_threshold_technique('link_est')
        print("Done generating word2vec model")
        return trace_model
