'''
Daniel McCrystal
Nov 2019
'''

from typing import List, Set, Callable, Tuple, Dict, Optional
import re
from Corpus import Corpus
import sentencepiece as spm
from nltk.stem.snowball import SnowballStemmer


class PreprocessorResult:
    def __init__(self, processed_sources: List[List[str]], processed_targets: List[List[str]], source_vocab: Set[str], target_vocab: Set[str]):
        self.processed_sources = processed_sources
        self.processed_targets = processed_targets
        self.source_vocab = source_vocab
        self.target_vocab = target_vocab


# Define types
RawArtifacts = List[str]
TokenizedArtifacts = List[List[str]]
PreprocessorStageOutput = Tuple[TokenizedArtifacts, TokenizedArtifacts]
PreprocessorStageFunctionArgs = Dict[str, any]
PreprocessorStageFunction = Callable[[
    TokenizedArtifacts, PreprocessorStageFunctionArgs], TokenizedArtifacts]
PreprocessorUniversalStageFunction = Callable[[
    TokenizedArtifacts, TokenizedArtifacts, PreprocessorStageFunctionArgs], Tuple[TokenizedArtifacts, TokenizedArtifacts]]


class Preprocessor:

    def __init__(self):
        # Functions that determine how the tokens are
        # further split into more tokens.
        self.__source_tokenizer_functions: List[PreprocessorStageFunction] = []
        self.__target_tokenizer_functions: List[PreprocessorStageFunction] = []

        # Functions that determine how the tokens are modified.
        # Must take List[List[str]] as input and return List[List[str]]
        self.__source_token_modifier_functions: List[PreprocessorStageFunction] = [
        ]
        self.__target_token_modifier_functions: List[PreprocessorStageFunction] = [
        ]

        # Functions that determne which tokens can be ignored.
        # Must take List[List[str]] as input and return List[List[str]]
        self.__source_filter_functions: List[PreprocessorStageFunction] = []
        self.__target_filter_functions: List[PreprocessorStageFunction] = []

        # Functions that determine which tokens can be ignored
        # but depend on both the source and target artifacts.
        # Must take List[List[str]], List[List[str]] as input and return List[List[str]], List[List[str]]
        self.__universal_filter_functions: List[PreprocessorStageFunction] = []

        # Function that retokenizes after other preprocessing
        # Must take List[str]] as input and return List[List[str]]
        self.__source_posttokenizer_functions: List[PreprocessorStageFunction] = [
        ]
        self.__target_posttokenizer_functions: List[PreprocessorStageFunction] = [
        ]

        self.__prepocess_pipeline_stages = [
            (self.__source_tokenizer_functions, self.__target_tokenizer_functions),
            (self.__source_token_modifier_functions,
             self.__target_token_modifier_functions),
            (self.__source_filter_functions, self.__target_filter_functions),
            # self.__universal_filter_functions,
            (self.__source_posttokenizer_functions,
             self.__target_posttokenizer_functions),
        ]

        self.__corpus_caches: Dict[str, PreprocessorResult] = {}

    @classmethod
    def get_default_preprocessor_instance(cls):
        return cls(
        ).add_tokenize_function(
            cls.Tokenizers.WHITESPACE
        ).add_tokenize_function(
            cls.Tokenizers.CAMEL_CASE,
        ).add_tokenize_function(
            cls.Tokenizers.SNAKE_CASE
        ).add_tokenize_function(
            cls.Tokenizers.SKEWER_CASE
        ).add_tokenize_function(
            cls.Tokenizers.ALPHA_BOUNDARY
        ).add_token_modifier_function(
            cls.TokenModifiers.LOWERCASE
        ).add_token_modifier_function(
            cls.TokenModifiers.STEM
        ).add_filter_function(
            cls.TokenFilters.NUMBERS
        ).add_filter_function(
            cls.TokenFilters.SYMBOLS
        ).add_filter_function(
            cls.TokenFilters.STOP_WORDS
        )
        

    def get_preprocess_stages(self) -> Dict:
        stages = {}
        stage_names = ['tokenize', 'modify', 'filter', 'posttokenize']
        for name, functions in zip(stage_names, self.__preprocess_pipeline_stages):
            source_functions, target_functions = functions
            # TODO: Finish writing this function
        raise NotImplementedError

    def process_corpus(self, corpus: Corpus) -> PreprocessorResult:
        corpus_name = corpus.get_corpus_name()
        if corpus_name in self.__corpus_caches:
            print("Found cached tokens for: {}".format(corpus_name))
            return self.__corpus_caches[corpus_name]

        self.add_universal_argument('currently_processing_corpus', corpus)
        result = self.process(corpus.get_sources(), corpus.get_targets())
        self.__corpus_caches[corpus_name] = result
        return result

    def add_universal_argument(self, arg_name: str, arg_value: any):
        for stage in self.__prepocess_pipeline_stages:
            source_functions, target_functions = stage

            for func_arg_pair in source_functions + target_functions:
                func, args = func_arg_pair
                args[arg_name] = arg_value

    def reset_corpus_cache(self):
        self.__corpus_caches = {}

    def process(self, sources: RawArtifacts, targets: RawArtifacts) -> PreprocessorResult:
        print("Starting processing")
        sources = [[doc] for doc in sources]
        targets = [[doc] for doc in targets]

        for stage in self.__prepocess_pipeline_stages:
            sources, targets = self.execute_preprocess_pipeline_stage(
                stage, sources, targets)

        # Build final vocabulary
        source_vocab = {
            term for artifact in sources for term in artifact
        }
        target_vocab = {
            term for artifact in targets for term in artifact
        }
        print("Finished")
        return PreprocessorResult(sources, targets, source_vocab, target_vocab)

    def execute_preprocess_pipeline_stage(self,
                                          stage_functions: Tuple[List[PreprocessorStageFunction], List[PreprocessorStageFunction]],
                                          sources: TokenizedArtifacts,
                                          targets: TokenizedArtifacts) -> PreprocessorStageOutput:

        source_stage_functions, target_stage_functions = stage_functions

        processed_sources = sources
        for func, args in source_stage_functions:
            processed_sources = func(processed_sources, args)

        processed_targes = targets
        for func, args in target_stage_functions:
            processed_targes = func(processed_targes, args)

        return processed_sources, processed_targes

    def add_tokenize_function(self, func, args=None, sources=True, targets=True):
        if sources:
            self.__source_tokenizer_functions.append((func, {} if args is None else args))

        if targets:
            self.__target_tokenizer_functions.append(
                (func, {} if args is None else args))

        self.reset_corpus_cache()
        return self

    def add_token_modifier_function(self, func, args=None, sources=True, targets=True):
        if sources:
            self.__source_token_modifier_functions.append(
                (func, {} if args is None else args))

        if targets:
            self.__target_token_modifier_functions.append(
                (func, {} if args is None else args))

        self.reset_corpus_cache()
        return self

    def add_filter_function(self, func, args=None, sources=True, targets=True):
        if sources:
            self.__source_filter_functions.append(
                (func, {} if args is None else args))

        if targets:
            self.__target_filter_functions.append(
                (func, {} if args is None else args))

        self.reset_corpus_cache()
        return self

    def add_posttokenize_function(self, func, args=None, sources=True, targets=True):
        if sources:
            self.__source_posttokenizer_functions.append(
                (func, {} if args is None else args))

        if targets:
            self.__target_posttokenizer_functions.append(
                (func, {} if args is None else args))

        self.reset_corpus_cache()
        return self

    class Tokenizers:
        @classmethod
        def WHITESPACE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            subtokenized_artifacts = []
            for artifact in artifacts:
                new_tokenized_artifact = []
                for token in artifact:
                    new_tokenized_artifact += token.split()
                subtokenized_artifacts.append(new_tokenized_artifact)
            return subtokenized_artifacts

        @classmethod
        def CAMEL_CASE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            subtokenized_artifacts = []
            for artifact in artifacts:
                new_tokenized_artifact = []
                for token in artifact:
                    new_tokenized_artifact += re.sub(
                        '([a-z])([A-Z])', r'\1 \2', token).split()
                subtokenized_artifacts.append(new_tokenized_artifact)
            return subtokenized_artifacts

        @classmethod
        def SNAKE_CASE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            subtokenized_artifacts = []
            for artifact in artifacts:
                new_tokenized_artifact = []
                for token in artifact:
                    new_tokenized_artifact += re.sub(
                        '([A-Za-z])?_([A-Za-z])', r'\1 \2', token).split()
                subtokenized_artifacts.append(new_tokenized_artifact)
            return subtokenized_artifacts

        @classmethod
        def SKEWER_CASE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            subtokenized_artifacts = []
            for artifact in artifacts:
                new_tokenized_artifact = []
                for token in artifact:
                    new_tokenized_artifact += re.sub(
                        '([A-Za-z])?-([A-Za-z])', r'\1 \2', token).split()
                subtokenized_artifacts.append(new_tokenized_artifact)
            return subtokenized_artifacts

        @classmethod
        def ALPHA_BOUNDARY(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            subtokenized_artifacts = []
            for artifact in artifacts:
                new_tokenized_artifact = []
                for token in artifact:
                    new_tokenized_artifact += re.sub('([a-zA-Z])([^a-zA-Z])', r'\1 \2',
                                                     re.sub('([^a-zA-Z])([a-zA-Z])', r'\1 \2', token)).split()
                subtokenized_artifacts.append(new_tokenized_artifact)
            return subtokenized_artifacts

    class TokenModifiers:
        @classmethod
        def LOWERCASE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            return [[token.lower() for token in artifact]for artifact in artifacts]

        # args: {
        #   stemmers: List[NLTK stemmer]
        # }
        @classmethod
        def STEM(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            if 'stemmers' in args:
                stemmers = args['stemmers']
            else:
                print("Using default English stemmer")
                stemmers = [SnowballStemmer('english')]

            stemmed_artifacts = []

            for artifact in artifacts:
                new_artifact = list(artifact)
                for i, token in enumerate(artifact):
                    for stemmer in stemmers:
                        stemmed = stemmer.stem(token)
                        if stemmed != token:
                            new_artifact[i] = stemmed
                            break
                stemmed_artifacts.append(new_artifact)

            return stemmed_artifacts

    class TokenFilters:

        # args: {
        #   stop_words: List[str],
        #   stop_words_path
        # }
        @classmethod
        def STOP_WORDS(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            filtered_artifacts = []


            if 'stop_words' in args:
                stop_words = set(args['stop_words'])
            elif 'stop_words_path' in args:
                # TODO: Implement getting stop words from file
                raise NotImplementedError
            elif 'currently_processing_corpus' in args:
                stop_words = args['currently_processing_corpus'].get_stop_words()
                print("Success!")
            else:
                return artifacts

            for artifact in artifacts:
                filtered_artifacts.append(
                    [token for token in artifact if token not in stop_words])

            return filtered_artifacts

        @classmethod
        def NUMBERS(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            filtered_artifacts = []

            for artifact in artifacts:
                filtered_artifacts.append(
                    [token for token in artifact if not token.isnumeric()])

            return filtered_artifacts

        @classmethod
        def SYMBOLS(cls, artifacts: TokenizedArtifacts, args=None) -> TokenizedArtifacts:
            filtered_artifacts = []

            for artifact in artifacts:
                filtered_artifacts.append(
                    [token for token in artifact if token.isalnum()])

            return filtered_artifacts

    class UniversalTokenFilters:
        @classmethod
        def NONCOMMON_VOCAB(cls, source_artifacts: TokenizedArtifacts, target_artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            # TODO
            pass

    class Posttokeniers:

        # args: {
        #   use_pretrained_model: bool,
        #   pretrained_model_path: str,
        #   
        #   vocab_size: int
        # }
        @classmethod
        def BPE(cls, artifacts: TokenizedArtifacts, args: PreprocessorStageFunctionArgs) -> TokenizedArtifacts:
            artifacts = [' '.join(artifact) for artifact in artifacts]
            
            if args.get('use_pretrained_model', False):
                pretrained_model_path = args.get('pretrained_model_path', None)
                if not pretrained_model_path:
                    raise AttributeError

                bpe_model = spm.SentencePieceProcessor()
                bpe_model.load(pretrained_model_path)

                return [bpe_model.encode_as_pieces(artifact) for artifact in artifacts]
            else:
                # Train on corpus
                raise NotImplementedError


Preprocessor.PIPELINE_STAGE_NAMES = {
    'default': 'CUSTOM_FUNCTION',

    Preprocessor.Tokenizers.WHITESPACE: 'TOKENIZE_ON_WHITESPACE',
    Preprocessor.Tokenizers.CAMEL_CASE: 'TOKENIZE_ON_CAMEL_CASE',
    Preprocessor.Tokenizers.SNAKE_CASE: 'TOKENIZE_ON_SNAKE_CASE',
    Preprocessor.Tokenizers.ALPHA_BOUNDARY: 'TOKENIZE_ON_ALPHA_BOUNDARY',

    Preprocessor.TokenModifiers.LOWERCASE: 'LOWERCASE',
    Preprocessor.TokenModifiers.STEM: 'STEM',

    Preprocessor.TokenFilters.STOP_WORDS: 'FILTER_STOP_WORDS',
    Preprocessor.TokenFilters.NUMBERS: 'FILTER_NUMBERS',
    Preprocessor.TokenFilters.SYMBOLS: 'FILTER_SYMBOLS',

    Preprocessor.Posttokeniers.BPE: 'BPE',
}


if __name__ == '__main__':
    preprocessor = Preprocessor.get_default_preprocessor_instance()
    sources = ["helloThere",
               "my fri_end isGood I am going to be hearing what you wrote", "texa123s isF0RU"]
    targets = ["how are you?", "pretty good"]

    corpus = Corpus.get_preset_corpus('0_1')
    result = preprocessor.process_corpus(corpus)

    print(corpus.get_source_names()[0])
    print(result.processed_sources[0])
