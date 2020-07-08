'''
Daniel McCrystal
June 2018

'''

from math import fabs
import random

class DistributionApproximator:

    def __init__(self, data):

        self.data = data
        self.n = len(data)

        self.phi = 2 / sum([(data[i+1] - data[i]) * (self._density(i+1, 1) + self._density(i, 1)) for i in range(self.n - 1)])

        print("Phi found! phi=" + str(self.phi))

    def _density(self, i, phi):
        #print("Calculating density at index " + str(i) + " with phi: " + str(phi))
        sum = 0
        for k in range(self.n):
            if k != i:
                sum += phi / (fabs(self.data[k] - self.data[i]) ** 0.25)

        #print("Density: " + str(sum))
        return sum

    def density(self, i):
        return self._density(i, self.phi)

    def densities(self):
        return [self.density(i) for i in range(self.n)]
