'''
Daniel McCrystal
July 2018

'''

from statistics import mean, stdev
import math
from numpy import absolute, median

# Min-Max
def normalize_min_max(data):
    data_min = min(data)
    data_max = max(data)
    data_range = data_max - data_min

    normalized_values = [(x - data_min) / data_range for x in data]
    return normalized_values

def get_threshold_min_max(data, ratio=0.5):
    data_min = min(data)
    data_max = max(data)

    return (data_max + data_min) * ratio


# Sigmoid
def sigmoid(x, mean, stdev):
    return 1 / (1 + math.exp(-(x - mean) / stdev))

def inv_sigmoid(y, mean, stdev):
    return (-stdev * math.log((1/y) - 1)) + mean

def normalize_sigmoid(data):
    data_mean = mean(data)
    data_stdev = stdev(data)

    normalized_values = [sigmoid(x, data_mean, data_stdev) for x in data]
    return normalized_values

def get_threshold_sigmoid(data):
    normalized_values = normalize_sigmoid(data)
    threshold = inv_sigmoid(mean(normalized_values), mean(data), stdev(data))
    return threshold


# Stats
def median_absolute_deviation(data):
    return median(absolute(data - median(data)))
