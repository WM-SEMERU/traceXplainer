
# coding: utf-8

# In[12]:


'''
@danaderp June-2018
Inferring Causal Traces from an Unified IR perspective
'''
import numpy as np
import pandas as pd

from causality.inference.search import IC
from causality.inference.independence_tests import RobustRegressionTest


# In[2]:


#importing IR-Modules
import sys
sys.path.insert(0, '../ir')
from Corpus import Corpus
from VSM import VSM
from LSI import LSI
from JensenShannon import JensenShannon


# In[3]:


#Uploading the Corpora
dataset = 'cisco_project'
corpus_root = '../../../../datasets/'+ dataset +'_semeru_format/'
source_path = 'requirements'
target_path = 'source_code'
truth_path = 'ground.txt'
whitelist = ['java', 'txt', 'jsp', 'h', 'c']
corpus = Corpus("Libest", corpus_root, source_path, target_path, truth_path, filetype_whitelist=whitelist)


# In[6]:


#Generating Similarities for Vector Space Model
import os

vsm = VSM(corpus)
vsm_model = vsm.compute_similarities()
vsm_model_smooth = vsm.compute_similarities(parameters={'smooth':True})


# In[16]:


sources = corpus.get_source_names()
targets = corpus.get_target_names()

y_true = np.array([corpus.get_truth()[source][target] for source in sources for target in targets])
y_scores = np.array([vsm_model_smooth.get_value(source, target) for source in sources for target in targets])


# In[2]:


#UpLoad Similarities R->C
linesR2C_Jensen = open('similarities/R-C/js.txt', encoding='utf-8').read().split('\n')
linesR2C_VSM = open('similarities/R-C/vsm.txt', encoding='utf-8').read().split('\n')
linesR2C_Smooth_VSM = open('similarities/R-C/svms.txt', encoding='utf-8').read().split('\n')



# In[3]:


#UpLoad Similarities R->T
linesR2T_Jensen = open('similarities/R-T/outjensen.txt', encoding='utf-8').read().split('\n')
linesR2T_VSM = open('similarities/R-T/outvsm.txt', encoding='utf-8').read().split('\n')
linesR2T_Smooth_VSM = open('similarities/R-T/outsvsm.txt', encoding='utf-8').read().split('\n')


# In[4]:


#Upload Ground Truth From Execution Traces
execution_trace = open('similarities/T-C/t.txt', encoding='utf-8').read().split('\n')


# In[5]:


#Mergining acccording to IR type
lines_Jensen = linesR2C_Jensen + linesR2T_Jensen
lines_VSM = linesR2C_VSM + linesR2T_VSM
lines_Smooth_VSM = linesR2T_Smooth_VSM + linesR2C_Smooth_VSM


# In[6]:


#Activation function is the sigmoid function
import scipy.special as sc
from collections import defaultdict

activation_function = lambda x: sc.expit(x)

R2C_jensen_dict = {}
R2C_vms_dict = {}
R2C_s_vsm_dict = {}
R2C_list_dict = [R2C_jensen_dict,R2C_vms_dict,R2C_s_vsm_dict]

for line in lines_Jensen:
    t = line.split(' ')
    if(len(t) == 3):
        R2C_jensen_dict[t[0]+'|'+t[1]] = t[-1] 
        
for line in lines_VSM:
    t = line.split(' ')
    if(len(t) == 3):
        R2C_vms_dict[t[0]+'|'+t[1]] = t[-1] 
        
for line in lines_Smooth_VSM:
    t = line.split(' ')
    if(len(t) == 3):
        R2C_s_vsm_dict[t[0]+'|'+t[1]] = t[-1]         


# In[28]:


pprint(R2C_vms_dict)


# In[7]:


from functools import reduce
from pprint import pprint
from collections import deque

summed_input =  reduce(
    lambda a,b: {k: float(a[k]) + float(b[k]) for k in a}, 
    R2C_list_dict,
    dict.fromkeys(R2C_list_dict[0], 0.0)
)

R2C_similarities =  dict(map(
    lambda b: (b[0], [float(i) for i in b[1]]),
    list(map(
        lambda a: (
            a[0].split('|')[0]+'~'+a[0].split('|')[1],
            (a[1].split('|')[1],a[1].split('|')[2],a[1].split('|')[3])
        ),
        reduce(
            lambda a,b: {k: a[k]+'|'+b[k] for k in a}, 
            R2C_list_dict,
            dict.fromkeys(R2C_list_dict[0], '0.0')
        ).items()
    ))
))

R2C_activation = {k: sc.expit(v) for k,v in summed_input.items()} #Transforming into Binomial Activation

mapped_activiation = list(map(
    lambda a : ( a[0].split('|')[0], (a[0].split('|')[1],a[1]) ),
    R2C_activation.items()
))

