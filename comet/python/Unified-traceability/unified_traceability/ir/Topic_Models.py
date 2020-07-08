'''
Daniel McCrystal
June 2018

'''

from .IR_Method import IR_Method

from math import sqrt
from numpy.linalg import norm
from scipy.stats import entropy

class Topic_Models(IR_Method):

    def hellinger_distance(self, P, Q):
        return 1 - (1 / sqrt(2)) * \
            sqrt( sum( [(sqrt(P[i]) - sqrt(Q[i])) ** 2 \
            for i in range(len(P))] ) )

    def euclidian_distance(self, P, Q):
        return 1 / norm(P - Q)

    def inverse_euclidian(self, P, Q):
        return norm(P - Q)

    def divergence(self, P, Q):
        for i in range(len(P)):
            if P[i] == 0:
                P[i] = 0.00001
        for i in range(len(Q)):
            if Q[i] == 0:
                Q[i] = 0.00001


        return 1 / entropy(P, Q)
