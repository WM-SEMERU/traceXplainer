'''
Daniel McCrystal
June 2018

'''
from nltk import word_tokenize
import sentencepiece as spm
import re
from evaluation.Trace_Model import Trace_Model
import os.path
import sys
sys.path.append('../..')


class IR_Method:
    """
    Abstract class that all IR classes will be derived from

    Attributes:
        _corpus (Corpus): The corpus object that this IR object will evaluate

        processed_sources (list(list(str))): A list of source artifacts that have been
            preprocessed according to the given hyperparameters
        processed_targets (list(list(str))): A list of target artifacts that have been
            preprocessed according to the given hyperparameters

        _source_vocab (set): Set of all unique words that appear in the source corpus
        _target_vocab (set): Set of all unique words that appear in the target corpus
        _common_vocab (set): Set of all unique words that appear in both source
            and target corpus

        # Hyperparameters:
        # See __init__ documentation for descriptions
        _only_alphnum (bool)
        _only_alph (bool)
        _split_camel_case (bool)
        _split_snake_case (bool)
        _remove_stop_words (bool)
        _stem (bool)
        _stop_words (list of str)
    """

    EXISTING_METHODS = []

    def __init__(self, corpus,
                 only_alphnum=False,
                 only_alph=True,
                 split_camel_case=True,
                 split_snake_case=True,
                 remove_stop_words=True,
                 stem=True,
                 only_common_vocab=True,
                 bpe=False,
                 bpe_vocab_size=2000,
                 relationship_type=0):
        """
        Attributes:
            corpus (Corpus): The corpus object that this IR object will evaluate

            # Hyperparameters:
            only_alphnum (bool, optional): If true (default: false), ignore all non
                alpha-numeric characters. If only_alph is true, this is ignored.

            only_alph (bool, optional): If true (default), ignore all non
                alphabetical characters

            split_camel_case (bool, optional): If true (default), instances of
                camel case (camelCase) will be tokenized

            split_snake_case (bool, optioinal): If true (default), instances of
                snake case (snake_case) will be tokenized

            remove_stop_words (bool, optional): If true (default), words in the
                default stop word list, or a custom list given by the stop_words
                parameter, will be removed

            stem (bool, optional): If true(default), words will be stemmed to
                their base meaning (cats -> cat)

            only_common_vocab (bool, optional): If true(default), words that do not
                appear in both the source and target artifacts will be ignored

        """

        self._corpus = corpus

        self._source_vocab = set()
        self._target_vocab = set()
        self._common_vocab = set()

        self._relationship_type = relationship_type

        non_default_arguments = dict()
        if only_alphnum != False:
            non_default_arguments['only_alphanum'] = only_alphnum
        if only_alph != True:
            non_default_arguments['only_alph'] = only_alph
        if split_camel_case != True:
            non_default_arguments['split_camel_case'] = split_camel_case
        if split_snake_case != True:
            non_default_arguments['split_snake_case'] = split_snake_case
        if remove_stop_words != True:
            non_default_arguments['remove_stop_words'] = remove_stop_words
        if stem != True:
            non_default_arguments['stemming'] = stem
        if only_common_vocab != True:
            non_default_arguments['only_common_vocab'] = only_common_vocab
        if bpe != False:
            non_default_arguments['bpe'] = bpe
        if bpe_vocab_size != 2000:
            non_default_arguments['bpe_vocab_size'] = bpe_vocab_size

        self._non_default_parameters = non_default_arguments

        self._only_alphnum = only_alphnum
        self._only_alph = only_alph
        self._split_camel_case = split_camel_case
        self._split_snake_case = split_snake_case
        self._remove_stop_words = remove_stop_words
        self._stem = stem
        self._only_common_vocab = only_common_vocab
        self._bpe = bpe
        self._bpe_vocab_size = bpe_vocab_size

        for method in IR_Method.EXISTING_METHODS:
            if self.has_same_preprocessing_params(method):
                print("Reusing existing preprocessed artifacts")
                self._source_vocab = method._source_vocab
                self._target_vocab = method._target_vocab

                self._processed_sources = method._processed_sources
                self._processed_targets = method._processed_targets

                if relationship_type == 1:
                    self._processed_targets = method._processed_sources
                    self._target_vocab = method._source_vocab
                elif relationship_type == 2:
                    self._processed_sources = method._processed_targets
                    self._source_vocab = method._target_vocab

                self._common_vocab = self._source_vocab.intersection(
                    self._target_vocab)
                break
        else:
            self._preprocess()
            IR_Method.EXISTING_METHODS.append(self)

    def _preprocess(self):
        """
        Preprocesses both source and target corpus.

        Returns:
            None
        """

        if self._bpe:
            corpus_root = self._corpus.get_corpus_root()
            corpus_code = self._corpus.get_corpus_code()

            corpus_path = corpus_root + corpus_code + '_raw_corpus.txt'
            print(corpus_path)

            sp_bpe = spm.SentencePieceProcessor()
            sp_bpe.load('../../../data/pretrained_models/bpe_models/big_bpe_2000.model')

            self._processed_sources = self._bpe_processed_artifacts(
                sp_bpe, sources=True)
            self._processed_targets = self._bpe_processed_artifacts(
                sp_bpe, sources=False)

            # print(self._processed_sources)
            # print(self._processed_targets)

        else:

            tokenized_processed_sources = self._processed_artifacts(
                self._corpus.get_sources(), self._source_vocab)
            tokenized_processed_targets = self._processed_artifacts(
                self._corpus.get_targets(), self._target_vocab)

            if self._relationship_type == 1:
                tokenized_processed_targets = tokenized_processed_sources
                self._target_vocab = self._source_vocab
            elif self._relationship_type == 2:
                tokenized_processed_sources = tokenized_processed_targets
                self._source_vocab = self._target_vocab

            self._common_vocab = self._source_vocab.intersection(
                self._target_vocab)

            self._processed_sources = [[x for x in source_artifact if (
                not self._only_common_vocab or x in self._common_vocab)] for source_artifact in tokenized_processed_sources]
            self._processed_targets = [[x for x in target_artifact if (
                not self._only_common_vocab or x in self._common_vocab)] for target_artifact in tokenized_processed_targets]

    def _split_camel_case_token(self, token):
        return re.sub('([a-z])([A-Z])', r'\1 \2', token).split()

    def _split_snake_case_token(self, token):
        return re.sub('([A-Za-z])?_([A-Za-z])', r'\1 \2', token).split()

    def _join_lists(self, lists):
        return [x for y in lists for x in y if len(x) > 0]

    def _bpe_processed_artifacts(self, bpe_model, sources):
        bpe_model.load('m_bpe.model')
        out = []

        if sources:
            artifacts = self._corpus.get_sources()
        else:
            artifacts = self._corpus.get_targets()

        for artifact in artifacts:
            encoded = bpe_model.encode_as_pieces(artifact)
            out.append(encoded)

        return out

    def _processed_artifacts(self, artifacts, vocabulary):
        """
        Processes a list of artifacts and returns the tokenized processed versions.
        Also compiles vocabulary.

        Arguments:
            artifacts (list of str): The list of raw artifacts to be processed
            vocabulary (set): The set object which will hold all unique terms in
                this corpus (should be empty when passed in).

        Returns:
            list of list of str: The tokenized processed artifacts
        """

        out = []
        flag = False
        for artifact in artifacts:

            tokenized_artifact = word_tokenize(artifact)

            new_tokens = []
            for token in tokenized_artifact:

                potential_new_tokens = [token]

                potential_new_tokens = self._join_lists(
                    [re.split('[-,./]', t) for t in potential_new_tokens])

                if self._only_alph:
                    # Remove non alphabetical characters
                    potential_new_tokens = [
                        re.sub(r'[^a-zA-z_]', '', t) for t in potential_new_tokens]

                elif self._only_alphnum:
                    # Remove non alpha-numeric characters
                    potential_new_tokens = [
                        re.sub(r'[^a-zA-Z0-9_]', '', t) for t in potential_new_tokens]

                if self._split_camel_case:
                    # Split instances of camel case
                    potential_new_tokens = self._join_lists([self._split_camel_case_token(t)
                                                             for t in potential_new_tokens])

                if self._split_snake_case:
                    # Split instances of snake case
                    potential_new_tokens = self._join_lists([self._split_snake_case_token(t)
                                                             for t in potential_new_tokens])

                # Make all tokens lowercase
                potential_new_tokens = [t.lower()
                                        for t in potential_new_tokens]

                if self._remove_stop_words:
                    # Remove stop words
                    potential_new_tokens = [
                        t for t in potential_new_tokens if t not in self._corpus.get_stop_words()]

                if self._stem:
                    # Get stem of words
                    stemmed_tokens = []
                    for t in potential_new_tokens:
                        for stemmer in self._corpus.get_stemmers():
                            stemmed = stemmer.stem(t)
                            if stemmed != t:
                                stemmed_tokens.append(stemmed)
                                break
                        else:
                            stemmed_tokens.append(t)
                    potential_new_tokens = stemmed_tokens

                new_tokens += potential_new_tokens

            vocabulary.update(new_tokens)
            out.append(new_tokens)
        return out

    def _new_model(self, name, parameters=None):
        if parameters is None:
            all_parameters = self._non_default_parameters
        else:
            all_parameters = {**parameters, **self._non_default_parameters}

        sources = self._corpus.get_source_names()
        targets = self._corpus.get_target_names()

        if self._relationship_type == 1:
            targets = sources
        elif self._relationship_type == 2:
            sources = targets

        model = Trace_Model(name, self._corpus.get_corpus_name(),
                            sources, targets, all_parameters)

        return model

    def generate_model(self, parameters=None):
        raise NotImplementedError

    def has_same_preprocessing_params(self, other):
        return self._only_alphnum == other._only_alphnum and \
            self._only_alph == other._only_alph and \
            self._split_camel_case == other._split_camel_case and \
            self._split_snake_case == other._split_snake_case and \
            self._remove_stop_words == other._remove_stop_words and \
            self._stem == other._stem and self._corpus == other._corpus and \
            self._bpe == other._bpe and self._bpe_vocab_size == other._bpe_vocab_size

    def get_corpus(self):
        return self._corpus
