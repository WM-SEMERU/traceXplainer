
'''
Sandeep Kanchiraju, Jack Liegey
October 2019

'''
import sys
sys.path.append('../..')

import gensim.models as gm
from gensim.models import Word2Vec
from gensim.similarities import WmdSimilarity
from IR_Method import IR_Method

class Word2Vec_IR(IR_Method):
    """
    Implementation of word2vec as an IR method
    """

    def generate_model(self, parameters=None, subtitle=None):

        print("Generating new word2vec model")

        default_parameters = dict()
        default_parameters['sg'] = 1
        default_parameters['size'] = 100
        default_parameters['min_count'] = 2
        default_parameters['epochs'] = 40

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized word2vec parameter [" + str(key) + "]")

        parameters = default_parameters

        trace_model = self._new_model("word2vec: " + subtitle, parameters=parameters)

        w2v_corpus = self._processed_sources + self._processed_targets
        word2vec_model = Word2Vec(w2v_corpus, sg = parameters['sg'], size = parameters['size'],
                         min_count = parameters['min_count'],iter = parameters['epochs'])

        sources = trace_model.get_source_names()
        targets = trace_model.get_target_names()
        #print("hi :", len(targets))
        instance = WmdSimilarity(self._processed_targets, word2vec_model, num_best=len(targets))

        print("Populating trace models")
        for i in range(len(sources)):
            doc = self._processed_sources[i]
            similarity = instance[doc]
            for target_sim in similarity:
                j, similarity_score = target_sim
                source = sources[i]
                target = targets[j]
                #print("{} - {} : {}".format(source, target, similarity_score))
                trace_model.set_value(source, target, similarity_score)


        trace_model.set_default_threshold_technique('link_est')
        print("Done generating word2vec model")
        return trace_model
