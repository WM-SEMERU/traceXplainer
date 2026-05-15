#!/usr/bin/env python3

import os
import pandas as pd
from gensim.models import Doc2Vec
from scipy.spatial import distance
from itertools import product
from datetime import datetime

# --- Configuration Parameters ---
SYSTEM_CSV = '../dvc-data/systems/dronology/corpus/dronology_conv_with_bpe_fixed.csv'
SEP = '~'
MODEL_PATH = '../dvc-data/systems/dronology/2vec_models/bpe32k/doc2vec/[doc2vec-py-java-PVDBOW-500-20E-32k-c-1751567950.521369].model'
MAPPING_CSV = '../dvc-data/systems/dronology/mappings/conventional.csv'
OUTPUT_DIR = '../dvc-data/systems/dronology/experiment4.1.3/results/'
INFER_STEPS = 200

# --- Helper Functions ---

def load_dataset(system_csv, sep):
    df = pd.read_csv(system_csv, sep=sep, header=0)
    df['bpe32k'] = df.get('bpe32k', '').fillna('')
    df_src = df[df.get('type') == 'req'][['ids', 'bpe32k']].reset_index(drop=True)
    df_trg = df[df.get('type') == 'code'][['ids', 'bpe32k']].reset_index(drop=True)
    return df_src, df_trg


def infer_vectors(df, model_path, epochs):
    model = Doc2Vec.load(model_path)
    model.init_sims(replace=True)
    return pd.DataFrame({
        'ids': df['ids'],
        'vector': df['bpe32k'].apply(lambda doc: model.infer_vector(doc.split(), epochs=epochs))
    })


def compute_metrics(src_df, trg_df):
    funcs = {
        'EUC': (distance.euclidean, lambda d: 1/(1+d)),
        'COS': (distance.cosine,   lambda d: 1-d),
        'MAN': (distance.cityblock,lambda d: 1/(1+d)),
    }
    records = []
    for src_id, trg_id in product(src_df['ids'], trg_df['ids']):
        u = src_df.loc[src_df['ids']==src_id,'vector'].iloc[0]
        v = trg_df.loc[trg_df['ids']==trg_id,'vector'].iloc[0]
        rec = {'Source': src_id, 'Target': trg_id}
        for name,(dist_fn, sim_fn) in funcs.items():
            d = dist_fn(u, v)
            rec[f'{name}_dist'] = d
            rec[f'{name}_sim'] = sim_fn(d)
        records.append(rec)
    return pd.DataFrame.from_records(records)


def load_ground_truth(path):
    dfm = pd.read_csv(path, header=0)
    cols = dfm.columns.tolist()
    if 'id_pr' in cols and 'doc_id' in cols:
        srcs = dfm['id_pr'].astype(str)
        trgs = dfm['doc_id'].astype(str)
    else:
        srcs = dfm.iloc[:,0].astype(str)
        trgs = dfm.iloc[:,1].astype(str)
    gt = set(zip(srcs, trgs))
    print(f'Loaded {len(gt)} ground-truth pairs')
    return gt


def extract_artifact_id(path):
    """
    Given a file path or ID string ending in 'XYZ.ext', returns 'XYZ'.
    """
    base = os.path.basename(str(path))
    return os.path.splitext(base)[0]


def assign_link_flags(df, gt_set):
    df['req_id']  = df['Source'].map(extract_artifact_id)
    df['code_id'] = df['Target'].map(extract_artifact_id)
    df['Linked?'] = df.apply(
        lambda x: 1 if (x['req_id'], x['code_id']) in gt_set else 0,
        axis=1
    )
    npos = int(df['Linked?'].sum())
    print(f'Assigned Linked? flags: {npos} positives out of {len(df)} pairs')
    df.drop(columns=['req_id','code_id'], inplace=True)
    return df

# --- Main Execution ---
if __name__ == '__main__':
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print('Loading data...')
    src_df, trg_df = load_dataset(SYSTEM_CSV, SEP)

    print('Inferring vectors...')
    src_vecs = infer_vectors(src_df, MODEL_PATH, INFER_STEPS)
    trg_vecs = infer_vectors(trg_df, MODEL_PATH, INFER_STEPS)

    print('Computing metrics...')
    df_all = compute_metrics(src_vecs, trg_vecs)

    print('Loading ground truth...')
    gt_set = load_ground_truth(MAPPING_CSV)

    print('Assigning Linked? flags...')
    df_all = assign_link_flags(df_all, gt_set)

    # Save single CSV
    timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
    fname = f'dronology_doc2vec_all_with_labels_{timestamp}.csv'
    out = os.path.join(OUTPUT_DIR, fname)
    df_all.to_csv(out, index=False)
    print(f'Saved results with labels to: {out}')
