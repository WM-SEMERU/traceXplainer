'''
Daniel McCrystal
June 2018

'''
from .IR_Method import IR_Method

from sklearn.decomposition import TruncatedSVD
from sklearn.feature_extraction.text import CountVectorizer

from sklearn.preprocessing import Normalizer

import numpy as np

class LSI(IR_Method):
    """
    Implementation of Latent Semantic Indexing IR Method
    """

    def generate_model(self, parameters=None):
        """
        Computes the similarity values using the VSM IR method.

        Arguments:
            parameters (dict of str:obj, optional): Optional parameter dictionary for
                computing LSI similarity. If no parameter dictionary is given, default
                values will be used. Below are the LSI specific parameters.

                'n_components' (int or str) : Desired dimensionality, or 'max' for
                    highest possible dimensionality. Must be less than
                    min(total # artifacts, # unique terms). Default is highest
                    possible dimensionality

        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new LSI model")

        default_parameters = dict()
        default_parameters['n_components'] = 'max'

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized LSI parameter [" + str(key) + "]")

        parameters = default_parameters

        vectorizer = CountVectorizer()

        all_artifacts = self._processed_sources + self._processed_targets

        dtm = vectorizer.fit_transform(all_artifacts).astype('d')

        n_components = min(dtm.shape) - 1

        if parameters['n_components'] == 'max':
            parameters['n_components'] = str(n_components) + ' (max)'
        else:
            n_components = parameters['n_components']

        model = self._new_model("LSI", parameters=parameters)

        lsi = TruncatedSVD(n_components, algorithm='arpack')

        dtm_lsi = lsi.fit_transform(dtm)
        dtm_lsi = Normalizer(copy=False).fit_transform(dtm_lsi)

        similarity_matrix = np.asarray(np.asmatrix(dtm_lsi) * np.asmatrix(dtm_lsi).T)

        sources = model.get_source_names()
        targets = model.get_target_names()

        num_sources = len(sources)

        for i, source in enumerate(sources):
            for j, target in enumerate(targets):

                j += num_sources

                similarity = similarity_matrix[i][j]
                #similarity -= 0.14614614614614618
                #similarity *= 50

                model.set_value(source, target, similarity)

        model.set_default_threshold_technique('link_est')
        print("Done generating LSI model")
        return model
