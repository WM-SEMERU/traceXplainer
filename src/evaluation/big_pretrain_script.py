'''
Greg Pennisi
November 2019

'''

# This file should be placed in /src/evaluation/

# Big training data file should be placed in /data/training/
# and should be named training_corpus.txt

# BPE model files should be in /src/evaluation/bpe_models/

import sys
sys.path.append('../..')

import smart_open
import gensim.models as gm
from gensim.models import Word2Vec
import sentencepiece as spm

def prepare_corpus_d2v(fname, bpe_model):
    with smart_open.open(fname) as f:
        for i, line in enumerate(f):
            tokens = bpe_model.encode_as_pieces(line)
            # For training data, add tags
            yield gm.doc2vec.TaggedDocument(tokens, [i])

def prepare_corpus_w2v(fname, bpe_model):
    encoded_corpus = []
    f = open(fname, 'r')
    for line in f:
        encoded_corpus.append(bpe_model.encode_as_pieces(line.strip()))
    return encoded_corpus

def generate_model(corpus, vocab_size, vector_size=100, min_count=2, epochs=50, model_type='sg'):

    if model_type == 'sg':
        # skip-gram word2vec here
        print("Training Skip-Gram word2vec model with vector size {}, min count {}, and BPE vocab size {}".format(vector_size, min_count, vocab_size))
        word2vec_model = Word2Vec(corpus, sg = 1,
            size = vector_size, min_count = min_count, iter = epochs)
        print("Finished training Skip-Gram word2vec model\n")

        # add code to save model here

    elif model_type == 'cbow':
        # CBOW word2vec here
        print("Training CBOW word2vec model with vector size {}, min count {}, and BPE vocab size {}".format(vector_size, min_count, vocab_size))
        word2vec_model = Word2Vec(corpus, sg = 0,
            size = vector_size, min_count = min_count, iter = epochs)
        print("Finished training CBOW word2vec model\n")

        # add code to save model here

    elif model_type == 'doc2vec':
        # doc2vec here
        doc2vec_model = gm.doc2vec.Doc2Vec(
            vector_size=vector_size,
            min_count=min_count,
            epochs=epochs
        )
        doc2vec_model.build_vocab(corpus)
        print("Training doc2vec model with vector size {}, min count {}, and BPE vocab size {}".format(vector_size, min_count, vocab_size))
        doc2vec_model.train(
            corpus,
            total_examples=doc2vec_model.corpus_count,
            epochs=doc2vec_model.epochs
        )
        print("Finished training doc2vec model\n")

        # add code to save model here

    else:
        print("Invalid model type. Valid model types: sg, cbow, doc2vec")

# For BPE: 2000/5000/10000
# Vector_size: 100, 300
# Epoch: 50

# Path to training corpus file relative to current directory
filename = '../../data/training/training_corpus.txt'

# Main loop to generate all the different model types
for i in range(3):

    if i == 0:
        vocab_size = 2000
    elif i == 1:
        vocab_size = 5000
    else:
        vocab_size = 10000

    bpe_model = spm.SentencePieceProcessor()
    model_name = 'bpe_models/big_bpe_{}.model'.format(vocab_size)
    bpe_model.load(model_name)

    w2v_input = prepare_corpus_w2v(filename, bpe_model)
    d2v_input = list(prepare_corpus_d2v(filename, bpe_model))

    # for each line in the input training file, process with BPE and add to corpus

    for j in range(2):
        if j == 0:
            vec_size = 100
        else:
            vec_size = 300

        generate_model(w2v_input, vocab_size=vocab_size, vector_size=vec_size, model_type='sg')
        #generate_model(w2v_input, vocab_size=vocab_size, vector_size=vec_size, model_type='cbow')

        generate_model(d2v_input, vocab_size=vocab_size, vector_size=vec_size, model_type='doc2vec')
