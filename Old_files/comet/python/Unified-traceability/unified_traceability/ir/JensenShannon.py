'''
Daniel McCrystal
June 2018

'''

from .IR_Method import IR_Method

from sklearn.feature_extraction.text import CountVectorizer
from scipy.stats import entropy
import numpy as np

class JensenShannon(IR_Method):
    """
    Implementation of Jensen-Shannon Divergence IR method
    """

    def generate_model(self, parameters=None):

        print("Generating new Jensen-Shannon model")

        if parameters is not None:
            print("Jensen-Shannon takes no parameters, ignoring provided parameters")

        model = self._new_model("JS")

        vectorizer = CountVectorizer()

        source_matrix = vectorizer.fit_transform(self._processed_sources)
        target_matrix = vectorizer.fit_transform(self._processed_targets)

        source_array = source_matrix.toarray().astype(float)
        for vector in source_array:
            vec_sum = vector.sum()
            for i in range(len(vector)):
                vector[i] /= vec_sum

        target_array = target_matrix.toarray().astype(float)
        for vector in target_array:
            vec_sum = vector.sum()
            for i in range(len(vector)):
                vector[i] /= vec_sum

        sources = model.get_source_names()
        targets = model.get_target_names()

        for i, source in enumerate(sources):
            for j, target in enumerate(targets):

                source_vector = source_array[i]
                target_vector = target_array[j]

                m = (source_vector + target_vector) / 2
                similarity = 1 - ((entropy(source_vector, m) + entropy(target_vector, m)) / 2)
                #similarity -= 0.5225225225225225
                #similarity *= 50

                model.set_value(source, target, similarity)

        model.set_default_threshold_technique('min-max')
        print("Done generating Jensen-Shannon model")
        return model
