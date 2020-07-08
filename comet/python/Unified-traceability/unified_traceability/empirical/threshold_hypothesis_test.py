'''
Daniel McCrystal
July 2018

'''
import sys, os
sys.path.append(os.path.abspath('../ir'))
from Corpus import Corpus
from Trace_Model import Trace_Model

from statistics import *
from scipy import stats

#corpora = [Corpus.get_preset_corpus('1_2'), Corpus.get_preset_corpus('4_0')]
corpora = Corpus.get_all_preset_corpora()
corpora = corpora[1:3] + corpora[4:]

observations = dict()

threshold_techniques = ['gt_sample', 'mean', 'median', 'min-max', 'sigmoid', 'link_est', 'infer_samples']
ir_techniques = ['vsm', 'lsi', 'js', 'lda', 'nmf', 'jslda', 'jsnmf', 'vsmjs', 'vsmlda', 'vsmnmf']


for ir_technique in ir_techniques:
    observations[ir_technique] = dict()
    for threshold_technique in threshold_techniques:
        observations[ir_technique][threshold_technique] = [None] * len(corpora)


for i, corpus in enumerate(corpora):
    corpus_code = corpus.get_corpus_code()
    truth = corpus.get_truth_dict()
    for ir in ir_techniques:
        model = Trace_Model.get_instance_from_file('d_' + ir + '_sim_' + corpus_code + '.tm', path='default_output/' + corpus_code + '/sim/')

        optimal = model.get_optimal_Fmeasure(truth)

        corpus_subsets = corpus.get_subsets(1, 30)

        Fmeasures_gt_samples = [model.get_Fmeasure_with_technique('gt_sample', truth, subset=subset) for subset in corpus_subsets]

        avg_Fmeasure_gt_sample = mean(Fmeasures_gt_samples)

        observations[ir]['gt_sample'][i] = (avg_Fmeasure_gt_sample / optimal, [val / optimal for val in Fmeasures_gt_samples])

        observations[ir]['mean'][i] = (model.get_Fmeasure_with_technique('mean', truth) / optimal,)

        observations[ir]['median'][i] = (model.get_Fmeasure_with_technique('median', truth) / optimal,)

        observations[ir]['min-max'][i] = (model.get_Fmeasure_with_technique('min-max', truth) / optimal,)

        observations[ir]['sigmoid'][i] = (model.get_Fmeasure_with_technique('sigmoid', truth) / optimal,)

        observations[ir]['link_est'][i] = (model.get_Fmeasure_with_technique('link_est', truth, 5) / optimal,)

        positive_link_subsets = corpus.get_positive_link_subsets(5, 30)
        Fmeasures_inferred_samples = [model.get_Fmeasure_with_technique('infer_samples', truth, subset=subset) for subset in positive_link_subsets]
        avg_Fmeasure_inferred_sample = mean(Fmeasures_inferred_samples)
        observations[ir]['infer_samples'][i] = (avg_Fmeasure_inferred_sample / optimal, [val / optimal for val in Fmeasures_inferred_samples])

confidence = 0.95
p_threshold = 1 - confidence

print()
for ir_technique in ir_techniques:
    print("Evaluating: " + ir_technique + " (n = " + str(len(corpora)) + ")")
    print("==============================")

    # ranked_techniques -> tuple: (threshold technique str, [x1, x2, ... xn] of observations on that ir/threshold pair)
    ranked_techniques = [(thresh_tech, observations[ir_technique][thresh_tech]) for thresh_tech in threshold_techniques]
    ranked_techniques.sort(key=lambda x: mean([val[0] for val in x[1]]), reverse=True)
    if ir_technique == 'nmf':
        for t in ranked_techniques:
            if t[0] == 'sigmoid':
                print(t)

    '''
    i = 0
    while i < len(ranked_techniques) - 1:
        hypothesis = ranked_techniques[i]
        print("Testing whether " + hypothesis[0] + " outperforms " + str([t[0] for t in ranked_techniques[i+1:]]))
        print("(Null Hypothesis: " + hypothesis[0] + " and compared technique have same distribution)")
        print("-----------------------------------------")
        for target in ranked_techniques[i+1:]:
            wilcoxon = stats.wilcoxon([val[0] for val in hypothesis[1]], [val[0] for val in target[1]])
            print(hypothesis[0] + " - " + target[0] + " -> P-val: " + str(wilcoxon[1]), end='')
            if wilcoxon[1] <= p_threshold:
                print(" -> Statistically significant! Removing " + target[0] + " as a potential technique.")
                ranked_techniques.remove(target)
            else:
                print(" -> Difference is not statistically significant.")
        i += 1
        print()

    print("Remaining potential techniques cannot be statistically proven to outperform one another. Recommend using the technique with the lowest standard deviation.")
    '''
    print()
    print("Results")
    print("-----------------------------")
    for technique in ranked_techniques:
        tech_string = technique[0]

        score_mean = mean([val[0] for val in technique[1]])
        score_stdev = stdev([val[0] for val in technique[1]])

        if technique[0] == 'gt_sample' or technique[0] == 'infer_samples':
            score_stdev = stdev([val for samples in technique[1] for val in samples[1]])

        print('{:<16s}mean: {:f}   stdev: {:f}'.format(tech_string, score_mean, score_stdev))

    print()
