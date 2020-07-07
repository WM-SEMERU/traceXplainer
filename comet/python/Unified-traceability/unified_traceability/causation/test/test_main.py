
# coding: utf-8

# In[1]:


'''
Version 1.0
@danaderp July-2018
Testing File for Causation
'''
#import sys
#import os
#this_dir = os.path.dirname(os.path.realpath('__file__'))
#sys.path.append(os.path.dirname(this_dir))
#print(this_dir)

#imporing modules
from association import PosteriorTraceability,StochasticComponent,PriorChoice,AssociationPrior,SamplingParameters, ProbabilisticModel
from intervention import CausalGraph,CausalFrame

#from causation.association import PosteriorTraceability

#importing general libraries
import pandas as pd
import random as rd
import numpy as np
import matplotlib.pyplot as plt
from networkx import nx
from scipy.stats import uniform
from pprint import pprint
from causality.analysis.dataframe import CausalDataFrame


# In[ ]:


print(StochasticComponent.LINKAGE)
confirmed_links = np.r_[np.ones(5), np.zeros(5)]
print(confirmed_links)


# In[2]:


#Testing Association Traceability

def test1_from_empirical_nuts(ir_asm_prior,confirmed_links):
    associationModel_1 = PosteriorTraceability(ir_asm_prior=ir_asm_prior)
    #Testing with 5000 samples and Sampling
    res = associationModel_1.association_model_1(inference_key='a',num_samples=5000,
                                                confirmed_links=confirmed_links) 
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    print(res.get_summary(2500))
    res.get_trace_plot(2500)
    pass

def test1_from_empirical_vi(ir_asm_prior,confirmed_links):
    associationModel_1 = PosteriorTraceability(ir_asm_prior=ir_asm_prior,progressbar=True)
    #Testing with 5000 samples and Sampling
    
    #VI is applied
    res = associationModel_1.association_model_1(inference_key='b',num_samples=5000,
                                                confirmed_links=confirmed_links) 
    print('VI: ' + str(res.get_expected_linkage_value()))
    
    #Persistance Test
    file = 'persistent_model_vi.pkl'
    #Save model
    res.save_a_trace(file = file)
    
    #Load model
    load_model = ProbabilisticModel()
    load_model.load_a_trace(file=file)
    print('LOAD VI: ' + str(load_model.get_expected_linkage_value()))
    
    print(load_model.get_summary(2500))
    load_model.get_trace_plot(2500)
    
    tracker = load_model.get_vi_tracker()
    hist = load_model.get_vi_hist()
    #plt.plot(associationModel_1.get_vi_hist())
    
    print(tracker)
    
    fig = plt.figure(figsize=(16, 9))
    mu_ax = fig.add_subplot(221)
    std_ax = fig.add_subplot(222)
    hist_ax = fig.add_subplot(212)
    mu_ax.plot(tracker['mean'])
    mu_ax.set_title('Mean track')
    std_ax.plot(tracker['std'])
    std_ax.set_title('Std track')
    hist_ax.plot(hist)
    hist_ax.set_title('Negative ELBO track') 
    
    pass

def test2_from_domain_knowledge(ir_asm_prior,confirmed_links):
    associationModel_2 = PosteriorTraceability(ir_asm_prior=ir_asm_prior)
    
    res = associationModel_2.association_model_2(
        inference_key='a',num_samples=5000,confirmed_links=confirmed_links) 
    
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    #print(res.get_summary(2500))
    #res.get_trace_plot(2500)
    sampling = res.get_dataframe()
    return sampling

def test2_predict_from_d_k(ir_asm_prior,confirmed_links):
    #TODO the ppc proces is still questionable
    associationModel_2 = PosteriorTraceability(ir_asm_prior=ir_asm_prior)
    
    res = associationModel_2.association_model_2(
        inference_key='a',num_samples=5000,confirmed_links=confirmed_links) 
    
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    print(res.get_summary(2500))
    #associationModel_2.get_trace_plot(2500,associationModel_2.get_traces_mcmc())
    ppc = associationModel_2.predict_links_by_confidence(
            trace = associationModel_2.get_traces_mcmc(),
            samples = 2500,
            confidence = 0.85
        )
    print('PPC' + str(ppc))