#Computing Probabilities from activation values
bernulli_u = dict(map(lambda a: (a[0]+'~'+a[1][0],a[1][1]),mapped_activiation))

pprint(bernulli_u)


# In[8]:


print(R2C_similarities)


# In[9]:


#Computing Likehood by Pearson Similarity

#Apriori: Scanning according to a Threshold Support
Min_Supp = 0.6
bernulli_u_apriori = {k: v for k, v in bernulli_u.items() if v >= Min_Supp}

import itertools
keys = list(itertools.permutations( list(bernulli_u_apriori.keys()), 2)) #
#print(keys)


# In[10]:


print(keys)


# In[11]:


#Computing Correlations
from scipy.stats.stats import pearsonr   

likelihood = dict(map(
    lambda k : ((k[0]+'|'+k[1]),(
        pearsonr(R2C_similarities[k[0]],R2C_similarities[k[1]])
                                )),
    keys
))

#pprint(likelihood)


# In[12]:


#Computing Posterior Probabilities
#Min_corr = 0.0
Min_pvalue = 0.05
posterior_bernulli = {k.split('|')[1]+'|'+k.split('|')[0]: 
                      ( sc.expit(v[0])* bernulli_u_apriori[k.split('|')[0]]/bernulli_u_apriori[k.split('|')[1]])
                      for k, v in likelihood.items() if  v[1] <= Min_pvalue }

pprint(posterior_bernulli)


# In[13]:


#Maximum Posterior Detected

def insertIntoDataStruct(key,value,aDict):
    if not key in aDict:
        aDict[key] = [value]
    else:
        aDict[key].append(value)

posterior_bernulli_depence = {}

for key, value in posterior_bernulli.items():
    insertIntoDataStruct(key.split('|')[0],value,posterior_bernulli_depence)

posterior_bernulli_depence = {k: sc.expit(sum(v)) for k, v in posterior_bernulli_depence.items()}


# In[14]:


pprint(posterior_bernulli_depence)


# In[15]:


def returnDependencyProb(key, aDict):
    print(key)
    if not key in aDict:
        return bernulli_u[key]
    else:
        return aDict[key]

def modifyValue(key, aDict, value):
    if not key in aDict:
        if not key in posterior_bernulli_depence:
            return (0.0,value)
        else:
            return (0.0,posterior_bernulli_depence[key])
    else:
        return aDict[key]
#Upload Ground Truth 
ground_trace = open('similarities/ground.txt', encoding='utf-8').read().split('\n')
#pprint(execution_trace)

ground = {}

for line in ground_trace:
    artifact = line.split(' ')
    key = artifact[0]
    artifact.pop(0)
    for t in artifact:
        key_in = key+'~'+t
        ground[key_in] = (1.0, returnDependencyProb(key_in,posterior_bernulli_depence) )

for k,v in bernulli_u.items():
    ground[k] = modifyValue(k,ground,v)
    
pprint(ground)        


# In[16]:


# Support in a ROC curve
from sklearn.metrics import roc_curve, auc
import matplotlib.pyplot as plt
import random
import pandas as pd


# In[17]:


x = pd.DataFrame(list(ground.values()))
actual = list(x[0])
predictions = list(x[1])
print(x)


# In[18]:


false_positive_rate, true_positive_rate, thresholds = roc_curve(actual, predictions)
roc_auc = auc(false_positive_rate, true_positive_rate)


# In[19]:


print(false_positive_rate)
print(true_positive_rate)
print(thresholds)


# In[20]:


get_ipython().run_line_magic('matplotlib', 'inline')

plt.title('Receiver Operating Characteristic')
plt.plot(false_positive_rate, true_positive_rate, 'b',
label='AUC = %0.2f'% roc_auc)
plt.legend(loc='lower right')
plt.plot([0,1],[0,1],'r--')
plt.xlim([-0.1,1.2])
plt.ylim([-0.1,1.2])
plt.ylabel('True Positive Rate')
plt.xlabel('False Positive Rate')
plt.show()


# In[21]:


#Precision Recall Curve
from sklearn.metrics import precision_recall_curve,average_precision_score
precision, recall, thresholds = precision_recall_curve(actual, predictions)
average_precision = average_precision_score(actual, predictions)


# In[27]:


plt.title('Precision-Recall Curve: AP={0:0.2f}'.format(average_precision))
plt.step(recall, precision, color='b', alpha=0.2, where='post')
#plt.fill_between(recall, precision, step='post', alpha=0.2, color='b')
#plt.plot(recall, precision, 'b',label='AP = %0.2f'% average_precision)
#plt.legend(loc='lower right')
#plt.plot([0,1],[0,1],'r--')
plt.ylim([0.0, 1.05])
plt.xlim([0.0, 1.0])
plt.ylabel('Precision')
plt.xlabel('Recall')
plt.show()


