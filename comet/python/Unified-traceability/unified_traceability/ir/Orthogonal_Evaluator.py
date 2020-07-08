'''
Daniel McCrystal
June 2018

'''

from .Orthogonal_IR import Orthogonal_IR

from sklearn.metrics import average_precision_score

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

class Orthogonal_Evaluator:

    def __init__(self, method_pairs, corpus):
        if type(method_pairs) is list:
            if len(method_pairs) < 1:
                raise ValueError("You must evaluate at least one orthogonal model")

            if len(method_pairs) > 6:
                raise ValueError("Too many models to plot simultaneously")

        else:
            method_pairs = [method_pairs]

        self._method_pairs = method_pairs
        self._corpus = corpus

        sources = self._corpus.get_source_names()
        targets = self._corpus.get_target_names()
        self._y_true = np.array([corpus.get_truth_value(source, target) for source in sources for target in targets])

    def get_average_precision(self, model):
        return average_precision_score(self._y_true, self._get_y_scores(model))

    def _get_y_scores(self, model):
        sources = self._corpus.get_source_names()
        targets = self._corpus.get_target_names()
        return np.array([model.get_value(source, target) for source in sources for target in targets])

    def lambda_ap_curve(self, granularity=20):
        delta = 0.9 / (granularity - 1)
        lambda_vals = [0.05 + (delta * x) for x in range(granularity)]

        colors = 'brgymc'
        handles = []

        for i, pair in enumerate(self._method_pairs):
            model_A = pair[0]
            model_B = pair[1]

            orth = Orthogonal_IR(self._corpus)
            models = orth.generate_model(model_A, model_B, parameters={'lambda':lambda_vals})

            aps = []
            for model in models:
                aps.append(self.get_average_precision(model))

            plt.plot(lambda_vals, aps, '-o', color=colors[i], alpha=0.8)

            handles.append(mpatches.Patch(color=colors[i], label=models[0].get_name()))

        plt.xlabel('Lambda')
        plt.ylabel('Average Precision')
        plt.ylim([0.0, 1.0])
        plt.xlim([0.0, 1.0])
        plt.title('Lambda vs AP Curve for \'' + self._corpus.get_corpus_name() + '\'')

        plt.legend(handles=handles)
        plt.show()