def test3_from_execution_traces(ir_asm_prior,confirmed_links):
    #Testing Mixture Model
    #execution_traces = [(2,2),(2,5)] This is for alfa beta
    execution_traces = [(0.5,0.02),(0.62,0.01)]
    associationModel_3 = PosteriorTraceability(ir_asm_prior=ir_asm_prior,progressbar=True)
    
    res = associationModel_3.association_model_3(
        inference_key='a',execution_traces=execution_traces,
        num_samples=5000, transitive_belief=0.4,confirmed_links=confirmed_links)
    
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    print(res.get_summary(2500))
    res.get_trace_plot(2500)
    
def test4_holistic(ir_asm_prior,confirmed_links):
    execution_traces = [(0.5,0.02),(0.62,0.01)]
    
    associationModel_4 = PosteriorTraceability(progressbar=True,ir_asm_prior=ir_asm_prior)
    
    res = associationModel_4.association_model_holistic(inference_key='a',execution_traces=execution_traces,
                                        num_samples=5000,execution_belief=0.4,confirmed_links=confirmed_links)
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    print(res.get_summary(2500))
    res.get_trace_plot(2500)
    
def test4_holistic_persistent(ir_asm_prior,confirmed_links):
    execution_traces = [(0.5,0.02),(0.62,0.01)]
    file = 'persistent_model.pkl'
    
    #Posterior Class Constructor
    associationModel_4 = PosteriorTraceability(progressbar=True,ir_asm_prior=ir_asm_prior)
    
    #Model Generation
    res = associationModel_4.association_model_holistic(inference_key='a',execution_traces=execution_traces,
                                        num_samples=5000,execution_belief=0.4,confirmed_links=confirmed_links)
    print('NUTS: ' + str(res.get_expected_linkage_value()))
    #print('Gelman-Rubin' + str(res.get_gelman_rubin(2500)))
    #print(res.get_summary(2500))
    #res.get_trace_plot(2500)
    
    #Save model
    res.save_a_trace(file = file)
    
    #Load model
    load_model = ProbabilisticModel()
    load_model.load_a_trace(file=file)
    print('LOAD NUTS: ' + str(load_model.get_expected_linkage_value()))
    
def test4_holistic_map(ir_asm_prior,confirmed_links):
    execution_traces = [(0.5,0.02),(0.62,0.01)]
    
    associationModel_4 = PosteriorTraceability(progressbar=True,ir_asm_prior=ir_asm_prior)
    
    res = associationModel_4.association_model_holistic(inference_key='c',execution_traces=execution_traces,
                                        num_samples=5000,execution_belief=0.4,confirmed_links=confirmed_links)
    
    print('MAP: ' + str(res.get_map_linkage_value()))
    print('Summary-MAP: ' + str(res.get_map()))
    

