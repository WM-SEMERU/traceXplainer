# Analysis is for LibEST 0_0 by default
# Script will work for any traceability dataset

# To change data set, find the line number that separates the NL from
# the code and set the sep_line variable equal to that line number
# minus 1
# Also change the fname variable to point to the raw corpus file for
# the data set you want to analyze

# BPE model trained on big NL/code dataset is used here
# with vocab size 2000

import sentencepiece as spm
import smart_open
import math

# load BPE model
bpe_model = spm.SentencePieceProcessor()
bpe_model.load('../../../data/pretrained_models/bpe_models/big_bpe_2000.model')

cur_dataset = 'LibEST'

# filename here
fname = '../../../data/raw/LibEST_semeru_format/0_0_raw_corpus.txt'

# line separating requirements and code
sep_line = 1155

# lists of requirements tokens and code tokens
req_tokens = []
code_tokens = []

# populate lists of requirements and code tokens as BPE tokens
with smart_open.open(fname) as f:
    for i, line in enumerate(f):
        if line != '\n':
            if i >= sep_line:
                code_tokens.append(bpe_model.encode_as_pieces(line.strip()))
            else:
                req_tokens.append(bpe_model.encode_as_pieces(line.strip()))

req_freq_dict = {}
code_freq_dict = {}

# populate frequency dictionaries
for r_tokenized_line in req_tokens:
    for r_token in r_tokenized_line:
        if r_token not in req_freq_dict:
            req_freq_dict[r_token] = 1
        else:
            req_freq_dict[r_token] += 1

for c_tokenized_line in code_tokens:
    for c_token in c_tokenized_line:
        if c_token not in code_freq_dict:
            code_freq_dict[c_token] = 1
        else:
            code_freq_dict[c_token] += 1

# calculate relative frequencies and unique tokens to each set
total_req_tokens = sum(req_freq_dict.values())
total_code_tokens = sum(code_freq_dict.values())
total_tokens = total_req_tokens + total_code_tokens

req_rel_freqs = {}
code_rel_freqs = {}
tot_rel_freqs = {}

req_unique_toks = []
code_unique_toks = []
shared_toks = []

for key in req_freq_dict.keys():
    # calculate frequency of current token
    cur_freq = req_freq_dict[key]
    # add current token's relative frequency to its dict
    req_rel_freqs[key] = cur_freq/total_req_tokens

    # if token is in both dictionaries (code and requirements)
    if key in code_freq_dict.keys():
        # calculate the total frequency of the token across both dictionaries
        combined_freq = cur_freq + code_freq_dict[key]
        # add its total relative frequency to the appropriate dictionary
        tot_rel_freqs[key] = combined_freq/total_tokens
        # add it to shared tokens list
        shared_toks.append([key, combined_freq])
    # if token is only in requirements dictionary
    else:
        # add its total frequency to the appropriate dictionary
        tot_rel_freqs[key] = cur_freq/total_tokens
        # add current token and its frequency to the unique list
        req_unique_toks.append([key, cur_freq])

for key in code_freq_dict.keys():
    # calculate frequency of current token
    cur_freq = code_freq_dict[key]
    # add current token's relative frequency to its dict
    code_rel_freqs[key] = cur_freq/total_code_tokens

    # if token is only in code dictionary
    if key not in tot_rel_freqs.keys():
        # add its total frequency to the appropriate dictionary
        tot_rel_freqs[key] = cur_freq/total_tokens
        # add current token and its frequency to the unique list
        code_unique_toks.append([key, cur_freq])

# sort token lists into descending order
req_unique_toks.sort(key=lambda x: x[1], reverse=True)
code_unique_toks.sort(key=lambda x: x[1], reverse=True)
shared_toks.sort(key=lambda x: x[1], reverse=True)

output_filename = 'entropy_results/' + cur_dataset + '_analysis_results.txt'
output_file = open(output_filename, 'w')

# print out relevant data
print("\nRequirements Unique Tokens:")
output_file.write("\nRequirements Unique Tokens:\n")
for pair in req_unique_toks:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\nCode Requirements Unique Tokens:")
output_file.write("\nCode Requirements Unique Tokens:\n")
for pair in code_unique_toks:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 most frequent tokens present in both sets:")
output_file.write("\n20 most frequent tokens present in both sets:\n")
for pair in shared_toks[:20]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 least frequent tokens present in both sets:")
output_file.write("\n20 least frequent tokens present in both sets:\n")
for pair in shared_toks[-20:]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 most frequent tokens in requirements:")
output_file.write("\n20 most frequent tokens in requirements:\n")
for pair in req_unique_toks[:20]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 least frequent tokens in requirements:")
output_file.write("\n20 least frequent tokens in requirements:\n")
for pair in req_unique_toks[-20:]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 most frequent tokens in code:")
output_file.write("\n20 most frequent tokens in code:\n")
for pair in code_unique_toks[:20]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\n20 least frequent tokens in code:")
output_file.write("\n20 least frequent tokens in code:\n")
for pair in code_unique_toks[-20:]:
    print(str(pair[0]) + ": " + str(pair[1]))
    output_file.write(str(pair[0]) + ": " + str(pair[1]) + "\n")

print("\nMost frequent tokens in common overlapping both sets:")
output_file.write("\nMost frequent tokens in common overlapping both sets:\n")
req_most_common = [x[0] for x in req_unique_toks[:20]]
code_most_common = [x[0] for x in code_unique_toks[:20]]
count = 0
for pair in shared_toks[:20]:
    if pair[0] in req_most_common and pair[0] in code_most_common:
        print(pair[0])
        output_file.write(pair[0] + "\n")
        count += 1
print("Total: " + str(count))
output_file.write("Total: " + str(count) + "\n")

print("\nLeast frequent tokens in common overlapping both sets:")
output_file.write("\nLeast frequent tokens in common overlapping both sets:\n")
req_least_common = [x[0] for x in req_unique_toks[-20:]]
code_least_common = [x[0] for x in code_unique_toks[-20:]]
count = 0
for pair in shared_toks[-20:]:
    if pair[0] in req_least_common and pair[0] in code_least_common:
        print(pair[0])
        output_file.write(pair[0] + "\n")
        count += 1
print("Total: " + str(count))
output_file.write("Total: " + str(count) + "\n")

####################################################

# Entropy calculations below here

####################################################

# p = src code, q = reqs, w = all

# Self-information measure H(X) for the probability distribution
# p given a set X of tokens, also for q and w

def self_information(tokens, freqs):
    output = 0

    for tok in tokens:
        cur_freq = freqs[tok]
        output += log((1/cur_freq)**cur_freq)

    return output

# Cross-entropy error H(p,q) given set X of tokens

def cross_entropy(tokens, p, q):
    pass

# KL-Divergence D(p||q) and D(q||p)

def KL_divergence(tokens, p, q):
    pass

output_file.close()














