'''
Daniel McCrystal
June 2018

'''

from .Topic_Models import Topic_Models

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.decomposition import NMF as NonnegativeMatrixFactorization

class NMF(Topic_Models):
    """
    Implementation of the Non-negative Matrix Factorization IR method
    """

    def generate_model(self, parameters=None):
        """
        Arguments:
            parameters (dict of str: obj, optional): Optional parameter dictionary
                for computing NMF similarity. If no parameter dictionary is given,
                default values will be used. Below are the NMF specific parameters.

                'similarity_metric' (str) : 'divergence' (default) or 'euclidian'
                'n_topics' (int) : Number of topics, default=10
        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new NMF model")

        default_parameters = dict()
        default_parameters['similarity_metric'] = 'divergence'
        default_parameters['n_topics'] = 10

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized NMF parameter [" + str(key) + "]")

        parameters = default_parameters
        model = self._new_model("NMF", parameters=parameters)

        vectorizer = TfidfVectorizer()

        all_artifacts = self._processed_sources + self._processed_targets

        tfidf_matrix = vectorizer.fit_transform(all_artifacts)

        n_topics = parameters['n_topics']
        nmf_model = NonnegativeMatrixFactorization(n_components=n_topics).fit(tfidf_matrix)

        doc_topic_matrix = nmf_model.transform(tfidf_matrix)

        if parameters['similarity_metric'] == 'divergence':
            similarity_metric = self.divergence
        elif parameters['similarity_metric'] == 'euclidian':
            similarity_metric = self.euclidian_distance

        sources = model.get_source_names()
        targets = model.get_target_names()

        num_sources = len(sources)

        for i, source in enumerate(sources):
            for j, target in enumerate(targets):

                j += num_sources

                source_topic_vector = doc_topic_matrix[i]
                target_topic_vector = doc_topic_matrix[j]

                model.set_value(source, target, similarity_metric(source_topic_vector, target_topic_vector))

        model.set_default_threshold_technique('median')
        print("Done generating NMF model")
        return model
