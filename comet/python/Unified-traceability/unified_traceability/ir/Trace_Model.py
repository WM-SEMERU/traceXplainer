'''
Daniel McCrystal
June 2018

'''
import sys, os
sys.path.append('../')
from utils import normalization
import math
import random
import bisect
from statistics import *
import ast

class Trace_Model:
    """
    Represents artifact link predictions.

    Attributes:
        _name (str): The name of the IR method used to generate the model
            plus any specific parameters

        _corpus_name (str): The identifier of the corpus from which the model was generated

        _model (dict of (dict of str:float)): The value at _model[source][target]
            represents the confidence that source and target are linked, where
            source and target are strings representing the id of the artifact
    """

    def __init__(self, name, corpus_name, source_ids, target_ids, parameters=None):
        """
        Arguments:
            name (str): The name of the IR method used to generate the model
            corpus_name (str): The identifier of the corpus from which the model was generated
            source_ids (list of str): The identifiers of all the source artifacts
            target_ids (list of str): The identifiers of all the target artifacts
            parameters (dict of str:obj): Stores the non-default parameters that were
                used to generate this model
        """

        self._name = name
        self._corpus_name = corpus_name

        if parameters is None:
            parameters = {}

        self._parameters = parameters

        self._model = dict()

        for source in source_ids:
            self._model[source] = dict()
            for target in target_ids:
                self._model[source][target] = None

        self._default_threshold = None
        self._default_threshold_technique = None

        self._num_links = 0

    @classmethod
    def get_instance_from_file(cls, filename, path, name=None, corpus_name=None):
        """
        Generates an Trace_Model object using a given similarities file.

        Arguments:
            filename (str): The name of the file to be read.
            name (str): The name of the method used to generate this model
            corpus_name (str): The identifier of the corpus from which the model
                was generated. (* It is important that this string be identical
                to the corpus_name attribute of any other Trace_Model object that
                you wish to evaluate simultaneously with this object *)
            path (str, optional): The location of the similarities file, defaults to 'input/'.

        Returns:
            A new instance of Trace_Model with values from the file.
        """

        model = dict()

        threshold_technique = None
        threshold = None

        with open(path + filename, 'r') as input_file:

            lines = input_file.readlines()
            data_offset = 0

            split_filename = filename.split('.')
            if len(split_filename) > 1 and split_filename[-1] == 'tm':
                print("Parsing metadata from: " + filename)
                check = lines[0]
                if check != '# Trace Model\n':
                    raise ValueError("Invalid .tm file")

                if name is not None:
                    print("Getting name from .tm file, ignoring provided name")
                if corpus_name is not None:
                    print("Getting corpus name from .tm file, ignoring provided corpus name")

                read_name = lines[1][lines[1].index(':')+2:-1]
                read_parameters = lines[2][lines[2].index(':')+2:-1]
                print(read_parameters)
                read_corpus_name = lines[3][lines[3].index(':')+2:-1]

                read_threshold_technique = lines[4][lines[4].index(':')+2:-1]
                read_threshold = lines[5][lines[5].index(':')+2:-1]

                name = read_name
                parameters = ast.literal_eval(read_parameters)
                #parameters = None
                corpus_name = read_corpus_name

                if read_threshold_technique != 'n/a':
                    threshold_technique = read_threshold_technique
                if read_threshold != 'n/a':
                    threshold = float(read_threshold)

                data_offset = 6

            else:
                print("Reading similarities from text file")
                if name is None:
                    raise ValueError("If not reading a .tm file, you must provide a name")

                if corpus_name is None:
                    raise ValueError("If not reading a .tm file, you must provide a corpus name")

            for line in lines[data_offset:]:
                tokens = line.split()

                source = tokens[0]
                target = tokens[1]
                value = float(tokens[2])

                if source not in model:
                    model[source] = dict()

                model[source][target] = value

        source_ids = list(model.keys())

        target_set = set()
        for source in source_ids:
            target_set.update(list(model[source].keys()))
        target_ids = list(target_set)

        model_obj = Trace_Model(name, corpus_name, source_ids, target_ids, parameters=parameters)
        model_obj._default_threshold_technique = threshold_technique
        model_obj._default_threshold = threshold
        for source in source_ids:
            for target in model[source].keys():
                model_obj.set_value(source, target, model[source][target])

        return model_obj

    @classmethod
    def get_all_models_from_folder(cls, path, names=None, corpus_name=None):
        models = []
        filenames = os.listdir(path)
        for i in range(len(filenames)):
            filename = filenames[i]
            name = None if names is None else filename
            models.append(Trace_Model.get_instance_from_file(filename, path, name, corpus_name))

        return models

    def write_to_file(self, filename, path='', threshold_technique=None):
        """
        Writes the similarity values of each source target pair to a file.

        Arguments:
            filename (str): The name of the file to be written
            path (str): The location of the file to be written

        Returns:
            None
        """

        if threshold_technique is None:
            threshold_technique = self._default_threshold_technique

        if path != '':
            os.makedirs(os.path.dirname(path), exist_ok=True)

        with open(path + filename, 'w+') as output_file:
            split_filename = filename.split('.')
            if len(split_filename) > 1 and split_filename[-1] == 'tm':
                print("Writing metadata to: " + filename)
                output_file.write('# Trace Model\n')
                output_file.write('# name: ' + self.get_name() + '\n')
                output_file.write('# parameters: ' + str(self._parameters) + '\n')
                output_file.write('# corpus_name: ' + self.get_corpus_name() + '\n')
                if threshold_technique is not None:
                    threshold = self._default_threshold
                    output_file.write('# threshold_technique: ' + threshold_technique + '\n')
                    output_file.write('# threshold: ' + str(threshold) + '\n')
                else:
                    output_file.write('# threshold_technique: n/a\n')
                    output_file.write('# threshold: n/a\n')
            else:
                print("Writing only similarities to text file")

            for source, target in self.get_links():
                output_file.write(source + ' ' + target + ' ' + str(self.get_value(source, target)) + '\n')

    def write_links_to_file(self, threshold, filename, path='output/'):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path + filename, 'w+') as output_file:
            for source, target in self.get_links():
                    output_file.write(source + ' ' + target + ' ' + str(self.get_link_status_with_threshold(source, target, threshold)) + '\n')

    def set_value(self, source, target, value):
        if source not in self._model:
            raise KeyError("Source artifact \'" + source + "\' not a recognized artifact")
        if target not in self._model[source]:
            raise KeyError("target artifact \'" + target + "\' not a recognized artifact")

        if self._model[source][target] is None:
            if value is not None:
                self._num_links += 1
        else:
            if value is None:
                self._num_links -= 1

        self._model[source][target] = value

    def get_value(self, source, target):
        if source not in self._model:
            raise KeyError("Source artifact \'" + source + "\' not a recognized artifact")
        if target not in self._model[source]:
            raise KeyError("Target artifact \'" + target + "\' not a recognized artifact")

        if self._model[source][target] is None:
            raise Error(self.get_name() + " Warning: A value for (" + source + ", " + target + ") has not yet been set")

        return self._model[source][target]

    def query(self, source, threshold=None):
        #print(source)
        if source not in self._model:
            raise KeyError("Source artifact \'" + source + "\' not a recognized artifact")

        if threshold is None:
            threshold = self._default_threshold

        response = []
        for target in self._model[source]:
            if self._model[source][target] >= threshold and source != target:
                response.append((target, self._model[source][target]))

        sorted_response = sorted(response, key=lambda x: x[1], reverse=True)
        return sorted_response


    def get_all_values(self):
        return [self._model[source][target] for source, target in self.get_links()]

    def get_link_status_with_threshold(self, source, target, threshold):
        return 1 if self.get_value(source, target) >= threshold else 0

    def get_link_status(self, source, target):
        if self._default_threshold is None:
            raise ValueError("No default threshold given")

        return self.get_link_status_with_threshold(source, target, self._default_threshold)

    def get_link_model(self, threshold=None):
        if threshold is None:
            if self._default_threshold is None:
                raise ValueError("No default threshold given")

            threshold = self._default_threshold

        sources = self.get_source_names()
        targets = self.get_target_names()

        link_model = Trace_Model(self.get_name() + ' [links]', self.get_corpus_name(), sources, targets)

        for source, target in self.get_links():
            link_model.set_value(source, target, self.get_link_status_with_threshold(source, target, threshold))

        return link_model

    def set_default_threshold_technique(self, threshold_technique, subset=None):
        self._default_threshold = self.get_threshold_with_technique(threshold_technique, subset=subset)
        self._default_threshold_technique = threshold_technique

    def set_default_threshold(self, threshold):
        self._default_threshold = threshold
        self._default_threshold_technique = 'arbitrary'

    def get_default_threshold(self):
        return self._default_threshold

    def get_threshold_with_technique(self, technique, subset=None):
        data = self.get_all_values()
        if technique == 'gt_sample':
            return self.get_optimal_threshold(subset)
        elif technique == 'mean':
            return mean(data)
        elif technique == 'median':
            return median(data)
        elif technique == 'min-max':
            return normalization.get_threshold_min_max(data)
        elif technique == 'sigmoid':
            return normalization.get_threshold_sigmoid(data)
        elif technique == 'link_est':
            return self.get_threshold_with_link_estimate()
        elif technique == 'infer_samples':
            return self.get_optimal_threshold(subset)
        else:
            raise ValueError("Unrecognized threshold technique: " + technique)

    def get_Fmeasure_with_technique(self, technique, known_links, subset=None):
        threshold = self.get_threshold_with_technique(technique, subset=subset)
        return self.get_Fmeasure_with_threshold(threshold, known_links)

    def get_threshold_with_link_estimate(self, est_links_per_source=5, error=0.2, alpha=1):

        similarity_values = []

        sources = self.get_source_names()
        targets = self.get_target_names()

        num_sources = len(sources)
        for source, target in self.get_links():
            val = self.get_value(source, target)
            bisect.insort(similarity_values, val)

        est_positive_links = num_sources * est_links_per_source

        est_postive_links_with_error = int(est_positive_links * (1+error))

        est_threshold = similarity_values[-est_postive_links_with_error]

        return est_threshold

    def get_normalized_values_with_minmax(self):
        data = self.get_all_values()
        data_min = min(data)
        data_max = max(data)
        data_range = data_max - data_min

        normalized_dict = dict()
        for source in self.get_source_names():
            normalized_dict[source] = dict()
            for target in self.get_target_names():
                if self._model[source][target] is not None:
                    if data_range != 0:
                        normalized_value = (self.get_value(source, target) - data_min) / data_range
                    else:
                        normalized_value = 0.5

                        normalized_dict[source][target] = normalized_value
        return normalized_dict

    def get_normalized_values_with_sigmoid(self, include_threshold=False):
        data = self.get_all_values()
        mean_data = mean(data)
        stdev_data = stdev(data)

        normalized_dict = dict()
        for source in self.get_source_names():
            normalized_dict[source] = dict()
            for target in self.get_target_names():
                if self._model[source][target] is not None:
                    normalized_value = normalization.sigmoid(self.get_value(source, target), mean_data, stdev_data)
                    if normalized_value >= 1:
                        normalized_value = 0.999999999
                    elif normalized_value <= 0:
                        normalized_value = 0.000000001
                    normalized_dict[source][target] = normalized_value

        if not include_threshold:
            return normalized_dict
        else:
            if self._default_threshold is None:
                raise ValueError("No default threshold given")
            return (normalized_dict, normalization.sigmoid(self._default_threshold, mean_data, stdev_data))

    def Fmeasure(self, precision, recall, alpha=1):
        if precision == 0 and recall == 0:
            return 0

        return (1 + alpha) * ((precision * recall) / (alpha * precision + recall))

    def get_Fmeasure_with_threshold(self, threshold, known_links, alpha=1):

        retrieved_links = set()
        relevant_links = set()

        for source in known_links:
            for target in known_links[source]:
                if self.get_link_status_with_threshold(source, target, threshold) == 1:
                    retrieved_links.add((source, target))
                if known_links[source][target] == 1:
                    relevant_links.add((source, target))

        if len(retrieved_links) > 0:
            precision = len(relevant_links.intersection(retrieved_links)) / len(retrieved_links)
        else:
            if len(relevant_links) > 0:
                precision = 0
            else:
                precision = 1

        if len(relevant_links) > 0:
            recall = len(relevant_links.intersection(retrieved_links)) / len(relevant_links)
        else:
            recall = 1

        return self.Fmeasure(precision, recall, alpha=alpha)


    def get_Fmeasure_per_link_value(self, known_links, alpha=1):
        Fmeasures = []
        link_values = [(source, target, self.get_value(source, target)) for source in known_links for target in known_links[source]]
        link_values.sort(key=lambda x: x[2], reverse=True)

        num_relevant_links = len([0 for source in known_links for target in known_links[source] if known_links[source][target] == 1])
        num_retrieved_and_relevant = 0

        max_f_measure = -1
        max_threshold = 0

        i = 0
        while i < len(link_values):
            source = link_values[i][0]
            target = link_values[i][1]
            val = link_values[i][2]

            duplicates = -1
            while i < len(link_values) and link_values[i][2] == val:
                if known_links[source][target] == 1:
                    num_retrieved_and_relevant += 1
                i += 1
                duplicates += 1

            precision = num_retrieved_and_relevant / i
            if num_relevant_links > 0:
                recall = num_retrieved_and_relevant / num_relevant_links
            else:
                recall = 1

            score = self.Fmeasure(precision, recall)
            Fmeasures.append(score)
            for d in range(duplicates):
                #print("Appending duplicate: " + str(d))
                Fmeasures.append(score)

        return Fmeasures

    def get_optimal_threshold(self, known_links):
        link_values = [(source, target, self.get_value(source, target)) for source in known_links for target in known_links[source]]
        link_values.sort(key=lambda x: x[2], reverse=True)

        num_relevant_links = len([0 for source in known_links for target in known_links[source] if known_links[source][target] == 1])
        num_retrieved_and_relevant = 0

        max_f_measure = -1
        max_threshold = 0

        i = 0
        while i < len(link_values):
            source = link_values[i][0]
            target = link_values[i][1]
            val = link_values[i][2]

            while i < len(link_values) and link_values[i][2] == val:
                if known_links[source][target] == 1:
                    num_retrieved_and_relevant += 1
                i += 1

            precision = num_retrieved_and_relevant / i
            if num_relevant_links > 0:
                recall = num_retrieved_and_relevant / num_relevant_links
            else:
                recall = 1

            score = self.Fmeasure(precision, recall)

            if score > max_f_measure:
                max_f_measure = score
                max_threshold = val

        return max_threshold

    def get_optimal_Fmeasure(self, known_links):
        return self.get_Fmeasure_with_threshold(self.get_optimal_threshold(known_links), known_links)

    def get_source_names(self):
        return list(self._model.keys())

    def get_target_names(self):
        return list(self._model[self.get_source_names()[0]].keys())

    def get_links(self):
        for source in self.get_source_names():
            for target in self.get_target_names():
                if self._model[source][target] is not None:
                    yield (source, target)

    def get_number_of_links(self):
        return self._num_links

    def get_name(self):
        return self._name

    def set_name(self, new_name):
        self._name = new_name

    def set_parameter(self, parameter, value):
        if parameter in self._parameters:
            print("Warning: Overwriting existing parameter [" + str(parameter) + "] - (" + str(self._parameters[parameter]) + ")")
        self._parameters[parameter] = value

    # keys for normal trace model -> ['key1', 'key2', ...]
    # OR
    # keys for orthogonal trace model -> ['key1', {'model_A': ['subkey1', 'subkey2'], 'model_B': ['subkey1', 'subkey2']}]
    # potential output: {key1: val1, key2: val2, model_A: {subkey1: subval1, subkey2: subval2}}
    def get_parameters(self, keys=None):
        if keys is None:
            keys = list(self._parameters.keys())

        if type(keys) is not list:
            keys = [keys]

        out = "{"
        for key in keys:
            if type(key) == str:
                if key not in self._parameters:
                    raise KeyError("Unrecognized parameter: " + key)

                out += "\'" + key + "\'" + ": " + str(self._parameters[key]) + ", "

            elif type(key) is dict:
                for model in key:
                    if model not in self._parameters or type(self._parameters[model]) is not dict:
                        raise KeyError("Unrecognized orthogonal method: " + str(model))
                    out += model + ": {"
                    if type(key[model]) is not list:
                        key[model] = [key[model]]
                    for subkey in key[model]:
                        out += "\'" + subkey + "\'" + ": "
                        if type(self._parameters[model][subkey]) is str:
                            out += "\'" + self._parameters[model][subkey] + "\'" + ", "
                        else:
                            out += str(self._parameters[model][subkey]) + ", "

                    out = out[:-2] + "}, "

            else:
                raise KeyError("Unrecognized key type: " + str(type(key)))
        if len(out) > 1:
            out = out[:-2] + "}"
        else:
            out += "}"

        return out

    def get_corpus_name(self):
        return self._corpus_name

    def get_sampled_model(self, ratio=None, num_links=None, links=None, random_seed=12345): # random seed is for sampling links

        if links is None:
            if ratio is not None:
                num_links = int(self.get_number_of_links() * ratio)

            all_links = list(self.get_links())
            all_links = sorted(all_links)
            random.seed(random_seed)
            sampled_links = random.sample(all_links, num_links)
        else:
            sampled_links = links

        unique_sampled_sources = set()
        unique_sampled_targets = set()

        for source, target in sampled_links:
            unique_sampled_sources.add(source)
            unique_sampled_targets.add(target)

        new_model = Trace_Model(self.get_name(), self.get_corpus_name(), list(unique_sampled_sources), list(unique_sampled_targets), parameters=self._parameters)
        new_model._default_threshold = self._default_threshold
        new_model._default_threshold_technique = self._default_threshold_technique

        for source, target in sampled_links:
            new_model.set_value(source, target, self.get_value(source, target))

        return new_model

    def get_broken_down(self):
        sub_models = []

        for i, source in enumerate(self.get_source_names()):

            new_model = Trace_Model(self.get_name() + '~[' + str(i) + ']', self.get_corpus_name(), [source], self.get_target_names(), self._parameters)

            new_model._default_threshold = self._default_threshold
            new_model._default_threshold_technique = self._default_threshold_technique

            for target in self.get_target_names():
                new_model.set_value(source, target, self._model[source][target])
            sub_models.append(new_model)

        return sub_models

    @classmethod
    def merge_models(cls, models):
        name = models[0].get_name()[:models[0].get_name().index('~')]
        corpus_name = models[0].get_corpus_name()
        parameters = models[0]._parameters

        threshold = models[0]._default_threshold
        threshold_technique = models[0]._default_threshold_technique

        main_model = models[0]._model
        source_names = models[0].get_source_names()
        target_names = models[0].get_target_names()

        for model in models[1:]:
            main_model.update(model._model)
            source_names += model.get_source_names()

        merged_model = Trace_Model(name, corpus_name, source_names, target_names, parameters)
        merged_model._model = main_model
        merged_model._default_threshold = threshold
        merged_model._default_threshold_technique = threshold_technique
        return merged_model

    def merge_sampled_models_to_file(filepath,output_path):

        files = []
        lines = []
        all_lines = []
        special_words = ['RQ','UC','F-SOG','SMOS']

        if not os.path.exists(os.path.dirname(output_path)):
            try:
                os.makedirs(os.path.dirname(output_path))
            except OSError as exc: # Guard against race condition
                if exc.errno != errno.EEXIST:
                    raise

        merged_file = open(output_path, 'w')


        for file in os.listdir(filepath):
            if file.endswith('.tm'):
                files.append(os.path.join(filepath, file))

        first_file = files[0]
        with open(first_file) as ff:
            preamble = ff.readlines()

        for pre_line in preamble:
            if 'RQ' not in pre_line:
                merged_file.write(pre_line)

        for curr_file in files:
            with open(curr_file) as f:
                lines=(f.readlines())
                print(len(lines))
                for sing_line in lines:
                    all_lines.append(sing_line)

        for curr_line in all_lines:
            if any(x in curr_line for x in special_words):
            #if 'RQ' or 'UC' or 'F-SOG' or 'SMOS' in curr_line:
                merged_file.write(curr_line)