if __name__ == '__main__':
    #Random Generation of Empirical Evaluations
    N = 20 #Number of Experiments (this is the different empircal evaluations)
    mu, sigma = 0, 0.1 # mean and standard deviation
    domain_conf = 0.2 #Domain-Knowledge of Links
    empirical_sim = uniform.rvs(size = N)
    
    #n = 20 #Number of observed trials
    #links = 13 #Number of successfull links
    confirmed_links = np.r_[np.ones(5), np.zeros(5)] #This is for Bernulli
    
    d_penalty = 0.2
    
    #print(empirical_sim)
    
    ir_asm_prior1 = AssociationPrior(
        prior_linkage = PriorChoice.SPECIFIC_INFORMATIVE,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.SPECIFIC_INFORMATIVE,
        domain_conf = domain_conf,
        d_penalty = d_penalty
    )
    
    ir_asm_prior2 = AssociationPrior(
        prior_linkage = PriorChoice.SPECIFIC_INFORMATIVE,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL,
        domain_conf = domain_conf,
        d_penalty = d_penalty
    )
    
    weak_prior1 = AssociationPrior(
        prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL,
        prior_domain = PriorChoice.SPECIFIC_INFORMATIVE,
        domain_conf = domain_conf,
        d_penalty = d_penalty
    )
    
    weak_prior2 = AssociationPrior(
        prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL,
        prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL,
        d_penalty = d_penalty,
        domain_conf = domain_conf,
    )
    
    #Testing Association Models
    
    #test1_from_empirical_nuts(ir_asm_prior1,confirmed_links)
    #test1_from_empirical_nuts(ir_asm_prior2,confirmed_links)
    #test1_from_empirical_nuts(weak_prior1,confirmed_links)
    #test1_from_empirical_nuts(weak_prior2,confirmed_links)
    
    #test1_from_empirical_vi(ir_asm_prior1,confirmed_links) #<-- Converging
    #test1_from_empirical_vi(ir_asm_prior2,confirmed_links)
    #test1_from_empirical_vi(weak_prior1,confirmed_links)
    test1_from_empirical_vi(weak_prior2,confirmed_links) #<-- Testing Persistance
    
    #test2_from_domain_knowledge(ir_asm_prior1,confirmed_links)
    #test2_from_domain_knowledge(ir_asm_prior2,confirmed_links)
    #test2_from_domain_knowledge(weak_prior1,confirmed_links)
    #test2_from_domain_knowledge(weak_prior2,confirmed_links)
    
    #test2_predict_from_d_k(ir_asm_prior1,confirmed_links) #<-- Prediction
    
    #test3_from_execution_traces(ir_asm_prior1,confirmed_links)
    #test3_from_execution_traces(ir_asm_prior2,confirmed_links)
    #test3_from_execution_traces(weak_prior1,confirmed_links)
    #test3_from_execution_traces(weak_prior2,confirmed_links)
    
    #test4_holistic(ir_asm_prior1,confirmed_links)
    #test4_holistic(ir_asm_prior2,confirmed_links)
    #test4_holistic(weak_prior1,confirmed_links)
    #test4_holistic(weak_prior2,confirmed_links)
    
    #test4_holistic_persistent(ir_asm_prior1,confirmed_links) #<-- Persistent Model 
    
    #test4_holistic_map(ir_asm_prior1,confirmed_links)
    #test4_holistic_map(ir_asm_prior2,confirmed_links)
    #test4_holistic_map(weak_prior1,confirmed_links)
    #test4_holistic_map(weak_prior2,confirmed_links)


# In[ ]:


#Testing Causal Graph Inference with PosteriorTraceability
def generate_data_from_associations(num_variables = 5, samples=5000):
        #Random Generation of Empirical Evaluations
        N = 20 #Number of Experiments (this is the different empircal evaluations)
        mu, sigma = 0, 0.1 # mean and standard deviation
        domain_injection = 0.5 #Domain-Knowledge of Datasets
        n = 20 #Number of observed trials
        data_links_dict = {}

        for x in range(0, num_variables):
            empirical_sim = uniform.rvs(size = N) #Simulated Uniform Similarities 
            links = rd.randint(0, n) #Number of successfull links
            associationModel = PosteriorTraceability(empirical_sim,n,links)
            res = associationModel.association_model_1(samples) #Testing with 5000 samples
            linkage_traces = associationModel.get_traces_mcmc() #Traces from Samplings MCMC
            #print(linkage_traces['linkage'])
            data_links_dict['x' + str(x)] =  linkage_traces['linkage']
        print(data_links_dict) 
        return data_links_dict

def generate_data_from_distributions(num_variables = 5, samples=5000):
        data_links_dict = {}
        for x in range(0, num_variables):
            empirical_sim = halfnorm.rvs(size = samples)
            data_links_dict['x' + str(x)] =  empirical_sim

        print(data_links_dict) 
        return data_links_dict
    
def test1_causal_effect():
    data_links_dict = generate_data_from_associations(2)
    #data_links_dict = generate_data_from_distributions(num_variables=5,samples=2000)
    #Testing Causal Effect
    data_links = pd.DataFrame(data_links_dict) #Converting the traces into dataframes
    variable_types = dict.fromkeys(data_links, 'c') #Assigning continuous variables
    causalGraph = CausalGraph(data_links,variable_types)
    graph = causalGraph.get_causal_graph()
    pass

