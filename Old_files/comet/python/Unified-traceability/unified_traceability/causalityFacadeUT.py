
# coding: utf-8

# In[1]:


"""
Version: 1.5
@danaderp July 2018
Provide a unified interface to a set of causality interfaces.
CausalityFacade is a higher-level interface that makes the causality module easier to use. 
"""

from causation.association import PosteriorTraceability,AssociationPrior,PriorChoice,VariationalParameters,SamplingParameters 
from causation.intervention import CausalGraph, CausalFrame
from causalityFacade import PriorFacade, ParametersFacade, CausalityFacadeAssociation, CausalityFacadeIntervention

from scipy.stats import halfnorm, uniform
import sys
import matplotlib.pyplot as plt
import numpy as np


# In[4]:


if __name__ == '__main__':
    #Random Generation of Empirical Evaluations
    N = 20 #Number of Experiments (this is the different empircal evaluations)
    mu, sigma = 0, 0.1 # mean and standard deviation
    
    domain_injection = 0.5 #Domain-Knowledge of Datasets
    execution_belief = 0.8 #How much do you value the execution traces?
    
    empirical_sim = uniform.rvs(size = N) #Simulated Normalized Similarities 
    
    #n = 20 #Number of observed trials
    #links = 13 #Number of successfull links
    confirmed_links = np.r_[np.ones(5), np.zeros(5)] #This is for Bernulli
    
    #Specific Information
    ir_asm_prior1 = PriorFacade(
        prior_linkage = PriorChoice.SPECIFIC_INFORMATIVE,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.SPECIFIC_INFORMATIVE,
        domain_conf = domain_injection        
    )
    
    ir_asm_prior2 = PriorFacade(
        prior_linkage = PriorChoice.SPECIFIC_INFORMATIVE,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL,
        domain_conf = domain_injection        
    )
    
    ir_asm_prior3 = PriorFacade(
        prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.SPECIFIC_INFORMATIVE,
        domain_conf = domain_injection        
    )
    
    ir_asm_prior4 = PriorFacade(
        prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL,
        empirical_sim = empirical_sim,
        prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL,
        domain_conf = domain_injection        
    )
    
    hyper_params = ParametersFacade(
        variational_params = VariationalParameters(itera=10000),
        sampling_params = SamplingParameters(tune=1000)
    )

    #AssociationModels
    inference_key='b'
    test_ltr1(ir_asm_prior=ir_asm_prior4,confirmed_links=confirmed_links,execution_belief=execution_belief,
              hyper_params=hyper_params,
              info_prior=True,inference_key=inference_key)

    
    #test_ltr2(ir_asm_prior,confirmed_links,execution_belief,domain_injection,hyper_params,
    #          info_prior=False,inference_key=inference_key) 
    #test_ltr3(ir_asm_prior,confirmed_links,execution_belief,domain_injection,hyper_params,
    #          info_prior=False,inference_key=inference_key)
    #test_ltr_holistic(ir_asm_prior1,confirmed_links,execution_belief,hyper_params,
    #         info_prior=False,inference_key=inference_key)


# In[2]:


#Testing Cusality Facade

def test_ltr1(ir_asm_prior,confirmed_links,execution_belief,hyper_params,
              info_prior=True,inference_key='a'):
    ass_complexity = 1
    
    test_ltr_holistic(ir_asm_prior,confirmed_links,execution_belief,hyper_params, 
                      info_prior=info_prior,inference_key=inference_key,ass_complexity=ass_complexity)
    
def test_ltr2(ir_asm_prior,confirmed_links,execution_belief,hyper_params,
              info_prior=True,inference_key='a'):
    ass_complexity = 2
    
    test_ltr_holistic(ir_asm_prior,confirmed_links,execution_belief,hyper_params, 
                      info_prior=info_prior,inference_key=inference_key,ass_complexity=ass_complexity)
    
    
def test_ltr3(ir_asm_prior,confirmed_links,execution_belief,hyper_params,
              info_prior=True,inference_key='a'):
    ass_complexity = 3
    
    test_ltr_holistic(ir_asm_prior,confirmed_links,execution_belief,hyper_params, 
                      info_prior=info_prior,inference_key=inference_key,ass_complexity=ass_complexity)

    
