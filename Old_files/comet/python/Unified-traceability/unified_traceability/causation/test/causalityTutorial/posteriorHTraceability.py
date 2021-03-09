
# coding: utf-8

# In[1]:


import numpy as np
from scipy.special import factorial
from scipy.stats import beta, halfnorm
import matplotlib.pyplot as plt
import pymc3 as pm
get_ipython().run_line_magic('matplotlib', 'inline')


# In[2]:


#Fist Step is to Generate the "Prior" Data
N = 20 #Number of Experiments (this is the different empircal evaluations)
logitInv = lambda x: np.exp(x)/(1.0+np.exp(x)) #Sigmoid Function

mu, sigma = 0, 0.1 # mean and standard deviation
#domain_injection = np.random.normal(mu, sigma, N) #Domain-Knowledge of Datasets
domain_injection = 0.5 #Domain-Knowledge of Datasets
empirical_sim = halfnorm.rvs(size = N) #Simulated Similarities 
s = domain_injection + empirical_sim #Linear Combinations of Parameters

link_rates = list(map(logitInv,s)) #History of Rates of One Link


# In[3]:


print(s)
print(link_rates)


# In[4]:


#Second Step is to generate the Prior Distribution
#Fitting a beta distribution to the rates
prior_parameters = beta.fit(link_rates, floc=0, fscale=1) #extract a,b from fit
prior_a, prior_b = prior_parameters[0:2]
prior_distribution = beta(prior_a, prior_b) #define prior distribution sample from prior
prior_samples = prior_distribution.rvs(10000)


# In[5]:


print(prior_a)
print(prior_b)


# In[6]:


#Plotting the Fitted Beta Distribution
zero_to_one = [j/100 for j in range(100)] #values from 0 to 0.99
fit_counts, bins = np.histogram(prior_samples, zero_to_one)
fit_counts = list(map(lambda x: float(x)/fit_counts.sum(), fit_counts)) #normalize histogram


# In[7]:


f, ax = plt.subplots(1)
ax.plot(bins[:-1], fit_counts)
estimated_prior = ax.lines
ax.legend(estimated_prior,'Estimated Prior')
ax.grid()
ax.set_title("Empirical Prior")
plt.show()


# In[8]:


#Lets say that this is our observed data
n = 20 #Number of observed trials
links = 13 #Number of successfull links


# In[9]:


#Third Step is computing the posteiors with Inference Algorithms
#Sampling with MCMC
with pm.Model() as model:
    #HyperPriors
    alpha = pm.Normal('alpha', mu = prior_a, sd= 100)
    beta = pm.Normal('beta', mu = prior_b, sd= 100)
    #Prior
    theta_prior = pm.Beta('prior',prior_a,prior_b) #Prior Beta Distribution, parameters can be fitted
    #Likelihood
    observations = pm.Binomial('obs', n=n, p=theta_prior, observed=links) #Likelihood
    #Inference
    start = pm.find_MAP() #Find good starting values for the sampling algorithm
    step = pm.NUTS() #MCMC algorithm
    trace = pm.sample(5000, step, start=start, progressbar=True)
    approx = pm.fit() #Variational?
    tracevar = approx.sample(5000)


# In[12]:


#Summary
print(pm.summary(trace))
print(pm.summary(tracevar))
print(start['prior'])


# In[11]:


#Posterior trace
pm.plot_posterior(trace)
pm.plot_posterior(tracevar)


# In[37]:


#Get histogram of samples from posterior distrubution of CTRs
posterior_counts, posterior_bins = np.histogram(trace['prior'],bins=zero_to_one)
posterior_counts = posterior_counts/float(posterior_counts.sum()) #normalized histogram
most_plausible_theta = np.mean(trace['prior']) #mean of the samples as the most plausible value
prior_counts, bins = np.histogram(prior_samples, zero_to_one)
prior_counts = list(map(lambda x:float(x)/prior_counts.sum(),prior_counts)) #normalized histogram


# In[38]:


#Most Plausible Theta
print(most_plausible_theta)


# In[39]:


#Plotting
#plt.rcParams['figure.figsize']=(16,7)
f,ax = plt.subplots(1)
ax.plot(bins[:-1], prior_counts, alpha=.2) #Priors from sampling
ax.plot(bins[:-1], posterior_counts) #Posterior
ax.axvline(most_plausible_theta, linestyle='--', alpha=.5)
line1, line2, line3 = ax.lines
ax.legend((line1, line2, line3),('Prior','Posterior','Most-Plausible'), loc='upper left')

ax.set_xlabel("Theta")
ax.set_ylabel("Likelihoods")
ax.grid()
ax.set_title("Prior Distribution Updated with Some Evidence")
plt.show()


# In[40]:


#Fourth step is checking the model (convergence analysis)
#Gelman-Rubin
print(pm.gelman_rubin(trace))


# In[32]:


pm.forestplot(trace)