# In[ ]:


#Learning Causal Graph
N = 3 #trials
binomial_probabilities = {k.split('|')[0]+'~'+k.split('|')[1]: v for k,v in R2C_activation.items()}
binomial_distributions = {k: numpy.random.binomial(n=1, p=v,size=N) for k,v in binomial_probabilities.items()}


keys_final_var =  list(binomial_corr_a.keys())
reduced_keys_final = set()
for k in keys_final_var:
    reduced_keys_final.add(k[0])
    reduced_keys_final.add(k[1])
reduced_keys_final = list(reduced_keys_final)
    
binomial_causal = {k: binomial_distributions[k] for k in reduced_keys_final}

pprint(binomial_causal)


# In[113]:


#Link as Binonial Distribution

pprint(len(binomial_supp)) #Original Sets
pprint(len(binomial_supp_1)) #Sets after the first scan 

import itertools
keys = list(itertools.combinations( list(binomial_supp_1.keys()), 2)) #

binomial_supp_a = dict(map(lambda k: (k ,(binomial_supp_1[k[0]]*binomial_supp_1[k[1]]
                                         )),  keys)) #Computing a = P(x=1, y=1)
#print(len(binomial_supp_a)) #Sets With Combined Support (assuming independence)
#binomial_supp_a = {k: v for k, v in binomial_supp_a.items() if v >= Min_Supp}
#print(len(binomial_supp_a)) #Sets after the second scan
#pprint(binomial_supp_a)


# In[9]:


#Computing Complete Joint Distrubution
binomial_supp_b = {k: (v,binomial_supp_1[k[0]] * (1-binomial_supp_1[k[1]])) for k, v in binomial_supp_a.items()} #Computing b=P(x=1, y=0)
binomial_supp_c = {k: (v[0],v[1],(1-binomial_supp_1[k[0]]) * binomial_supp_1[k[1]]) for k, v in binomial_supp_b.items()} #Computing c=P(x=0, y=1)
binomial_supp_d = {k: (v[0],v[1], v[2],(1-binomial_supp_1[k[0]]) * (1-binomial_supp_1[k[1]])) for k, v in binomial_supp_c.items()} #Computing d=P(x=0, y=0)

#Computing Correlations
from scipy.stats.stats import pearsonr   

binomial_corr = {k: pearsonr(
    R2C_similarities[k[0]],R2C_similarities[k[1]]
) for k, v in binomial_supp_d.items()} 

#print(len(binomial_corr))
#pprint(binomial_corr)

#Filtering by correlation value and pvalue
Min_corr = 0.6
Min_pvalue = 0.05
binomial_corr_a = {k:v for k,v in binomial_corr.items() if v[0]>= Min_corr and v[1]<=Min_pvalue}

print(len(binomial_corr_a))
pprint(binomial_corr_a)


# In[112]:


#Computing Binomial Trials
N = 10 #trials
binomial_probabilities = {k.split('|')[0]+'~'+k.split('|')[1]: v for k,v in R2C_activation.items()}
binomial_distributions = {k: numpy.random.binomial(n=1, p=v,size=N) for k,v in binomial_probabilities.items()}


keys_final_var =  list(binomial_corr_a.keys())
reduced_keys_final = set()
for k in keys_final_var:
    reduced_keys_final.add(k[0])
    reduced_keys_final.add(k[1])
reduced_keys_final = list(reduced_keys_final)
    
binomial_causal = {k: binomial_distributions[k] for k in reduced_keys_final}

pprint(binomial_causal)


# In[16]:


# load the data into a dataframe:
B = pd.DataFrame(binomial_causal)
variable_types_binomial = dict.fromkeys(binomial_causal, 'c')

#Computing Causal Effect
from causality.estimation.adjustments import AdjustForDirectCauses
from networkx import DiGraph
from causality.estimation.nonparametric import CausalEffect

g = DiGraph()

g.add_nodes_from(reduced_keys_final)
g.add_edges_from(keys_final_var)
adjustment = AdjustForDirectCauses()

print(adjustment.admissable_set(g,['RQ17~est_client.c'], ['RQ34~est_server.c']))


# In[ ]:


#P(x3|do(x2))
admissable_set = adjustment.admissable_set(g,['RQ13~est_client.c'], ['RQ55~us895.c'])
effect = CausalEffect(B, ['RQ13~est_client.c'], ['RQ55~us895.c'], variable_types=variable_types_binomial, admissable_set=list(admissable_set))
x = pd.DataFrame({'RQ13~est_client.c' : [1.], 'RQ55~us895.c' : [1.]})
effect.pdf(x)


# In[18]:


