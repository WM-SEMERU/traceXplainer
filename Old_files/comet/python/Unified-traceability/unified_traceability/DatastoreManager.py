'''
Daniel McCrystal
August 2018
'''

import os
from causalityFacade import LatentTraceabilityRecovery
from ir.Trace_Model import Trace_Model
from ir.Corpus import Corpus

class DatastoreManager:

    def __init__(self, corpus_name, complexity, config='default', location=''):
        self._location = location
        if len(self._location) > 0 and self._location[-1] != '/':
            self._location += '/'

        self._corpus_name = corpus_name
        self._complexity = complexity
        self._config = config

    def _old_get_path(self, source, target, complexity=None):
        if complexity is None:
            complexity = self._complexity
        return self._location + 'datastore/ltr_models/' + self._corpus_name + '/c' + str(complexity) +  '/' + source + '/' + target + '/' + self._config +'/'

    def _get_path(self, source, target, complexity=None):
        if complexity is None:
            complexity = self._complexity
        return self._location + 'datastore/' + self._corpus_name + '/c' + str(complexity) + '/' + self._config + '/' + source + '/' + target + '/'

    def _get_filename(self, source, target, inference_type):
        return inference_type + '-' + source + '-' + target + '.txt'

    def _old_get_filename(self, source, target, inference_type):
        return inference_type + '-' + source + '-' + target + '.pkl'


    def write_to_datastore(self, source, target, inference_type, ltr_obj):
        if self._config != 'default':
            print("Attempting to write probabilistic model for " + inference_type + "-(" + source + ", " + target + ") with config: " + self._config + " to file")
        else:
            print("Attempting to write probabilistic model for " + inference_type + "-(" + source + ", " + target + ") to file")
        path = self._get_path(source, target)
        if not os.path.exists(path):
            os.makedirs(path)

        filename = self._get_filename(source, target, inference_type)

        mu = ltr_obj.get_mean_linkage()
        dev = ltr_obj.get_dev_linkage()

        with open(path + filename, 'w') as output_file:
            output_file.write(str(mu) + ' ' + str(dev))

        print("Success!")


    def file_exists(self, source, target, inference_type, complexity=None, actualComplexity=None):
        if actualComplexity == 3:
            temp = self._corpus_name
            self._corpus_name = 'LibEST (0_2)'
            path = self._get_path(source, target, complexity=complexity)
            self._corpus_name = temp
        else:
            path = self._get_path(source, target, complexity=complexity)
        filename = self._get_filename(source, target, inference_type)
        return os.path.exists(path + filename)

    def old_load_from_datastore(self, source, target, inference_type, complexity=None):
        if self._config != '':
            print("Attempting to read probabilistic model for " + inference_type + "-(" + source + ", " + target + ") with config: " + self._config + " from file")
        else:
            print("Attempting to read probabilistic model for " + inference_type + "-(" + source + ", " + target + ") from file")

        path = self._old_get_path(source, target, complexity=complexity)
        filename = self._old_get_filename(source, target, inference_type)

        if self.file_exists(source, target, inference_type, complexity=complexity):
            print("File found: " + path + filename)
            ltr_obj = LatentTraceabilityRecovery()
            ltr_obj.load_ltr(path + filename)

            print("Success!")
            return ltr_obj
        else:
            print("File not found: " + path + filename)
            return None

    def load_from_datastore(self, source, target, inference_type, complexity=None, actualComplexity=None):
        if self._config != 'default':
            print("Attempting to read probabilistic model for " + inference_type + "-(" + source + ", " + target + ") with config: " + self._config + " from file")
        else:
            print("Attempting to read probabilistic model for " + inference_type + "-(" + source + ", " + target + ") from file")

        ##hardcodig to make function trace_links logic
        if actualComplexity == 3:
            temp = self._corpus_name
            self._corpus_name = 'LibEST (0_2)'
            path = self._get_path(source, target, complexity=complexity)
            self._corpus_name = temp
        else:
            path = self._get_path(source, target, complexity=complexity)
        filename = self._get_filename(source, target, inference_type)

        if self.file_exists(source, target, inference_type, complexity=complexity,actualComplexity=actualComplexity):
            print("File found: " + path + filename)
            with open(path + filename, 'r') as input_file:
                data = input_file.readline()
                mu, dev = data.split()

            print("Success!")
            return (float(mu), float(dev))
        else:
            print("File not found: " + path + filename)
            return None

    @classmethod
    def clean(cls, corpus_code, complexity, config='ten_ir'):
        corpus = Corpus.get_preset_corpus(corpus_code)
        sources = corpus.get_source_names()
        targets = corpus.get_target_names()

        dsm_old = DatastoreManager(corpus.get_corpus_name(), complexity, config)
        dsm_new = DatastoreManager(corpus.get_corpus_name(), complexity, config, location='new_datastore')

        for source in sources:
            for target in targets:
                ltr_obj = dsm_old.old_load_from_datastore(source, target, 'NUTS')
                if ltr_obj is not None:
                    dsm_new.write_to_datastore(source, target, 'NUTS', ltr_obj)


if __name__ == '__main__':
    DatastoreManager.clean('0_2', 1)
