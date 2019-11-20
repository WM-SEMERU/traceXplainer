'''
Daniel McCrystal
October 2019

'''

from .IR_Method import IR_Method
import gensim
import collections
import numpy as np
from numpy import dot
from numpy.linalg import norm


class Doc2Vec_IR(IR_Method):
    """
    Implementation of doc2vec as an IR method
    """

    def generate_model(self, parameters=None, subtitle=None):
        """
        Computes the similarity values using the doc2vec.

        Arguments:
            parameters (dict of str:obj, optional): Optional parameter dictionary for
                computing doc2vec similarity. If no parameter dictionary is given, default
                values will be used. Below are the doc2vec specific parameters.

                'similarity_metric' (str) : 'most_similar' (default), 'cosine'
                'vector_size' (int) : the number of dimensions of the output vector (default: 100)
                'min_count' (int) : (default: 2)
                'epochs' (int) : (default: 40)
                'use_negatives' (bool) : (default: false)

        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new doc2vec model")

        default_parameters = dict()
        default_parameters['similarity_metric'] = 'most_similar'
        default_parameters['vector_size'] = 100
        default_parameters['min_count'] = 2
        default_parameters['epochs'] = 40
        default_parameters['use_negatives'] = False

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print(
                        "Ignoring unrecognized doc2vec parameter [" + str(key) + "]")

        parameters = default_parameters
        
        trace_model = self._new_model("doc2vec: " + subtitle, parameters=parameters)

        self.doc_index_map = {}
        train_corpus = list(self.tag_artifacts(
            self._processed_sources, self._processed_targets))
        doc2vec_model = gensim.models.doc2vec.Doc2Vec(
            vector_size=parameters['vector_size'],
            min_count=parameters['min_count'],
            epochs=parameters['epochs']
        )
        doc2vec_model.build_vocab(train_corpus)
        print("Training doc2vec model")
        doc2vec_model.train(
            train_corpus,
            total_examples=doc2vec_model.corpus_count,
            epochs=doc2vec_model.epochs
        )
        print("Done training")

        similarity_matrix = [None for i in range(len(self._processed_sources))]

        def most_similar(doc_a_index, doc_b_index):
            return similarity_matrix[doc_a_index][doc_b_index]

        if parameters['similarity_metric'] == 'cosine':
            raise ValueError
        elif parameters['similarity_metric'] == 'most_similar':
            similarity_metric = most_similar

            source_embedding_matrix = [doc2vec_model.infer_vector(
                doc) for doc in self._processed_sources]
            for source_index in range(len(source_embedding_matrix)):
                source_vector = source_embedding_matrix[source_index]
                most_similar_docs = doc2vec_model.docvecs.most_similar(
                    positive=[source_vector],
                    negative=source_embedding_matrix[:source_index] +
                    source_embedding_matrix[source_index +
                                            1:] if parameters['use_negatives'] else [],
                    topn=len(doc2vec_model.docvecs),
                )
                similarity_matrix[source_index] = [
                    -np.inf for k in range(len(self._processed_targets))]
                for doc_index, sim in most_similar_docs:
                    if doc_index >= len(self._processed_sources):
                        target_index = doc_index - len(self._processed_sources)
                        #print("{} - {}".format(target_names[target_index], self.doc_index_map[doc_index]))
                        similarity_matrix[source_index][target_index] = sim

        else:
            raise ValueError

        sources = trace_model.get_source_names()
        targets = trace_model.get_target_names()

        print("Populating trace models")
        for i in range(len(sources)):
            for j in range(len(targets)):
                source = sources[i]
                target = targets[j]

                #similarity = similarity_metric(self._processed_sources[i], self._processed_targets[j])
                similarity = similarity_metric(i, j)
                #print("{} - {} : {}".format(source, target, similarity))

                trace_model.set_value(source, target, similarity)

        trace_model.set_default_threshold_technique('link_est')
        print("Done generating doc2vec model")
        return trace_model

    # Function to get the corpus in the format that doc2vec accepts
    def tag_artifacts(self, source_artifacts, target_artifacts):
        source_names = self._corpus.get_source_names()
        target_names = self._corpus.get_target_names()

        print("len source names: {}".format(len(source_names)))
        print("len source artifacts: {}".format(len(source_artifacts)))

        print("len target names: {}".format(len(target_names)))
        print("len target artifacts: {}".format(len(target_artifacts)))

        for i, artifact in enumerate(source_artifacts + target_artifacts):

            # For training data, add tags
            if i < len(source_names):
                self.doc_index_map[i] = source_names[i]
            else:
                self.doc_index_map[i] = target_names[i-len(source_names)]
            yield gensim.models.doc2vec.TaggedDocument(artifact, [i])
