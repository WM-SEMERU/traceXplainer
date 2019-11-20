import sys
sys.path.append('../..')

from evaluation.Corpus import Corpus
from evaluation.word2vec import Word2Vec_IR
from evaluation.Evaluator import Evaluator
from evaluation.VSM import VSM



def test_w2v_sg_bpe2000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=2000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'sg'},
        subtitle="Trained on corpus"
    )
    
    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True, 'fine_tune': False, 'train_type': 'sg'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True, 'fine_tune': True, 'train_type': 'sg'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained, w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")

def test_w2v_cbow_bpe2000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=2000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'cbow'},
        subtitle="Trained on corpus"
    )

    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': False, 'train_type': 'cbow'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': True, 'train_type': 'cbow'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained,
                           w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")

def test_w2v_sg_bpe5000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=5000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'sg'},
        subtitle="Trained on corpus"
    )

    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': False, 'train_type': 'sg'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': True, 'train_type': 'sg'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained,
                           w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")

def test_w2v_cbow_bpe5000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=5000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'cbow'},
        subtitle="Trained on corpus"
    )

    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': False, 'train_type': 'cbow'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': True, 'train_type': 'cbow'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained,
                           w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")

def test_w2v_sg_bpe10000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=10000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'sg'},
        subtitle="Trained on corpus"
    )

    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': False, 'train_type': 'sg'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': True, 'train_type': 'sg'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained,
                           w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")

def test_w2v_cbow_bpe10000():
    w2v_generator = Word2Vec_IR(corpus, bpe=True, bpe_vocab_size=10000)

    w2v_corpus_trained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': False, 'train_type': 'cbow'},
        subtitle="Trained on corpus"
    )

    w2v_pretrained = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': False, 'train_type': 'cbow'},
        subtitle="Pretrained"
    )

    w2v_fine_tuned = w2v_generator.generate_model(
        parameters={'use_pretrained_model': True,
                    'fine_tune': True, 'train_type': 'cbow'},
        subtitle="Fine-tuned"
    )

    evaluator = Evaluator([vsm_model, w2v_corpus_trained,
                           w2v_pretrained, w2v_fine_tuned], corpus)
    print("=== w2v-sg, bpe: 2000 ===")
    evaluator.print_average_precision_report()
    print("=========================")


for corpus in Corpus.get_all_preset_corpora():
    print("START TESTS FOR {}".format(corpus.get_corpus_name()))
    vsm_generator = VSM(corpus)
    vsm_model = vsm_generator.generate_model(subtitle="baseline")
    test_w2v_sg_bpe2000()
    test_w2v_cbow_bpe2000()
    test_w2v_sg_bpe5000()
    test_w2v_cbow_bpe5000()
    test_w2v_sg_bpe10000()
    test_w2v_cbow_bpe10000()
    print("END TESTS FOR {}".format(corpus.get_corpus_name()))
