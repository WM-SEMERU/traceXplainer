import sentencepiece as spm
import os

# Path to your merged CSN corpus
CORPUS = "pretrain_corpus_code_wiki.txt"  

# Where to write your models
OUT_DIR = "models"
os.makedirs(OUT_DIR, exist_ok=True)

# (vocab_size, filename prefix)
for vocab_size, prefix in [(8000, "bpe8k"),
                           (32000, "bpe32k"),
                           (128000, "bpe128k")]:
    model_path = os.path.join(OUT_DIR, prefix)
    spm.SentencePieceTrainer.Train(
        input=CORPUS,
        model_prefix=model_path,
        vocab_size=vocab_size,
        model_type="bpe",
        character_coverage=1.0,
        pad_id=0,
        unk_id=1,
        bos_id=2,
        eos_id=3,
        user_defined_symbols=[],
        input_sentence_size=0,
        shuffle_input_sentence=True,
    )
    print(f"Trained {prefix}: {model_path}.model (+ .vocab)")