def test_ltr_holistic(ir_asm_prior,confirmed_links,execution_belief,hyper_params, 
                      info_prior=True,inference_key='a',ass_complexity = 0):
    #execution_traces = [(2,2),(2,5)] #execution traces
    execution_traces = [(0.8,0.02),(0.7,0.01)] #Using Mean and Var
    
    if info_prior == True:
        associationModelFacade = CausalityFacadeAssociation( #Informative Priors
            confirmed_links,progressbar=True,ir_asm_prior=ir_asm_prior,hyper_params=hyper_params)
    else:
        associationModelFacade = CausalityFacadeAssociation( #Weakly Informative
            confirmed_links,progressbar=True,hyper_params=hyper_params)
        
    #Testing Sampling By Default 
    latent_traceability_recovery_all(associationModelFacade, execution_traces, 
                                 execution_belief, ass_complexity = ass_complexity,inference_key=inference_key)
    


# In[3]:


def latent_traceability_recovery_all(associationModelFacade, execution_traces, 
                                 execution_belief, ass_complexity, inference_key='a'):
    #Testing with 5000 samples with Selected Inference Method
    associationModelFacade.apply_latent_traceability_recovery(
        ass_complexity = ass_complexity, 
        execution_traces = execution_traces,
        inference_key=inference_key, 
        num_samples=5000,
        transitive_belief=execution_belief
    )
    
    if inference_key == 'a':
        evaluate_sampling_nuts(associationModelFacade,ass_complexity)
    elif inference_key == 'b':
        evaluate_vi(associationModelFacade,ass_complexity)
    elif inference_key == 'c':
        evaluate_map(associationModelFacade,ass_complexity)
    pass

def evaluate_sampling_nuts(model, ass_complexity = 0):
      
    if ass_complexity == 0:
        print(model.getPlausibleValues_holistic_ltr()) 
        print("GelmanRubin: " + str(model.get_ltr_holistic().compute_gelman_rubin(2500)))
    elif ass_complexity == 1:
        print(model.getPlausibleValues_ltr_1())
        print("GelmanRubin: " + str(model.get_ltr_1().compute_gelman_rubin(2500)))
    elif ass_complexity == 2:
        print(model.getPlausibleValues_ltr_2())
        print("GelmanRubin: " + str(model.get_ltr_2().compute_gelman_rubin(2500)))
    elif ass_complexity == 3:
        print(model.getPlausibleValues_ltr_3())
        print("GelmanRubin: " + str(model.get_ltr_3().compute_gelman_rubin(2500)))

    pass

def show_evaluation_vi(ltr, prob_of_links):
    print("Mean VI: " + str(prob_of_links))
    tracker = ltr.compute_vi_tracking()['mean']
    print(tracker[-1]) #Last Iteration
    print("Mean of Means: " + str(np.mean(tracker,axis=0))) #Column AVG
    
def evaluate_vi(model, ass_complexity = 0):
    
    if ass_complexity == 0:
        probabilitiesOfLinks = model.getPlausibleValues_holistic_ltr() 
        show_evaluation_vi(model.get_ltr_holistic(), probabilitiesOfLinks)
    elif ass_complexity == 1:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_1()
        show_evaluation_vi(model.get_ltr_1(), probabilitiesOfLinks)
    elif ass_complexity == 2:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_2()
        show_evaluation_vi(model.get_ltr_2(), probabilitiesOfLinks)
    elif ass_complexity == 3:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_3()
        show_evaluation_vi(model.get_ltr_3(), probabilitiesOfLinks)
    pass

#def show_evaluate_map(ltr, prob_of_links)

def evaluate_map(model, ass_complexity = 0):
    
    if ass_complexity == 0:
        probabilitiesOfLinks = model.getPlausibleValues_holistic_ltr() 
    elif ass_complexity == 1:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_1()
    elif ass_complexity == 2:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_2()
    elif ass_complexity == 3:
        probabilitiesOfLinks = model.getPlausibleValues_ltr_3()

    print(probabilitiesOfLinks)
    pass
    

