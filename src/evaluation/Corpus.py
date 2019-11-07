'''
Daniel McCrystal
June 2018

'''

import os
import random

from nltk.stem.snowball import SnowballStemmer

class Corpus:
    """
    Represents a set of source artifacts, a set of target artifacts,
    and a ground truth model that represents the links between them.

    NOTE: This assumes that artifacts separated by line in the same file begin with
    an ID. Thus, the first word of every line will not be considered part of the artifact
    for semantic purposes

    Attributes:
        _name (str): The identifier of the corpus dataset

        _corpus_root (str): Path to the root directory of the corpus, from
            which the source, target, and truth files will be derived

        _sources (list of str): one str per artifact
        _targets (list of str): one str per artifact

        _truth (dict of str:(dict of str:int)): Holds the truth values of links
            between sources and targets. _truth[source][target] == 1 if link exists,
            0 otherwise.

        _source_index (list of str): aligned with source so that source_index[i]
            contains the filename or identifier for source[i]
        _target_index (list of str): aligned with target so that
            target_index[i] contains the filename or identifier for target[i]

        _filetype_whitelist (list of str): See __init__ documentation

        _filetype_blacklist(list of str): See __init__ documentation
    """

    def __init__(self, name, corpus_root='', source_path='requirements', target_path='source_code', truth_path=None, execution_traces=None, corpus_code=None, languages=['english'], filetype_whitelist=None, filetype_blacklist=None, blank=False):
        """
        Args:
            name (str): The identifier of the corpus dataset

            corpus_root (str): Path to the root directory of the corpus, from
                which the source, target, and truth files will be derived

            source_path (str): Path from corpus root to file or directory of
                source corpus. If directory, artifacts will be parsed from
                files in the directory. If file, artifacts will be parsed from
                lines in the file.
            target_path (str): Path from corpus root to file or directory of
                target corpus. If directory, artifacts will be parsed from
                files in the directory. If file, artifacts will be parsed from
                lines in the file.

            truth_path (str): Path to file containing ground truth

            natural_language (str or list of str, optional): The human language(s) that artifacts
                are written in. Will be matched to the corresponding stop words
                list and stemmer.

            code_language (str or list of str, optional): The programming language(s) that artifacts
                are written in. Will be matched to the corresponding stop words list.

            filetype_whitelist (list of str, optional): List of file extensions
                which will be converted into artifacts. If None (default), all
                file extensions will be converted. Do not include dot.

            filetype_blacklist(list of str, optional): List of file extensions
                which will be ignored when converting into artifacts. If None
                (default), all file extensions will be converted. Do not include
                dot.

        """
        self._name = name

        if blank:
            return

        self._corpus_root = corpus_root

        self._filetype_whitelist = filetype_whitelist
        self._filetype_blacklist = filetype_blacklist

        self._source_index = []
        self._target_index = []

        if type(source_path) is not list:
            source_path = [source_path]

        if type(target_path) is not list:
            target_path = [target_path]

        self._sources = []
        for sp in source_path:
            self._sources += self._parse_artifacts(sp, self._source_index)

        self._targets = []
        for tp in target_path:
            self._targets += self._parse_artifacts(tp, self._target_index)

        self._truth = None
        if truth_path is not None:
            self._truth = dict()
            self._all_links = []

            for source in self._source_index:
                self._truth[source] = dict()
                for target in self._target_index:
                    self._truth[source][target] = 0
                    self._all_links.append((source, target))

            if type(truth_path) is not list:
                truth_path = [truth_path]

            for tp in truth_path:
                with open(corpus_root + tp, 'r') as truth_file:
                    for line in truth_file.readlines():
                        tokens = line.split()

                        source = tokens[0]

                        if source not in self._truth:
                            raise KeyError("Source artifact \'" + source + "\' in truth file not a recognized artifact")

                        for target in tokens[1:]:
                            if target not in self._truth[source]:
                                print(source)
                                print(self._truth[source])
                                raise KeyError("Target artifact \'" + target + "\' in truth file not a recognized artifact")

                            self._truth[source][target] = 1

        self._execution_traces = None
        if execution_traces is not None:
            self._execution_traces = dict()
            with open(corpus_root + execution_traces, 'r') as et_file:
                for line in et_file.readlines():
                    tokens = line.split()
                    if tokens[0] not in self._execution_traces:
                        self._execution_traces[tokens[0]] = []
                    if tokens[1] not in self._execution_traces:
                        self._execution_traces[tokens[1]] = []

                    self._execution_traces[tokens[0]].append(tokens[1])
                    self._execution_traces[tokens[1]].append(tokens[0])

        self._stop_words = []
        self._stemmers = []

        if languages is not None:
            for language in languages:
                # check if there's a stop word list
                stop_words_path =  os.path.join(os.path.dirname(os.path.abspath(__file__)), "input/" + language + "_stop_words.txt")
                try:
                    with open(stop_words_path) as stop_words_file:
                        self._stop_words +=         stop_words_file.read().split()
                        print("Found stop words file: " + language + "_stop_words.txt")
                except FileNotFoundError:
                    print("No stop words found for language: [" + language + "]")

                # check if there's a stemmer
                try:
                    stemmer = SnowballStemmer(language)
                    print("Detected natural language: [" + language + "], generating stemmer")
                    self._stemmers.append(stemmer)
                except ValueError:
                    print("No natural language stemmer detected for language: [" + language + "]")

        self._corpus_code = corpus_code

    def _parse_artifacts(self, path, index):
        """
        Reads and indexes artifacts from file or files given in path

        Args:
            path (str): Path to the file or folder from which to parse artifacts
            index (list of str): Stores the identifiers for each artifact. This
                list should be empty when this method is called (the method will
                populate it).

        Returns:
            list of str: The list of artifacts
        """
        root = self._corpus_root
        print("Finding artifacts in: " + root + path)
        store = []
        if os.path.isfile(root + path):
            print("Getting artifacts by line from file")
            with open(root + path, 'r', encoding='utf-8', errors='ignore') as artifacts_file:
                for line in artifacts_file.readlines():
                    tokens = line.split()
                    artifact = ' '.join(tokens[1:])
                    index.append(tokens[0])
                    store.append(artifact)
                print("Read " + str(len(store)) + " artifacts from file")
        else:
            print("Getting artifacts by file from directory")
            for element in os.listdir(root + path):
                self._parse_artifacts_recur(path + '/', element, index, store)
            print("Read " + str(len(store)) + " artifacts from directory")

        print()
        return store

    def _parse_artifacts_recur(self, subroot, element, index, store):
        """
        Recursively searches for files in the directory given in the directory
        path from the corpus root.

        Args:
            path (str): Path to a file or directory. If path is a file, this is
                the base case and the file is read and an artifact is created
                and indexed
            index (list of str): Cumulatively stores the identifiers for each artifact.
            store (list of str): Cumulatively stores the artifacts.

        Returns:
            None
        """

        root = self._corpus_root
        full_path = root + subroot + element

        if os.path.isfile(full_path):
            extension = element[element.index('.')+1:].lower()

            if self._filetype_whitelist and extension not in self._filetype_whitelist:
                return
            if self._filetype_blacklist and extension in self._filetype_blacklist:
                return

            element_clean = element.replace('/', '.')
            index.append(element_clean)
            with open(full_path, 'r', encoding='utf-8', errors='ignore') as artifact_file:
                store.append(artifact_file.read())

        else:
            for sub_element in os.listdir(root + subroot + element):
                sub_element = element + '/' + sub_element
                self._parse_artifacts_recur(subroot, sub_element, index, store)

    def get_sources(self):
        return self._sources

    def get_targets(self):
        return self._targets

    def get_source_names(self):
        return self._source_index

    def get_target_names(self):
        return self._target_index

    def get_source_artifact_at_index(self, index):
        return self._sources[index]

    def get_target_artifact_at_index(self, index):
        return self._targets[index]

    def get_source_artifact_by_name(self, name):
        try:
            index = self._source_index.index(name)
            return self._sources[index]
        except ValueError:
            print("Source artifact \'" + name + "\' not found")
            return None

    def get_target_artifact_by_name(self, name):
        try:
            index = self._target_index.index(name)
            return self._targets[index]
        except ValueError:
            print("Target artifact \'" + name + "\' not found")
            return None

    def get_source_name_by_index(self, index):
        return self._source_index[index]

    def get_target_name_by_index(self, index):
        return self._target_index[index]

    def get_truth_value(self, source, target):
        if self._truth is not None:
            return self._truth[source][target]

    def get_truth_dict(self):
        return dict(self._truth)

    def get_execution_trace(self, artifact):
        if artifact in self._execution_traces:
            return self._execution_traces[artifact]
        else:
            return []

    def get_all_execution_traces(self):
        return dict(self._execution_traces)

    def get_stop_words(self):
        return self._stop_words

    def get_stemmers(self):
        return self._stemmers

    def get_subset(self, percent):
        if self._truth is None:
            print("No ground truth in corpus")
            return

        total_num_links = len(self._all_links)
        num_links = int(total_num_links * (percent / 100))

        link_subset = random.sample(self._all_links, num_links)

        link_subset_dict = dict()

        for link in link_subset:
            source = link[0]
            target = link[1]
            link_status = self.get_truth_value(source, target)

            if source not in link_subset_dict:
                link_subset_dict[source] = dict()

            link_subset_dict[source][target] = link_status

        return link_subset_dict

    def get_subsets(self, percent, n_trials):
        if self._truth is None:
            print("No ground truth in corpus")
            return

        subsets = []
        for n in range(n_trials):
            subsets.append(self.get_subset(percent))
        return subsets

    def get_positive_link_subset(self, num_sources, accuracy=0.75):
        if self._truth is None:
            print("No ground truth in corpus")
            return

        source_samples = random.sample(self.get_source_names(), num_sources)
        link_subset_dict = dict()

        targets = self.get_target_names()

        for source in source_samples:
            link_subset_dict[source] = dict()

            recall_rate = random.gauss(accuracy, 0.1)
            if recall_rate < 0:
                recall_rate = 0
            elif recall_rate > 1:
                recall_rate = 1

            actual_links = [target for target in targets if self.get_truth_value(source, target) == 1]

            num_links_found = int(len(actual_links) * recall_rate)

            for target in targets:
                link_subset_dict[source][target] = 0

            links_found = random.sample(actual_links, num_links_found)
            for target in links_found:
                link_subset_dict[source][target] = 1

        return link_subset_dict

    def get_positive_link_subsets(self, num_sources, n_trials, accuracy=0.75):
        if self._truth is None:
            print("No ground truth in corpus")
            return

        subsets = []
        for n in range(n_trials):
            subsets.append(self.get_positive_link_subset(num_sources, accuracy=accuracy))

        return subsets

    def get_link_sample(self, num_positive=5, num_negative=5):
        if self._truth is None:
            print("No ground truth in corpus")
            return

        sources = self.get_source_names()
        targets = self.get_target_names()

        positives = []
        negatives = []

        for source in sources:
            for target in targets:
                if self.get_truth_value(source, target) == 1:
                    positives.append((source, target))
                else:
                    negatives.append((source, target))

        if len(positives) < num_positive:
            print("Warning: " + str(num_positive) + " positive links were requested, but only " + str(len(positives)) + " positive links exist.")
            positive_subset = positives
        else:
            positive_subset = random.sample(positives, num_positive)

        if len(negatives) < num_negative:
            print("Warning: " + str(num_negative) + " negative links were requested, but only " + str(len(negatives)) + " negative links exist.")
            negative_subset = negatives
        else:
            negative_subset = random.sample(negatives, num_negative)

        return positive_subset + negative_subset

    def get_corpus_name(self):
        return self._name

    def get_corpus_code(self):
        if self._corpus_code is not None:
            return self._corpus_code
        else:
            print("No corpus code found for: " + self.get_corpus_name())

    def get_corpus_root(self):
        return self._corpus_root

    def get_raw_string(self):
        output = ''
        for doc in self._sources + self._targets:
            output += doc + '\n'
        return output

    def generate_raw_file(self, output_filename=None):
        if output_filename is None:
            output_filename = self._corpus_root + self._corpus_code + '_raw_corpus.txt'
        with open(output_filename, 'w+') as output_file:
            output_file.write(self.get_raw_string())

    def verify_datastore(self, datastore_manager):
        sources = self.get_source_names()
        targets = self.get_target_names()

        complete = True
        for source in sources:
            for target in targets:
                if not datastore_manager.file_exists(source, target, 'NUTS'):
                    print("Missing file for (" + source + ", " + target + ")")
                    complete = False

        if complete:
            print("All links have been generated for " + self.get_corpus_name() + "!")
        else:
            print("Some links are missing for " + self.get_corpus_name() + "...")
    # STATIC
    @classmethod
    def get_preset_corpus(cls, corpus_code):
        if corpus_code == 'LibEST':
            corpus_code = '0_1'
        elif corpus_code == 'EBT':
            corpus_code = '1_1'
        elif corpus_code == 'eTOUR':
            corpus_code = '2_0'
        elif corpus_code == 'iTrust':
            corpus_code = '3_0'
        elif corpus_code == 'Albergate':
            corpus_code = '4_0'
        elif corpus_code == 'SMOS':
            corpus_code = '5_0'

        try:
            separator_index = corpus_code.index('_')
        except ValueError:
            print("Invalid corpus code: " + corpus_code)
            return

        dataset = corpus_code[:separator_index]
        subset = corpus_code[separator_index+1:]

        dataset_name = None
        modifier = None
        source_path = None
        target_path = None
        truth_path = None
        execution_traces = None
        languages = None

        if dataset == '0':
            dataset_name = 'LibEST'
            source_path = 'requirements'
            languages = ['english', 'C']
            execution_traces = 'execution_traces.txt'

            if subset == '0':
                modifier = '(RQ to Code and Tests)'
                target_path = ['source_code', 'test']
                truth_path = ['req_to_code_ground.txt', 'req_to_test_ground.txt']

            elif subset == '1':
                modifier = '(RQ to Code)'
                target_path = 'source_code'
                truth_path = 'req_to_code_ground.txt'

            elif subset == '2':
                modifier = '(RQ to Tests)'
                target_path = 'test'
                truth_path = 'req_to_test_ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return

        elif dataset == '1':
            dataset_name = 'EBT'
            source_path = 'requirements.txt'
            languages = ['english']

            if subset == '0':
                modifier = '(RQ to Code and Tests)'
                target_path = ['source_code', 'test_cases.txt']
                truth_path = 'both_ground.txt'

            elif subset == '1':
                modifier = '(RQ to Code)'
                target_path = 'source_code'
                truth_path = 'code_ground.txt'

            elif subset == '2':
                modifier = '(RQ to Test)'
                target_path = 'test_cases.txt'
                truth_path = 'tests_ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return

        elif dataset == '2':
            dataset_name = 'eTOUR'
            source_path = 'use_cases_with_translation'
            languages = ['english', 'italian', 'java']
            if subset == '0':
                modifier = '(UC to Code)'
                target_path = 'source_code'
                truth_path = 'ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return

        elif dataset == '3':
            dataset_name = 'iTrust'
            source_path = 'use_cases'
            languages = ['english', 'java']

            if subset == '0':
                modifier = '(UC to Code)'
                target_path = 'source_code'
                truth_path = 'ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return

        elif dataset == '4':
            dataset_name = 'Albergate'
            source_path = 'requirements'
            languages = ['italian', 'java']

            if subset == '0':
                modifier = '(RQ to Code)'
                target_path = 'source_code'
                truth_path = 'ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return

        elif dataset == '5':
            dataset_name = 'SMOS'
            source_path = 'use_cases'
            languages = ['italian', 'java']

            if subset == '0':
                modifier = '(UC to Code)'
                target_path = 'source_code'
                truth_path = 'ground.txt'

            else:
                print("Unrecognized corpus code: " + corpus_code)
                return
        else:
            print("Unrecognized corpus code: " + corpus_code)
            return

        file_path = os.path.dirname(os.path.abspath(__file__))
        corpus_root = file_path + '/../../data/raw/' + dataset_name + '_semeru_format/'

        corpus_name = dataset_name + ' (' + corpus_code + ')'
        filetype_whitelist = ['java', 'txt', 'jsp', 'h', 'c']

        corpus = Corpus(
            corpus_name,
            corpus_root,
            source_path,
            target_path,
            truth_path,
            execution_traces=execution_traces,
            corpus_code=corpus_code,
            languages=languages,
            filetype_whitelist=filetype_whitelist
        )

        return corpus

    @classmethod
    def get_all_preset_corpus_codes(cls):
        codes = [
            '0_0', '0_1', '0_2',
            '1_0', '1_1', '1_2',
            '2_0',
            '3_0',
            '4_0',
            '5_0'
        ]
        return codes

    @classmethod
    def get_all_preset_corpora(cls):
        codes = Corpus.get_all_preset_corpus_codes()

        return [Corpus.get_preset_corpus(code) for code in codes]