def test2_causal_effect_w_intervention():
    #data_links_dict = generate_data_from_associations(5)
    data_links_dict = generate_data_from_distributions(num_variables=5,samples=2000)
    #Testing Causal Effect
    data_links = pd.DataFrame(data_links_dict) #Converting the traces into dataframes
    variable_types = dict.fromkeys(data_links, 'c') #Assigning continuous variables
    causalGraph = CausalGraph(data_links,variable_types)
    graph = causalGraph.get_causal_graph()
    
    #Testing an intervention 
    listNodes = ['x0','x2','x3','x4', 'x1']
    listEdges = [('x0','x1'),('x1','x2'),('x1','x3'),('x2','x3')]
    causalGraph.build_directed_causal_graph(listNodes,listEdges)
    x = pd.DataFrame({'x0' : [0.], 'x1' : [0.]})
    effect = causalGraph.do_intervention_non_parametric(['x0'],['x1'],x)
    print('Effect = '+str(effect))
    pass

def test3_causal_data_frame_discrete():
    #Testing Causal Data Frames
    #First Case with Discrete Treatment
    N = 1000
    z = np.random.normal(1., size=N)
    x = np.random.binomial(1, p=1./(1. + np.exp(-z/.1)))
    y = x + z + np.random.normal(size=N)
    causalFrame = CausalFrame(treatment=x,outcome=y,confounders=(z,))
    #print(causalFrame.get_causal_frame())
    df = causalFrame.do_expected_value_non_parametric(confounders=['z0'],types={'z0': 'c'})
    print(df)
    causalFrame.print_expectation_categorial(confounders=['z0'],types={'z0': 'c'})
    pass

def test_intervention_confidence_link():
    #Random Generation of Empirical Evaluations
    N = 20 #Number of Experiments (this is the different empircal evaluations)
    mu, sigma = 0, 0.1 # mean and standard deviation
    domain_conf = 0.2 #Domain-Knowledge of Links
    empirical_sim = uniform.rvs(size = N)
    
    n = 20 #Number of observed trials
    links = 13 #Number of successfull links
    confirmed_links = np.r_[np.ones(5), np.zeros(5)] #This is for Bernulli
    
    d_penalty = 0.2
    
    ir_asm_prior1 = AssociationPrior(
        prior_linkage = PriorChoice.SPECIFIC_INFORMATIVE,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.SPECIFIC_INFORMATIVE,
        domain_conf = domain_conf,
        d_penalty = d_penalty
    )
    
    #Calling the Sampling
    res_sampling = test2_from_domain_knowledge(ir_asm_prior1,n,links,confirmed_links)
    causalFrame = CausalFrame(ltr=res_sampling)
    
    #causalFrame.do_expected_value_non_parametric(
    #    confounders = 
    #)
    df = causalFrame.get_causal_frame()
    key_list = list(df.keys())
    conterfactual_keys = [e for e in key_list if e not in {'linkage', 'confidence'}]
    variable_types = dict.fromkeys(conterfactual_keys, 'c')
    
    print(conterfactual_keys)
    
    causalFrame.do_expected_value_non_parametric(
        confounders=conterfactual_keys, types=variable_types, sampling=500,
        confidence=0.95, treatment='confidence', outcome='linkage')
    
    print(variable_types)
    
    #print(causalFrame.get_causal_frame())
    
    
if __name__ == '__main__':
    #test1_causal_effect()
    #test2_causal_effect_w_intervention()
    #test3_causal_data_frame_discrete()
    test_intervention_confidence_link()


# In[ ]:


#Testing/Building Execution Traces
import pymc3 as pm 
from scipy.stats import beta, halfnorm, uniform
import ipdb


# In[ ]:


#Fitting Beta
N = 20
empirical_sim = uniform.rvs(size = N)
print(empirical_sim)
prior_parameters = beta.fit(empirical_sim, floc=0, fscale=1) #extract a,b from fit
prior_a, prior_b = prior_parameters[0:2]


# In[ ]:


print(prior_a)
print(prior_b)
#Mean and Dispersion of Linkage
mean,var,skew,kurt = beta.stats(prior_a,prior_b,moments='mvsk')
print(mean)
print(var)
execution_belief = 0.2 #Values defined between (0,1)


# In[ ]:


#execution_traces = [(2,2),(2,5)]
execution_traces = [(0.5,0.02),(0.62,0.01)]
print(execution_traces)
ncomp = len(execution_traces)
print(np.ones(ncomp))
norm_w = np.array([0.75, 0.25])
print(np.ones_like(norm_w))


# In[ ]:



