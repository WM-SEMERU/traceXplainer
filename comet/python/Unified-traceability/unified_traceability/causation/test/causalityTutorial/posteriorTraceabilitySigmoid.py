
# coding: utf-8

# In[9]:


import numpy as np
from scipy.special import factorial
from scipy.stats import beta, halfnorm
import matplotlib.pyplot as plt
import pymc3 as pm
from pymc3.math import invlogit
import seaborn as sns
from theano import shared
get_ipython().run_line_magic('matplotlib', 'inline')


# In[10]:


#Fist Step is to Generate the "Prior" Data
N = 4 #Number of Experiments (this is the different techniques)
number_of_links = 1 # It can vary to inspect different thresholds

mu, sigma = 0, 1 # mean and standard deviation
#domain_injection = np.random.normal(mu, sigma, number_of_links) #Domain-Knowledge of Datasets
domain_injection = 0.01
empirical_sim = halfnorm.rvs(size = N) #Simulated Similarities per link
empirical_sim_shared = shared(empirical_sim)


# In[11]:


print(domain_injection)
print(empirical_sim)


# In[12]:


#Lets say that this is our observed data
n = 1 * np.ones(N) #Number of independent links
links = np.array([0, 1, 1, 1]) #Number of successfull links


# In[13]:


print(n)
print(links)


# In[14]:


#Third Step is computing the posteiors with Inference Algorithms
#Sampling with MCMC
with pm.Model() as traceability_model:
    #Prior
    alfa =  pm.Normal('alpha', mu=0, sd=100)
    beta = pm.Normal('beta', mu=0, sd=100)
    #Probability of linkage
    s = domain_injection + alfa + empirical_sim_shared*beta #Linear Combinations of Parameters
    theta_prior = invlogit(s) #History of Rates of Links
    #Data Likelihood
    observations = pm.Binomial('obs', n=n, p=theta_prior, observed=links) #Likelihood
    #Posterior
    start = pm.find_MAP() #Find good starting values for the sampling algorithm
    step = pm.NUTS() #MCMC algorithm
    trace = pm.sample(5000, step, start=start, progressbar=True)


# In[7]:


#Variational inference
with pm.Model() as model:
    theta_prior = pm.Beta('prior',prior_a,prior_b) #Prior Beta Distribution, parameters can be fitted
    observations = pm.Binomial('obs', n=n, p=theta_prior, observed=links) #Likelihood
    #Posterior
    start = pm.find_MAP() #Find good starting values for the sampling algorithm
    approx = pm.fit() #Variational?
    trace = approx.sample(5000)


# In[15]:


#Summary
tr1 = trace[2500:]
print(pm.summary(trace))
print(start)


# In[9]:


#Posterior Predictive
empirical_sim_predict = np.random.uniform(0,1,size=20) #Prediction of similarities
empirical_sim_shared.set_value(empirical_sim_predict) #Update the values


# In[ ]:


ppc = pm.sample_ppc(tr1, model=traceability_model, samples=500) #Use the updated values and do prediction


# In[16]:


#Plotting Similarity vs Linkage
f = lambda a, b, domain, sim: np.exp(a + domain + sim*b)/(1.0 + np.exp(a + domain + sim*b))
domain = domain_injection
sim = np.linspace(-10, 10, 100)
a = trace.get_values('alpha').mean() #Posterior 
b = trace.get_values('beta').mean() #Posterior
plt.plot(sim, f(a, b, domain,sim), label="alpha={}, beta={}".format(round(a, 1),round(b, 1)))
plt.plot(sim, f(0, 1, domain,sim), label="alpha=1, beta=0")
#plt.plot(domain, f(a, b, domain,sim))
#plt.scatter(x, y/5, s=50);
plt.xlabel('Similarity')
plt.ylabel('Linkage')
plt.legend(loc=4)


# In[ ]:


sns.kdeplot(trace['alpha'][2500:], trace['beta'][2500:])
plt.xlabel(r"$\alpha$",size=20)
plt.ylabel(r"$\beta$",size=20)


# In[ ]:


#Posterior Predictive
empirical_sim_predict = np.random.uniform(0,1,size=20) #Prediction of similarities
empirical_sim_shared.set_value(empirical_sim_predict) #Update the values


# In[ ]:


print(empirical_sim_predict)


# In[ ]:


ppc = pm.sample_ppc(tr1, model=traceability_model, samples=500) #Use the updated values and do prediction


# In[ ]:


#Plotting Prediction
plt.errorbar(x=empirical_sim_predict, y=np.asarray(ppc['obs']).mean(axis=0), yerr=np.asarray(ppc['obs']).std(axis=0), linestyle='', marker='o')
#plt.plot(empirical_sim, links, 'o')
plt.xlabel('similarity',size=15)
plt.ylabel('linkage',size=15)


# In[ ]:


#Posterior trace
pm.plot_posterior(trace)


# In[ ]:


#Fourth step is checking the model (convergence analysis)
#Gelman-Rubin
print(pm.gelman_rubin(trace))


# In[ ]:


pm.forestplot(trace)

