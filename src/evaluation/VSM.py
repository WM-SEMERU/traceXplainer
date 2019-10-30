'''
Daniel McCrystal
June 2018

'''
import sys
sys.path.append('../..')

from .IR_Method import IR_Method

from sklearn.feature_extraction.text import TfidfVectorizer

from numpy import dot
from numpy.linalg import norm

class VSM(IR_Method):
    """
    Implementation of Vector Space Model IR method
    """

    def generate_model(self, parameters=None):
        """
        Computes the similarity values using the VSM IR method.

        Arguments:
            parameters (dict of str:obj, optional): Optional parameter dictionary for
                computing VSM similarity. If no parameter dictionary is given, default
                values will be used. Below are the VSM specific parameters.

                'similarity_metric' (str) : 'cosine' (default) or 'euclidian'
                'smooth' (bool) : Determines whether or not to use Laplace smoothing

        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new VSM model")


        default_parameters = dict()
        default_parameters['similarity_metric'] = 'cosine'
        default_parameters['smooth'] = False

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized VSM parameter [" + str(key) + "]")

        parameters = default_parameters

        model = self._new_model("VSM", parameters=parameters)

        vectorizer = TfidfVectorizer(smooth_idf=parameters['smooth'])

        processed_sources = [' '.join(source) for source in self._processed_sources]
        processed_targets = [' '.join(target) for target in self._processed_targets]

        source_matrix = vectorizer.fit_transform(processed_sources).toarray()
        target_matrix = vectorizer.fit_transform(processed_targets).toarray()

        # Determine similarities
        def cosine_similarity(a, b):
            return dot(a, b) / (norm(a) * norm(b))

        def euclidian_similarity(a, b):
            return 1 / norm(a - b)

        if parameters['similarity_metric'] == 'cosine':
            similarity_metric = cosine_similarity
        else: # similarity_metric == 'e'
            similarity_metric = euclidian_similarity

        sources = model.get_source_names()
        targets = model.get_target_names()

        for i in range(len(source_matrix)):
            for j in range(len(target_matrix)):
                source = sources[i]
                target = targets[j]

                similarity = similarity_metric(source_matrix[i], target_matrix[j])
                #similarity -= 0.14614614614614618
                #similarity *= 50

                model.set_value(source, target, similarity)

        model.set_default_threshold_technique('link_est')
        print("Done generating VSM model")
        return model
