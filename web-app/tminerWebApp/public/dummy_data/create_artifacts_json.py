import os
import random
import json

project_key = 'libest'

artifact_classes = ['req', 'src', 'tc']

base_filepath = '{}/'.format(project_key)

artifact_infos = {}

for artifact_class in artifact_classes:
	artifact_infos[artifact_class] = {}

	for filename in os.listdir(base_filepath + artifact_class):
		if artifact_class == 'req':
			artifact_infos[artifact_class][filename] = {
				'id': filename,
				'type': artifact_class,
				'lang': 'english',
				'security_status': random.randint(0, 1)
			}
		else:
			artifact_infos[artifact_class][filename] = {
				'id': filename,
				'type': artifact_class,
				'lang': 'c',
			}

with open(base_filepath + 'artifact_infos.json', 'w') as json_file:
	json.dump(artifact_infos, json_file)