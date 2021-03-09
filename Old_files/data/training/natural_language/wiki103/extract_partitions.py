
import os

MIN_SENTENCE_LENGTH = 20

partition_ratios = {
    'bpe': 0.1,
    'validation': 0.1,
    'test': 0.1,
    'train': 0.7
}

output_base_name = 'wiki103'

total_token_count_goal = 55750397 # Number of code tokens
total_token_count = 0

partition_token_count_goals = dict()
for partition_name, ratio in partition_ratios.items():
    partition_token_count_goals[partition_name] = int(total_token_count_goal * ratio)

partition_names = list(partition_ratios.keys())
partition_name_index = 0
current_partition_name = partition_names[partition_name_index]
current_token_counts = {name: 0 for name in partition_names}

output_file = open('partitions/{}_{}.txt'.format(output_base_name, current_partition_name), 'w+')
for file_name in os.listdir('raw/'):
    with open('raw/' + file_name, 'r') as text_file:
        print("Checking file: {}".format(file_name))

        line = 1
        while line:
            try:
                line = text_file.readline()
                tokenized_line = line.replace('\n', '').replace('\r', '').split(' ')[1:]
                if len(tokenized_line) > MIN_SENTENCE_LENGTH and tokenized_line[0] != '=':
                    total_token_count += len(tokenized_line)
                    if current_token_counts[current_partition_name] < partition_token_count_goals[current_partition_name]:
                        current_token_counts[current_partition_name] += len(tokenized_line)
                    else:
                        print("Finished assigning documents to {}".format(current_partition_name))
                        partition_name_index += 1
                        if partition_name_index < len(partition_names):
                            current_partition_name = partition_names[partition_name_index]
                        else:
                            break
                        current_token_counts[current_partition_name] = len(tokenized_line)
                        output_file.close()
                        output_file = open(
                            'partitions/{}_{}.txt'.format(output_base_name, current_partition_name), 'w+')

                    output_file.write(' '.join(tokenized_line) + '\n')
            except:
                pass
            
output_file.close()
print()
print("============= Validate Ratios =============")
for partition_name, token_count in current_token_counts.items():
    print("{}: {} / {} => {}".format(partition_name, token_count, total_token_count_goal, token_count / total_token_count))
print("===========================================")


print(total_token_count)
