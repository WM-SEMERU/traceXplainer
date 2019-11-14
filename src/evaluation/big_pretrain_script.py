'''
Greg Pennisi
November 2019

'''
import sys
sys.path.append('../..')

import gensim.models as gm
from gensim.models import Word2Vec
import sentencepiece as spm

def generate_model(corpus, vector_size=100, min_count=2, epochs=50, model_type='sg'):

    if model_type == 'sg':
        # skip-gram word2vec here
        print("Training Skip-Gram word2vec model")
        word2vec_model = Word2Vec(corpus, sg = 1,
            size = vector_size, min_count = min_count, iter = epochs)
        print("Finished training Skip-Gram word2vec model")

    elif model_type == 'cbow':
        # CBOW word2vec here
        print("Training CBOW word2vec model")
        word2vec_model = Word2Vec(corpus, sg = 0,
            size = vector_size, min_count = min_count, iter = epochs)
        print("Finished training CBOW word2vec model")

    elif model_type == 'doc2vec':
        # doc2vec here
        doc2vec_model = gensim.models.doc2vec.Doc2Vec(
            vector_size=vector_size,
            min_count=min_count,
            epochs=epochs
        )
        ##########
        doc2vec_model.build_vocab(train_corpus)
        print("Training doc2vec model")
        doc2vec_model.train(
            train_corpus,
            total_examples=doc2vec_model.corpus_count,
            epochs=doc2vec_model.epochs
        )
        ##########
        print("Done training")

    else:
        print("Invalid model type. Valid model types: sg, cbow, doc2vec")

# For BPE: 2000/5000/10000
# Vector_size: 100, 300
# Epoch: 50

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

    corpus = []

    # for each line in the input training file, process with BPE and add to corpus

    big_text_file = open('/../../data/training/training_corpus.txt', 'r')
    for line in big_text_file:
        stripped = line.strip()
        encoded = bpe_model.encode_as_pieces(artifact)
        corpus.append(encoded)

    for j in range(2):
        if j == 0:
            vec_size = 100
        else:
            vec_size = 300

        generate_model(corpus, vector_size=vec_size, model_type='sg')
        # add code to save model here
        generate_model(corpus, vector_size=vec_size, model_type='cbow')
        # add code to save model here
        generate_model(corpus, vector_size=vec_size, model_type='doc2vec')
        # add code to save model here