#Computing causal Effect of direct causes
direct_causal_effect = {}
for k in keys_final_var:
    admissable_set = adjustment.admissable_set(g,[k[0]], [k[1]])
    print(k[0]+k[1])
    print(admissable_set)
    effect = CausalEffect(B, [k[0]], [k[1]], variable_types=variable_types_binomial, admissable_set=list(admissable_set))
    effect =  effect.pdf(pd.DataFrame({k[0] : [1.], k[1] : [1.]}))
    print(effect)
    direct_causal_effect[k] = effect


# In[87]:


#Computing Causal Graph
# run the search
ic_algorithm = IC(RobustRegressionTest)
graphBinomial = ic_algorithm.search(B, variable_types_binomial)
graphBinomial.edges(data=True)


# In[10]:


reduced_activation = defaultdict(list)
for key, val in mapped_activiation:
    reduced_activation[key].append(val)

#Computing Probabilities from activation values
probabilities_act = dict(map(
    lambda x: (x[0] , 
               list(map(
               lambda y: (y[0],float(y[1])*(1/len(x[1]))),
               x[1]    
               ))
              ),
    reduced_activation.items()
))

#Computing multinomial from Probability Tables
N=10 #Number of Randomized experiments
multinomial_act = dict(map(
    lambda z: (z[0],numpy.random.choice(len(z[1]),N,z[1]).tolist()),
    dict(map(
        lambda y: (y[0], [1-sum(y[1])]+y[1] ),
        dict(map(
            lambda x: (x[0],list(dict(x[1]).values())),
            probabilities_act.items()
        )).items()
    )).items()
    ))
pprint(probabilities_act)


# In[8]:


# load the data into a dataframe:
R = pd.DataFrame(multinomial_act)
variable_types = dict.fromkeys(multinomial_act, 'c')

# run the search
ic_algorithm = IC(RobustRegressionTest)
graph = ic_algorithm.search(R, variable_types)
graph.edges(data=True)


# In[ ]:


#Association Rules
association_rules = map(
    lambda x: x[0]
    multinomial_act.items())

print()


# In[180]:


#Procesing Req-Test
R2T_jensen_dict = {}
R2T_vms_dict = {}

for line in linesR2T_Jensen:
    t = line.split(' ')
    if(len(t) == 3):
        R2T_jensen_dict[t[0]+t[1]] = t[-1] 
        
for line in linesR2T_VSM:
    t = line.split(' ')
    if(len(t) == 3):
        R2T_vms_dict[t[0]+t[1]] = t[-1] 
        
#Applying the activation function
for k,v in R2T_jensen_dict.items():
    R2T_jensen_dict[k] = activation_function(float(R2T_vms_dict[k])+float(v))

#To numpy
R2T_activation = numpy.array(list(R2T_jensen_dict.values()), dtype = 'float')    


# In[6]:


#ReShaping HardCoded
R2C_activation = R2C_activation.reshape((47,13))
R2T_activation = R2T_activation.reshape((36,21))


# In[8]:


#From Correlation to Probability Distribution
ACTIVATION_THRESHOLD = 0.6 #Suficient information to consider an observation probable
#R2C_probability = R2C_activation[(R2C_activation >= ACTIVATION_THRESHOLD)]
#R2C_probability = R2C_probability * (1/R2C_probability.size)
R2C_probability = R2C_activation * (1/R2C_activation.shape[1])
print(R2C_probability)
print(numpy.sum(R2C_probability))
print(1-numpy.sum(R2C_probability))


# In[9]:


R2T_probability = R2T_activation[(R2T_activation >= ACTIVATION_THRESHOLD)]
R2T_probability = R2T_probability * (1/R2T_probability.size)
print(numpy.sum(R2T_probability))
print(1-numpy.sum(R2T_probability))


# In[10]:


#print(R2C_probability)
req_code = []
for i in range(R2C_probability.shape[0]):
    #print(R2C_probability[i])
    x = numpy.random.choice(R2C_probability.shape[1],100,R2C_probability[i].tolist())
    req_code.append(x.tolist())
L = req_code
print(L)    


# In[11]:


#Stochastic Requirements
R0 = req_code[0]
R1 = req_code[1]
R2 = req_code[2]
R3 = req_code[3]
R4 = req_code[4]
R5 = req_code[5]

# load the data into a dataframe:
R = pd.DataFrame({'R0' : R0, 'R1' : R1, 'R2' : R2, 'R3' : R3, 'R4' : R4, 'R5' : R5})
variable_types = {'R0' : 'c', 'R1' : 'c', 'R2' : 'c', 'R3' : 'c', 'R4' : 'c', 'R5' : 'c'}


# In[12]:


# run the search
ic_algorithm = IC(RobustRegressionTest)
graph = ic_algorithm.search(R, variable_types)


# In[13]:


graph.edges(data=True)

