'''
Daniel McCrystal
June 2018

'''

import sys, os
sys.path.append(os.path.abspath('../utils'))
from utils import normalization

from .Random_Linker import Random_Linker
import pandas as pd
import numpy as np
#from .DistributionApproximator import DistributionApproximator

from .Trace_Model import Trace_Model

from sklearn.metrics import precision_recall_curve
from sklearn.metrics import average_precision_score
from sklearn.metrics import roc_curve
from sklearn.metrics import roc_auc_score

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

from statistics import *
import random

class Evaluator:
    """
    Holds one or more models in order to evaluate and compare them using different graphing methods.
    Up to 5 models can be compared at once.

    Attributes:
        _models (list of Trace_Model): The models to be evaluated and compared
        _corpus (Corpus): The corpus object that all models are derived from
    """

    def __init__(self, models, corpus):
        """
        Args:
            models ((list of Trace_Model) or Trace_Model): the model or models to be evaluated.
                If an Trace_Model object is passed in, it will be converted into a singleton list.
            corpus (Corpus): The corpus object that all models are derived from
        """

        if type(models) is list:
            if len(models) < 1:
                raise ValueError("You must evaluate at least one model")

            if len(models) > 7:
                raise ValueError("Too many models to plot simultaneously (" + str(len(models)) + ")")

            for model in models:
                if model.get_corpus_name() != corpus.get_corpus_name():
                    raise ValueError("All models must be derived from the given corpus ["
                        + corpus.get_corpus_name() + "]")

        else:
            models = [models]

        self._models = models
        self._corpus = corpus
        
       

        if self._corpus.get_truth_dict() is None:
            raise ValueError("Cannot create evaluator object with corpus with no ground truth.")

        rl = Random_Linker(corpus)
        self._random_model = rl.generate_model()

        links = models[0].get_links()
        self._link_order = [link for link in links]
        self._y_true = np.array([corpus.get_truth_value(source, target) for source, target in self._link_order])

    def get_best_model(self, models=None):
        if models is None:
            models = self._models

        best_ap = -1
        best_model = None
        for model in models:
            ap = self.get_average_precision(model)
            if ap > best_ap:
                best_ap = ap
                best_model = model
        return best_model

    @classmethod
    def generate_mock_model_from_medians(cls, models):
        sources = models[0].get_source_names()
        targets = models[0].get_target_names()

        median_model = Trace_Model('Using Median Similarities', models[0].get_corpus_name(), sources, targets)

        for source, target in models[0].get_links():
            similarites = [model.get_value(source, target) for model in models]
            median_model.set_value(source, target, median(similarites))

        return median_model

    def get_average_precision(self, model):
        return average_precision_score(self._y_true, self._get_y_scores(model))

    def print_average_precision_report(self):
        for model in self._models + [self._random_model]:
            print("AP: {} -> {}".format(self.get_average_precision(model), model.get_name()))

    def get_auc(self, model):
        return roc_auc_score(self._y_true, self._get_y_scores(model))

    def _get_y_scores(self, model):
        return np.array([model.get_value(source, target) for source, target in self._link_order])
        

    def precision_recall(self, show_parameters=False, keys=None, show_random_model=False, filename=None, existing_handles=None, subtitle=None):
        """
        Generates and displays a precision-recall curve for the models.

        Returns:
            None
        """

        corpus_name = self._corpus.get_corpus_name()
        

        print("Generating precision-recall curve for models derived from \'" + corpus_name + "\'")

        colors = 'brgymck'

        if existing_handles is not None:
            handles = existing_handles
        else:
            handles = []

        models = self._models
        if show_random_model:
            colors = 'k' + colors
            models.insert(0, self._random_model)

        for i, model in enumerate(models):

            y_scores = self._get_y_scores(model)

            average_precision = self.get_average_precision(model)

            precision, recall, thresholds = precision_recall_curve(self._y_true, y_scores)

            plt.step(recall, precision, '-', color=colors[i], alpha=0.8, where='post')

            ap = '{0:0.2f}'.format(average_precision)

            name = model.get_name()
            if show_parameters:
                name += ' ' + model.get_parameters(keys=keys)
            handles.append(mpatches.Patch(color=colors[i], label=name+" [AP="+ap+"]"))

        plt.xlabel('Recall')
        plt.ylabel('Precision')
        plt.ylim([0.0, 1.05])
        plt.xlim([0.0, 1.0])
        
  
        plt.suptitle('2-class Precision-Recall Curve for \'' + self._corpus.get_corpus_name() + '\'')
        if subtitle is not None:
            plt.title('{}'.format(subtitle))


        plt.legend(handles=handles)
        fig = plt.gcf()
        fig.set_size_inches(12, 8, forward=True)

        if filename is not None:
            plt.savefig(filename, dpi=100)
        else:
            plt.show()

    def precision_recall_variance(self, variance_models, show_parameters=False, output_values=False):

        model_values = []

        global_unique_recall_points = set()

        for model in variance_models:
            model_dict = dict()

            precision, recall, thresholds = precision_recall_curve(self._y_true, self._get_y_scores(model))

            unique_recall_points = []
            precisions = []

            last_recall = -1
            stacked_precision_values = None
            for i in range(len(recall)):
                if recall[i] != last_recall:

                    unique_recall_points.append(recall[i])
                    global_unique_recall_points.add(recall[i])
                    last_recall = recall[i]

                    if stacked_precision_values is not None:
                        precisions.append(stacked_precision_values)

                    stacked_precision_values = []

                stacked_precision_values.append(precision[i])

            precisions.append(stacked_precision_values)

            model_dict['recall'] = unique_recall_points
            model_dict['precision'] = precisions

            model_values.append(model_dict)

        global_unique_recall_points = sorted(global_unique_recall_points, reverse=True)
        all_precision_values_per_recall = []
        indeces = [0 for x in model_values]

        for i in range(len(global_unique_recall_points)):
            precision_values = []

            recall = global_unique_recall_points[i]

            for m in range(len(model_values)):
                if model_values[m]['recall'][indeces[m]] > recall:
                    indeces[m] += 1

                precision_values.append(median(model_values[m]['precision'][indeces[m]]))

            all_precision_values_per_recall.append(precision_values)

        minimums = [min(all_precision_values_per_recall[i]) for i in range(len(global_unique_recall_points))]
        maximums = [max(all_precision_values_per_recall[i]) for i in range(len(global_unique_recall_points))]
        medians = [median(all_precision_values_per_recall[i]) for i in range(len(global_unique_recall_points))]

        if output_values:
            filepath = 'empirical/precision_outputs/' + self._corpus.get_corpus_code() + '/'
            if not os.path.exists(filepath):
                os.makedirs(filepath)

            filename = 'median.txt'
            if not os.path.exists(filepath + filename):
                with open(filepath + filename, 'w+') as median_vector_file:
                    for i in range(len(medians)):
                        median_vector_file.write('{} {}\n'.format(global_unique_recall_points[i], medians[i]))
            else:
                print("Skipping duplicate {}".format(filepath + filename))

            for model in self._models:
                filename = model.get_name() + '.txt'
                precision, recall, thresholds = precision_recall_curve(self._y_true, self._get_y_scores(model))
                print("Writing to file: {}".format(filename))
                if os.path.exists(filepath + filename):
                    print("Skipping duplicate {}".format(filepath + filename))
                    continue
                with open(filepath + filename, 'w+') as vector_file:
                    for i in range(len(recall)):
                        vector_file.write('{} {}\n'.format(recall[i], precision[i]))

            return

        plt.plot(global_unique_recall_points, minimums, '--', color='k', alpha=0.2)
        plt.plot(global_unique_recall_points, medians, '-', color='k', alpha=0.4)
        plt.plot(global_unique_recall_points, maximums, '--', color='k', alpha=0.2)

        num_verticals = 6
        vl_delta = 1 / (num_verticals + 1)
        vl_goal = 1 - vl_delta

        i = 0
        vl = global_unique_recall_points[i]

        while vl_goal >= vl_delta - (vl_delta / 2):
            while vl > vl_goal:
                i += 1
                vl = global_unique_recall_points[i]

            mad = normalization.median_absolute_deviation(all_precision_values_per_recall[i])

            x = vl
            y = median(all_precision_values_per_recall[i])

            # Plot vertical line
            plt.plot([x, x], [y-mad, y+mad], '-', color='k', alpha=0.5)

            # Plot ticks
            plt.plot([x-0.01, x+0.01], [y-mad, y-mad], '-', color='k', alpha=0.5)
            plt.plot([x-0.01, x+0.01], [y+mad, y+mad], '-', color='k', alpha=0.5)

            vl_goal -= vl_delta

        handles = [mpatches.Patch(color='k', alpha=0.4, label="Median Precision [AP=" + '{0:0.2f}'.format(mean(medians)) + "]")]

        self.precision_recall(existing_handles=handles, show_parameters=show_parameters)

    def roc_curve(self, show_parameters=False, keys=None, filename=None):

        corpus_name = self._corpus.get_corpus_name()

        print("Generating ROC curve for models derived from \'" + corpus_name + "\'")

        colors = 'brgymck'
        handles = []

        x = np.linspace(0, 1, 2)
        ax = plt.axes()
        ax.plot(x, x, '--', alpha=0.7, color='k', zorder=0)

        for i, model in enumerate(self._models):

            y_scores = self._get_y_scores(model)

            auc = self.get_auc(model)

            fpr, tpr, thresholds = roc_curve(self._y_true, y_scores)

            plt.plot(fpr, tpr, color=colors[i], alpha=0.8)

            auc_str = '{0:0.2f}'.format(auc)
            name = model.get_name()
            if show_parameters:
                name += ' ' + model.get_parameters(keys=keys)
            handles.append(mpatches.Patch(color=colors[i], label=name + " [AUC=" + auc_str + "]"))

        plt.xlabel('False Positive Rate')
        plt.ylabel('True Positive Rate')
        plt.ylim([0.0, 1.05])
        plt.xlim([0.0, 1.0])

        plt.title('ROC curve for \'' + self._corpus.get_corpus_name() + '\'')

        plt.legend(handles=handles)
        fig = plt.gcf()
        fig.set_size_inches(12, 8, forward=True)
        if filename is not None:
            plt.savefig(filename, dpi=100)
        else:
            plt.show()
        plt.clf()

    def threshold_Fmeasure(self, threshold_range=[0.0, 1.0], alpha=1):
        if threshold_range[1] <= threshold_range[0]:
            raise ValueError("Threshold range must be a positive range")

        corpus_name = self._corpus.get_corpus_name()
        print("Generating Threshold vs Fmeasure curve for models derived from \'" + corpus_name + "\'")

        ylim = [0.0, 0.7]

        colors = 'brgymck'
        handles = []

        for i, model in enumerate(self._models):
            print("Evaluating: " + model.get_name())

            link_values = model.get_all_values()

            min_threshold = min(link_values)
            max_threshold = max(link_values)
            print(model.get_name() + " min similarity: " + str(min_threshold))
            print(model.get_name() + " max similarity: " + str(max_threshold))

            plotted_threshold_vals = link_values
            plotted_threshold_vals.sort()

            #Fmeasures = [model.get_Fmeasure_with_threshold(threshold, self._corpus.get_truth_dict(), alpha=alpha) for threshold in plotted_threshold_vals]
            Fmeasures = model.get_Fmeasure_per_link_value(self._corpus.get_truth_dict(), alpha=alpha)

            #Fmeasures_unsorted = [model.get_Fmeasure_with_threshold(threshold, self._corpus.get_truth_dict(), alpha=alpha) for threshold in link_values]
            #alphas = [0.8 if self._corpus.get_truth_value(source, target) == 1 else 0.2 for source in sources for target in targets]
            #for k in range(len(link_values)):
            #    plt.plot([link_values[k]], [Fmeasures_unsorted[k]], 'o', color=colors[i], alpha=alphas[k], markersize=2.5)

            plt.plot(plotted_threshold_vals, Fmeasures, '-', color=colors[i], alpha=0.5)

            peak = max(Fmeasures)
            peak_index = Fmeasures.index(peak)

            median_value = mean(link_values)

            print(model.get_name() + ": " + str(plotted_threshold_vals[peak_index]))

            plt.plot([plotted_threshold_vals[peak_index], plotted_threshold_vals[peak_index]], ylim, '--', color=colors[i], alpha=0.4)
            plt.text(plotted_threshold_vals[peak_index], -0.04, '{0:0.2f}'.format(plotted_threshold_vals[peak_index]), color=colors[i], horizontalalignment='center')
            plt.plot([median_value, median_value], ylim, '-', color=colors[i], alpha=0.2)

            handles.append(mpatches.Patch(color=colors[i], label=model.get_name()))

        plt.xlabel('Threshold')
        plt.ylabel('F-Measure (alpha=' + str(alpha) + ')')
        plt.ylim(ylim)
        plt.xlim(threshold_range)
        plt.title('Threshold for positive link vs F-Measure for \'' + self._corpus.get_corpus_name() + '\'')

        plt.legend(handles=handles)
        plt.show()


    def calibration_amount_test(self, n_trials=30, calibration_granularity=100):
        corpus_name = self._corpus.get_corpus_name()
        print("Generating Calibration Amount vs Distance to Optimal Threshold curve for models derived from \'" + corpus_name + "\'")

        delta = 20 / (calibration_granularity)
        calibration_values = [delta * x + delta for x in range(calibration_granularity)]

        corpus_subsets = dict()
        for val in calibration_values:
            corpus_subsets[val] = self._corpus.get_subsets(val, n_trials)

        colors = 'brgymck'
        handles = []

        for i, model in enumerate(self._models):
            print("Evaluating: " + model.get_name())

            #true_optimal_threshold = self.optimal_threshold(model)
            #true_optimal_threshold = model.get_optimal_threshold(self._corpus.get_truth_dict())
            true_optimal_fmeasure = model.get_optimal_Fmeasure(self._corpus.get_truth_dict())
            #print(true_optimal_threshold)

            distances_to_optimal = []

            for val in calibration_values:
                #print("\tCalculating for calibration amount: " + str(val) + "%")
                links_set = corpus_subsets[val]

                avg_optimal_fmeasure = mean([model.get_Fmeasure_with_threshold(model.get_optimal_threshold(links), self._corpus.get_truth_dict()) for links in links_set])

                dist = avg_optimal_fmeasure - true_optimal_fmeasure
                #print(str(avg_optimal_threshold) + " < " + str(true_optimal_threshold))
                distances_to_optimal.append(dist)

            plt.plot(calibration_values, distances_to_optimal, '-', color=colors[i], alpha=0.8)
            handles.append(mpatches.Patch(color=colors[i], label=model.get_name()))

        plt.plot([0, 20], [0, 0], '--', alpha=0.2)
        plt.xlabel('Percent of Ground Truth')
        plt.ylabel('Distance to True Optimal Fmeasure')
        plt.ylim([-0.25, 0.25])
        plt.xlim([0, 20])
        plt.title('Effect of Calibrating with Sample of Ground Truth for \'' + self._corpus.get_corpus_name() + '\'')

        plt.legend(handles=handles)
        plt.show()

    def similarity_density(self, phi=10000):
        corpus_name = self._corpus.get_corpus_name()

        print("Generating Similarity Density curve for models derived from \'" + corpus_name + "\'")

        colors = 'brgymck'
        handles = []

        for i, model in enumerate(self._models):
            print("Evaluating: " + model.get_name())

            data = model.get_all_values()
            data.sort()

            indeces = [k for k in range(len(data))]

            optimal = model.get_optimal_threshold(self._corpus.get_truth_dict())
            plt.plot([0, indeces[-1]], [optimal, optimal], '--', color=colors[i], alpha=0.4)

            plt.plot(indeces, data, '.', color=colors[i], alpha=0.5)

            true_stdev = stdev(data)
            handles.append(mpatches.Patch(color=colors[i], label=model.get_name() + " (stdev: " + str(true_stdev) + ")"))


        plt.xlabel('Index')
        plt.ylabel('Similarity')
        plt.ylim([0.0, 1.0])
        plt.xlim([0, indeces[-1]])
        plt.title('Model Similarity Value Density for \'' + self._corpus.get_corpus_name() + '\'')

        plt.legend(handles=handles)
        plt.show()


    def compare_threshold_techniques(self):
        corpus_name = self._corpus.get_corpus_name()
        print("Comparing Threshold Generation Techniques for models derived from \'" + corpus_name + "\'")

        colors = 'brgymck'
        handles = []

        truth = self._corpus.get_truth_dict()

        for i, model in enumerate(self._models):
            print("Evaluating: " + model.get_name())

            fig, ax = plt.subplots()

            corpus_subsets_1 = self._corpus.get_subsets(1, 30)

            avg_Fmeasure_subset_1 = mean([model.get_Fmeasure_with_technique('gt_sample', truth, subset=subset) for subset in corpus_subsets_1])


            Fmeasure_mean = model.get_Fmeasure_with_technique('mean', truth)

            Fmeasure_median = model.get_Fmeasure_with_technique('median', truth)

            Fmeasure_minmax = model.get_Fmeasure_with_technique('min-max', truth)

            Fmeasure_sigmoid = model.get_Fmeasure_with_technique('sigmoid', truth)

            Fmeasure_link_estimate = model.get_Fmeasure_with_technique('link_est', truth)

            optimal_Fmeasure = model.get_optimal_Fmeasure(truth)

            heights = [
                avg_Fmeasure_subset_1,
                Fmeasure_mean,
                Fmeasure_median,
                Fmeasure_minmax,
                Fmeasure_sigmoid,
                Fmeasure_link_estimate,
                optimal_Fmeasure
            ]
            max_height = optimal_Fmeasure
            min_height = min(heights)

            ticks = [i+1 for i in range(len(heights))]
            bars = plt.bar(ticks, heights, color=colors[i], alpha=0.7)

            for k, h in enumerate(heights):
                ax.text(k + 1, h + (max_height * 0.0025), '{0:0.2}'.format(h / optimal_Fmeasure), color=colors[i], fontweight='bold', ha='center')

            labels = [
                "1% GT",
                "Mean",
                "Median",
                "Min-Max",
                "Sigmoid",
                "Link Est.",
                "Optimal"
            ]
            ax.set_xticks(ticks)

            scale = (max_height - min_height) * 0.1
            ax.set_ylim([min_height - scale, max_height + scale])
            ax.set_xticklabels(labels)
            data_stdev = stdev(model.get_all_values())
            ax.set_title(model.get_name() + ' Threhsold Generation Techniques for \'' \
                + self._corpus.get_corpus_name() + '\' [stdev: ' + '{0:0.3}'.format(data_stdev) + ']')
            ax.set_ylabel("Fmeasure Using Threshold Technique")

        plt.show()
