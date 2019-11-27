'''
Daniel McCrystal
June 2018

'''
import sys
sys.path.append('../..')

from nltk import word_tokenize
import sentencepiece as spm
import re
from evaluation.Trace_Model import Trace_Model
import os.path
from .Preprocessor import Preprocessor


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

    """

    EXISTING_METHODS = []

    def __init__(self, corpus, preprocessor=None, relationship_type=0):
        """
        Attributes:
            corpus (Corpus): The corpus object that this IR object will evaluate

        """
        self._corpus = corpus
        if preprocessor is None:
            preprocessor = Preprocessor()
        self._preprocessor = preprocessor
        self._relationship_type = relationship_type

        self._preprocess()

    def _preprocess(self):
        """
        Preprocesses both source and target corpus.

        Returns:
            None
        """
        preprocessor_results = self._preprocessor.process_corpus(self._corpus)

        self._processed_sources = preprocessor_results.processed_sources
        self._processed_targets = preprocessor_results.processed_targets
        self._source_vocab = preprocessor_results.source_vocab
        self._target_vocab = preprocessor_results.target_vocab
        
        if self._relationship_type == 1:
            self._processed_targets = self._processed_sources
            self._target_vocab = self._source_vocab
        elif self._relationship_type == 2:
            self._processed_sources = self._processed_targets
            self._source_vocab = self._target_vocab

        

        self._common_vocab = self._source_vocab.intersection(
            self._target_vocab)

    def _new_model(self, name, parameters=None):
        
        sources = self._corpus.get_source_names()
        targets = self._corpus.get_target_names()

        if self._relationship_type == 1:
            targets = sources
        elif self._relationship_type == 2:
            sources = targets

        model = Trace_Model(name, self._corpus.get_corpus_name(),
                            sources, targets, parameters)

        return model

    def generate_model(self, parameters=None):
        raise NotImplementedError

    def get_corpus(self):
        return self._corpus
