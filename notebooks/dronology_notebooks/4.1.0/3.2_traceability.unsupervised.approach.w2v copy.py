#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Standalone script to run conventional Word2Vec-based unsupervised traceability
experiments on the Dronology dataset (3.2 notebook refactored).
"""
import os
import logging
import pandas as pd
import numpy as np
from itertools import product
from random import sample
import functools
from enum import Enum, auto
from datetime import datetime
from sentencepiece import SentencePieceProcessor
import types

import gensim
from gensim import corpora
from gensim.models import Word2Vec
from gensim.similarities import WordEmbeddingSimilarityIndex, SparseTermSimilarityMatrix
from scipy.spatial import distance

# Ensure Counter is available for MSI
from collections import Counter

# --- Enums and configuration ---
class Preprocessing(Enum):
    conv = auto()
    bpe = auto()

class DistanceMetric(Enum):
    WMD = auto()
    COS = auto()
    SCM = auto()
    EUC = auto()
    MAN = auto()

# --- Parameter loader for Dronology ---
def dronology_params():
    base = '../dvc-data/systems/dronology'
    return {
        'system': 'dronology',
        'vectorizationType': 'word2vec',
        'linkType': 'req2src',
        'path_to_trained_model': '../dvc-data/systems/dronology/2vec_models/models/word2vec_bpe8k_java_py.model',
        'source_type': 'req',
        'target_type': 'code',
        'system_path_config': {
            'system_path': os.path.join(base, 'dronology_conv_with_bpe_clean.csv'),
            'sep': '~',
            'prep': Preprocessing.bpe,
            'names': ['ids', 'bpe8k'],
            "bpe_model_path": '../dvc-data/systems/dronology/bpe_models/java_py_bpe8k.model'
        },
        'saving_path': os.path.join(base, 'experiment4.1.0/results') + os.sep,
        'names': ['Source', 'Target', 'Linked?']
    }

# --- Core Classes ---
class BasicSequenceVectorization:
    def __init__(self, params):
        self.params = params
        self.prep = None  # placeholder if needed
        cfg = params['system_path_config']
        self.df_all_system = pd.read_csv(
            cfg['system_path'], sep=cfg['sep'], header=0, index_col=None
        )
        if 'tokens' in self.df_all_system.columns:
            self.df_all_system.rename(columns={'tokens': 'conv'}, inplace=True)
        if self.df_all_system.index.name == 'ids':
            self.df_all_system.reset_index(inplace=True)
        names = cfg['names']
        self.df_all_system['type'] = self.df_all_system['type'].replace({
            params['source_type']: params['source_type'],
            params['target_type']: params['target_type']
        })
        self.df_source = self.df_all_system.loc[
            self.df_all_system['type'] == params['source_type'], names
        ].fillna('')
        self.df_target = self.df_all_system.loc[
            self.df_all_system['type'] == params['target_type'], names
        ].fillna('')
        tag = names[1]
        if cfg.get('prep') == Preprocessing.bpe:
            sp = SentencePieceProcessor()
            sp.load(cfg['bpe_model_path'])
            self.prep = types.SimpleNamespace(sp_bpe=sp)
            token_col = names[1]
        docs = [
            d.split() 
            for d in self.df_all_system[tag].fillna('').tolist() 
            if d.strip()
        ]        
        self.dictionary = corpora.Dictionary(docs)
        logging.info('Built dictionary from conv corpus')
        self.vocab = self.dictionary.token2id

    def msi(self, sentence_a, sentence_b):
        """
        Minimum Shared Information: Shannon entropy (I) and extropy (X) over shared tokens
        """
        cnt1 = Counter(sentence_a)
        cnt2 = Counter(sentence_b)
        tokens = list(self.vocab.keys())
        shared = np.array([min(cnt1.get(t,0), cnt2.get(t,0)) for t in tokens], dtype=float)
        total = shared.sum()
        if total == 0:
            return 0.0, 0.0
        p = shared / total
        mask = p > 0
        I = -np.sum(p[mask] * np.log2(p[mask]))
        q = 1 - p
        mask_q = q > 0
        X = -np.sum(q[mask_q] * np.log2(q[mask_q]))
        return I, X

    def samplingLinks(self, sampling=False, samples=10, basename=False):
        src = self.df_source['ids'].values
        tgt = self.df_target['ids'].values
        links = sample(list(product(src, tgt)), samples) if sampling else list(product(src, tgt))
        if basename:
            links = [(os.path.basename(a), os.path.basename(b)) for a, b in links]
        return links

    def cos_scipy(self, a, b):
        vecs_a = [self.model.wv[token] for token in a if token in self.model.wv.key_to_index]
        vecs_b = [self.model.wv[token] for token in b if token in self.model.wv.key_to_index]
        if not vecs_a or not vecs_b:
            return [1.0, 0.0]
        mean_a = np.mean(vecs_a, axis=0)
        mean_b = np.mean(vecs_b, axis=0)
        c = distance.cosine(mean_a, mean_b)
        return [c, 1 - c]

    def wmd_gensim(self, a, b):
        dist = self.model.wv.wmdistance(a, b)
        return [dist, 1 / (1 + dist)]

    def scm_gensim(self, a, b):
        bow1 = self.dictionary.doc2bow(a)
        bow2 = self.dictionary.doc2bow(b)
        sim = self.sim_matrix.inner_product(bow1, bow2, normalized=(True, True))
        return [1 - sim, sim]

    def distance(self, metrics, link):
        ids, txt = self.params['system_path_config']['names']
        sent_a = self.df_source.loc[self.df_source['ids'] == link[0], txt].values[0].split()
        sent_b = self.df_target.loc[self.df_target['ids'] == link[1], txt].values[0].split()
        vals = []
        for m in metrics:
            vals.extend(self.dispatch[m](sent_a, sent_b))
        return vals

class Word2VecSeqVect(BasicSequenceVectorization):
    def __init__(self, params, distance_metrics):
        super().__init__(params)
        self.model = Word2Vec.load(params['path_to_trained_model'])
        self.model.init_sims(replace=True)
        self.sim_index = WordEmbeddingSimilarityIndex(self.model.wv)
        self.sim_matrix = SparseTermSimilarityMatrix(self.sim_index, self.dictionary)

        self.dispatch = {
            DistanceMetric.COS: self.cos_scipy,
            DistanceMetric.WMD: self.wmd_gensim,
            DistanceMetric.SCM: self.scm_gensim
        }
        self.metrics = distance_metrics

    def compute_and_save(self, sampling=False, samples=None):
        links = self.samplingLinks(sampling, samples)
        rows = []
        total = len(links)
        for i, link in enumerate(links, 1):
            dvals = self.distance(self.metrics, link)
            rows.append([link[0], link[1]] + dvals)
            sent_a = self.df_source.loc[self.df_source['ids']==link[0],'bpe8k'].iat[0].split()
            sent_b = self.df_target.loc[self.df_target['ids']==link[1],'bpe8k'].iat[0].split()
            msi_i, msi_x = self.msi(sent_a, sent_b)
            rows[-1].extend([msi_i, msi_x])
            if i % 100 == 0:
                logging.info(f"Processed {i}/{total} links")
        col_names = (
            ['Source','Target'] +
            [f"{m.name}_dist" for m in self.metrics] +
            [f"{m.name}_sim"  for m in self.metrics] +
            ['MSI_I','MSI_X']
        )
        df = pd.DataFrame(rows, columns=col_names)
        out_path = os.path.join(
            self.params['saving_path'],
            f"{self.params['system']}_distances_{datetime.now().strftime('%Y%m%d%H%M%S')}.csv"
        )
        df.to_csv(out_path, index=False)
        logging.info(f"Saved results to {out_path}")
        return df

# --- Main entrypoint ---
def main():
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s %(levelname)s %(message)s',
        handlers=[logging.StreamHandler()]
    )
    params = dronology_params()
    metrics = [DistanceMetric.COS, DistanceMetric.SCM, DistanceMetric.WMD]
    vect = Word2VecSeqVect(params, metrics)
    vect.compute_and_save(sampling=False)

if __name__ == '__main__':
    main()