if __name__ == '__main__':
    with pm.Model() as model:
        
        #Mixture Params
        w = pm.Dirichlet('w',np.ones(ncomp))
        #theta_1 = pm.Beta.dist(alpha = 0.5, beta = 0.01)
        #theta_2 = pm.Beta.dist(alpha = 0.7, beta = 0.01)
        comp_dist = [pm.Beta.dist(mu = execution_traces[i][0], sd = execution_traces[i][1])
                        for i in range(ncomp)]
        
        mix_beta = pm.Mixture('mix_beta',w=w,comp_dists = comp_dist)
        
        #Theta Linkage Prior
        mu = execution_belief*mix_beta + (1-execution_belief)*mean #Deterministic?
        nu = var
        mu_new = pm.Normal("mu_new", mu = mu , sd = 0.01)
        
        alpha = pm.Deterministic('alpha', mu_new/(nu*nu))
        beta = pm.Deterministic('beta', (1. - mu_new)/(nu*nu))      
        
        theta_prior = pm.Beta('linkage',alpha, beta)
        
        obs = pm.Binomial('obs', n=20, p=theta_prior, observed=13)
        
        #Inference
        #start = pm.find_MAP() #Find good starting values for the sampling algorithm
        #step = pm.NUTS(max_treedepth=15) #Sampling with MCMC
        #trace = pm.sample(5000, step, target_accept = 0.90, tune= 1000, progressbar=True)
        #ipdb.set_trace() # debugging starts here
         
        approx = pm.fit(
            #n=10000, 
            #method='advi', 
            #model=modelE,
            #obj_optimizer=pm.adagrad_window(learning_rate=2e-4),
            #total_grad_norm_constraint=10
        ) #Variational?
        tracevar = approx.sample(5000) #Sampling with Variational Inference"""


# In[ ]:


for RV in model.basic_RVs:
    print(RV.name, RV.logp(model.test_point))


# In[ ]:


#Deterministic assessment
print(alpha.tag.test_value)
print(beta.tag.test_value)
#Starting Value of the Approximation is valid
print(approx.groups[0].bij.rmap(approx.params[0].eval()))


# In[ ]:


pm.traceplot(trace[2500:])


# In[ ]:


tr = trace[2500:]
pm.summary(tr, varnames=['linkage','mu_new','mix_beta'])


# In[ ]:


pm.gelman_rubin(tr)


# In[ ]:


pprint(start)


# In[ ]:


#Doing the same think but with one betta distriution
if __name__ == '__main__':
    with pm.Model() as modelE:
        
        #Execution Traces
        mix_beta = pm.Beta('mix_beta',mu = 0.1, sd = 0.05)
        
        #Theta Linkage Prior
        mu = execution_belief*(mix_beta) + (1-execution_belief)*mean #Deterministic?
        nu = var
        mu_new = pm.Normal("mu_new", mu = mu , sd = 0.01)
        
        alpha = pm.Deterministic('alpha', mu_new/(nu*nu))
        beta = pm.Deterministic('beta', (1. - mu_new)/(nu*nu))      
        
        theta_prior = pm.Beta('linkage',alpha, beta)
        
        obs = pm.Binomial('obs', n=20, p=theta_prior, observed=13)
        
        #Inference
        #start = pm.find_MAP() #Find good starting values for the sampling algorithm
        #step = pm.NUTS(max_treedepth=15) #Sampling with MCMC
        #trace = pm.sample(5000, step, target_accept = 0.99, tune= 1000, progressbar=True)
        approx = pm.fit(n=10000, method='advi', model=modelE,
            obj_optimizer=pm.adagrad_window(learning_rate=2e-4),total_grad_norm_constraint=10) #Variational?
        tracevar = approx.sample(5000) #Sampling with Variational Inference


# In[ ]:


for RV in modelE.basic_RVs:
    print(RV.name, RV.logp(modelE.test_point))


# In[ ]:


#Deterministic assessment
print(alpha.tag.test_value)
print(beta.tag.test_value)
#Starting Value of the Approximation is valid
print(approx.groups[0].bij.rmap(approx.params[0].eval()))


# In[ ]:


#pm.traceplot(trace[2500:])
pm.traceplot(tracevar[2500:])


# In[ ]:


#tr = trace[2500:]
tr = tracevar[2500:]
pm.summary(tr, varnames=['linkage','mu_new','mix_beta'])


# In[ ]:


pm.gelman_rubin(tr)


# In[ ]:


print(tr)

