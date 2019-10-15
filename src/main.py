from evaluation.Corpus import Corpus

if __name__ == '__main__':
    c = Corpus.get_preset_corpus('0_1')
    print(c.get_source_names())
