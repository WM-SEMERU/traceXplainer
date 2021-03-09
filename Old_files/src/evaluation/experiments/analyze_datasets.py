
import sys
sys.path.append('../..')

from evaluation.Corpus import Corpus
from evaluation.Corpus_Analyzer import Corpus_Analyzer

corpus = Corpus.get_preset_corpus('6_0')
analyzer = Corpus_Analyzer(corpus)

analyzer.print_report()