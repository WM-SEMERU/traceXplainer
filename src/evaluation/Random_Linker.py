'''
Daniel McCrystal
July 2018

An IR method that randomly assigns similarity values to pairs of artifacts,
to see if our actual IR methods are actually doing anything meaningful.
'''

import random

from .IR_Method import IR_Method

class Random_Linker(IR_Method):

    def generate_model(self):
        model = self._new_model("Random")

        for source in self._corpus.get_source_names():
            for target in self._corpus.get_target_names():
                model.set_value(source, target, random.random())

        return model
