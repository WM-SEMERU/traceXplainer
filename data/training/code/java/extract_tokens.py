import json


partition_ratios = {
    'bpe': 0.1,
    'validation': 0.1,
    'test': 0.1,
    'train': 0.7
}

output_base_name = 'java'

total_token_count = 0
with open('java_test_0.jsonl', 'r') as jsonl_file:
    # Look ahead to find total number of tokens
    for line in jsonl_file:
        json_obj = json.loads(line)
        total_token_count += len(json_obj['code_tokens'])

print("Found {} total tokens".format(total_token_count))

partition_token_count_goals = dict()
for partition_name, ratio in partition_ratios.items():
    partition_token_count_goals[partition_name] = int(total_token_count * ratio)

partition_names = list(partition_ratios.keys())
partition_name_index = 0
current_partition_name = partition_names[partition_name_index]
current_token_counts = {name: 0 for name in partition_names}

output_file = open('partitions/{}_{}.txt'.format(output_base_name, current_partition_name), 'w+')
with open('java_test_0.jsonl', 'r') as jsonl_file:
    for line in jsonl_file:
        json_obj = json.loads(line)
        code = json_obj['code']
        code_tokens = json_obj['code_tokens']
        if current_token_counts[current_partition_name] < partition_token_count_goals[current_partition_name]:
            current_token_counts[current_partition_name] += len(code_tokens)
        else:
            print("Finished assigning documents to {}".format(current_partition_name))
            partition_name_index += 1
            if partition_name_index < len(partition_names):
                current_partition_name = partition_names[partition_name_index]
            else:
                break
            current_token_counts[current_partition_name] = len(code_tokens)
            output_file.close()
            output_file = open('partitions/{}_{}.txt'.format(output_base_name, current_partition_name), 'w+')

        output_file.write(code.replace('\n', '').replace('\r', '') + '\n')
print("Finished assigning documents to {}".format(current_partition_name))
output_file.close()

print()
print("============= Validate Ratios =============")
for partition_name, token_count in current_token_counts.items():
    print("{}: {} / {} => {}".format(partition_name, token_count, total_token_count, token_count / total_token_count))
print("===========================================")
