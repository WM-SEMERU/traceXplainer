'''
Daniel McCrystal
June 2018

'''

from .Topic_Models import Topic_Models

from statistics import *
from statsmodels import robust

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.decomposition import LatentDirichletAllocation

class LDA(Topic_Models):
    """
    Implementation of Latent Dirichlet Allocation (Topic Modeling)
    """

    def generate_model(self, n_trials=30, parameters=None):
        """
        Arguments:
            parameters (dict of str: obj, optional): Optional parameter dictionary
                for computing LDA similarity. If no parameter dictionary is given,
                default values will be used. Below are the LDA specific parameters.

                'similarity_metric' (str) : 'hellinger' (default) or 'euclidian'
                'n_topics' (int) : Number of topics, default=10

        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new LDA model")

        default_parameters = dict()
        default_parameters['similarity_metric'] = 'hellinger'
        default_parameters['n_topics'] = 10

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized LDA parameter [" + str(key) + "]")

        parameters = default_parameters

        model = self._new_model("LDA", parameters=parameters)

        all_artifacts = self._processed_sources + self._processed_targets

        vectorizer = CountVectorizer()
        tf_matrix = vectorizer.fit_transform(all_artifacts)

        num_docs = len(all_artifacts)
        num_topics = parameters['n_topics']

        trials = []
        for n in range(n_trials):
            lda_model = LatentDirichletAllocation(n_topics=num_topics, learning_method='online').fit(tf_matrix)
            doc_topic_matrix = lda_model.transform(tf_matrix)
            trials.append(doc_topic_matrix)

        if parameters['similarity_metric'] == 'hellinger':
            similarity_metric = self.hellinger_distance
        elif parameters['similarity_metric'] == 'euclidian':
            similarity_metric = self.euclidian_distance
        elif parameters['similarity_metric'] == 'inverse':
            similarity_metric = self.inverse_euclidian

        sources = model.get_source_names()
        targets = model.get_target_names()

        num_sources = len(sources)

        for i, source in enumerate(sources):
            for j, target in enumerate(targets):

                j += num_sources

                similarities = []
                for n in range(n_trials):
                    source_topic_vector = trials[n][i]
                    target_topic_vector = trials[n][j]

                    similarity = similarity_metric(source_topic_vector, target_topic_vector)
                    #similarity -= 0.91991991991992
                    #similarity *= 50
                    similarities.append(similarity)

                model.set_value(source, target, median(similarities))

        model.set_default_threshold_technique('min-max')
        print("Done generating LDA model")
        return model
