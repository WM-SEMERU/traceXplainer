'''
Daniel McCrystal
Oct 2019
'''

from .IR_Method import IR_Method

class Corpus_Analyzer(IR_Method):

    def __init__(self, *args, **kwargs):
        super(Corpus_Analyzer, self).__init__(*args, **kwargs)
       
        self.__frequency_map = None
        self.__source_frequency_map = None
        self.__target_frequency_map = None

    def print_report(self):
        print("====================================")
        print("Corpus analysis for: {}\n".format(self._corpus.get_corpus_name()))

        print("Total num documents:\t{}".format(self.get_num_documents()))
        print("Num source documents:\t{}".format(self.get_num_source_documents()))
        print("Num target documents:\t{}".format(self.get_num_target_documents()))
        
        print()

        print("Total num trace links:\t{}".format(self.get_num_potential_trace_links()))

        print()

        print("Total vocabulary size:\t{}".format(self.get_total_vocab_size()))
        print("Source vocabulary size:\t{}".format(self.get_source_vocab_size()))
        print("Target vocabulary size:\t{}".format(self.get_target_vocab_size()))
        print("Shared vocabulary size:\t{}".format(self.get_shared_vocab_size()))

        print()

        print("Avg num tokens per document:\t{}".format(self.get_avg_tokens_per_document()))
        print("Avg num tokens per source doc:\t{}".format(self.get_avg_tokens_per_source_document()))
        print("Avg num tokens per target doc:\t{}".format(self.get_avg_tokens_per_target_document()))

        print()
        
        print("Most frequent tokens:")
        for token, freq in self.get_most_frequent_tokens():
            print("\t{} : {}".format(token, freq))

        print()
        
        print("Least frequent tokens:")
        for token, freq in self.get_least_frequent_tokens():
            print("\t{} : {}".format(token, freq))

        print()

        print("Most frequent source tokens:")
        for token, freq in self.get_most_frequent_source_tokens():
            print("\t{} : {}".format(token, freq))

        print()
        
        print("Least frequent source tokens:")
        for token, freq in self.get_least_frequent_source_tokens():
            print("\t{} : {}".format(token, freq))

        print()

        print("Most frequent target tokens:")
        for token, freq in self.get_most_frequent_target_tokens():
            print("\t{} : {}".format(token, freq))

        print()
        
        print("Least frequent target tokens:")
        for token, freq in self.get_least_frequent_target_tokens():
            print("\t{} : {}".format(token, freq))

        print()

        print("====================================")

    def get_total_vocab_size(self):
        return len(self._source_vocab.union(self._target_vocab))

    def get_shared_vocab_size(self):
        return len(self._common_vocab)

    def get_source_vocab_size(self):
        return len(self._source_vocab)

    def get_target_vocab_size(self):
        return len(self._target_vocab)

    def get_num_documents(self):
        return len(self._processed_sources) + len(self._processed_targets)

    def get_num_source_documents(self):
        return len(self._processed_sources)

    def get_num_target_documents(self):
        return len(self._processed_targets)

    def get_num_potential_trace_links(self):
        return len(self._processed_sources) * len(self._processed_targets)

    def __get_avg_tokens_per_document(self, documents):
        total_num_tokens = 0
        for doc in documents:
            total_num_tokens += len(doc)

        return total_num_tokens // len(documents)

    def get_avg_tokens_per_document(self):
        return self.__get_avg_tokens_per_document(self._processed_sources + self._processed_targets)

    def get_avg_tokens_per_source_document(self):
        return self.__get_avg_tokens_per_document(self._processed_sources)

    def get_avg_tokens_per_target_document(self):
        return self.__get_avg_tokens_per_document(self._processed_targets)

    def __build_frequency_map(self, documents, map_obj):
        
        for doc in documents:
            for token in doc:
                if token not in map_obj:
                    map_obj[token] = 0
                
                map_obj[token] += 1

    def get_most_frequent_tokens(self, n=5):
        if self.__frequency_map is None:
            self.__frequency_map = {}
            self.__build_frequency_map(self._processed_sources + self._processed_targets, self.__frequency_map)

        return sorted(self.__frequency_map.items(), key=lambda x: x[1], reverse=True)[:n]

    def get_least_frequent_tokens(self, n=5):
        if self.__frequency_map is None:
            self.__frequency_map = {}
            self.__build_frequency_map(self._processed_sources + self._processed_targets, self.__frequency_map)

        return sorted(self.__frequency_map.items(), key=lambda x: x[1])[:n]

    def get_most_frequent_source_tokens(self, n=5):
        if self.__source_frequency_map is None:
            self.__source_frequency_map = {}
            self.__build_frequency_map(self._processed_sources, self.__source_frequency_map)

        return sorted(self.__source_frequency_map.items(), key=lambda x: x[1], reverse=True)[:n]

    def get_least_frequent_source_tokens(self, n=5):
        if self.__source_frequency_map is None:
            self.__source_frequency_map = {}
            self.__build_frequency_map(self._processed_sources, self.__source_frequency_map)

        return sorted(self.__source_frequency_map.items(), key=lambda x: x[1])[:n]

    def get_most_frequent_target_tokens(self, n=5):
        if self.__target_frequency_map is None:
            self.__target_frequency_map = {}
            self.__build_frequency_map(self._processed_targets, self.__target_frequency_map)

        return sorted(self.__target_frequency_map.items(), key=lambda x: x[1], reverse=True)[:n]

    def get_least_frequent_target_tokens(self, n=5):
        if self.__target_frequency_map is None:
            self.__target_frequency_map = {}
            self.__build_frequency_map(self._processed_targets, self.__target_frequency_map)

        return sorted(self.__target_frequency_map.items(), key=lambda x: x[1])[:n]
