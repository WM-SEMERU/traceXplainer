
# coding: utf-8

# In[1]:


import numpy as np
from scipy.special import factorial
from scipy.stats import beta
import matplotlib.pyplot as plt
get_ipython().run_line_magic('matplotlib', 'inline')
#plt.rcParams['figure.figsize'] = (16,7)


# In[2]:


def likelihood(theta, n, x):
    """
    likelihood function for a binomial distribution

    n: [int] the number of experiments
    x: [int] the number of successes
    theta: [float] the proposed probability of success
    """
    return (factorial(n) / (factorial(x) * factorial(n - x)))             * (theta ** x) * ((1 - theta) ** (n - x))


# In[3]:


#the number of impressions for our facebook-yellow-dress campaign
n_impressions = 10.

#the number of clicks for our facebook-yellow-dress campaign
n_clicks = 7.
#observed click through rate
ctr = n_clicks / n_impressions
#0 to 1, all possible click through rates
possible_theta_values = list(map(lambda x: x/100., range(100)))


# In[4]:


print(possible_theta_values)
n = 10
x = 7


# In[5]:


#evaluate the likelihood function for possible click through rates
likelihoods = list(map(lambda theta: likelihood(theta, n, x) , possible_theta_values))


# In[6]:


print(np.argmax(likelihoods))
print(likelihoods)


# In[7]:


#pick the best theta
mle = possible_theta_values[np.argmax(likelihoods)]
print(mle)


# In[8]:


#plot
f, ax = plt.subplots(1)
ax.plot(possible_theta_values, likelihoods)
ax.axvline(mle, linestyle = "--")
ax.set_xlabel("Theta")
ax.set_ylabel("Likelihood")
ax.grid()
ax.set_title("Likelihood of Theta for New Campaign")
plt.show()


# In[9]:


import pymc3 as pm


# In[10]:


N = 100 #Number of trials or marketing campaings
impressions = np.random.randint(1,10000,size=N)
zero_to_one = [j/100 for j in range(100)] #values from 0 to 0.99
print(zero_to_one)


# In[11]:


true_a = 11.5
true_b = 48.5
p = np.random.beta(true_a,true_b,size = N)


# In[12]:


print(p)
print(impressions)


# In[13]:


clicks = np.random.binomial(impressions,p).astype(float) #sample number of clicks
click_through_rates = clicks / impressions


# In[14]:


print(clicks)
print(click_through_rates)


# In[15]:


#Fitting a beta distribution to the data
prior_parameters = beta.fit(click_through_rates, floc=0, fscale=1) #extract a,b from fit
prior_a, prior_b = prior_parameters[0:2]
prior_distribution = beta(prior_a, prior_b) #define prior distribution sample from prior
prior_samples = prior_distribution.rvs(10000)


# In[16]:


#Beta and Alfa
print(prior_a)
print(prior_b)


# In[17]:


#Plotting the Fitted Beta Distribution
fit_counts, bins = np.histogram(prior_samples, zero_to_one)
fit_counts = list(map(lambda x: float(x)/fit_counts.sum(), fit_counts)) #normalize histogram
f, ax = plt.subplots(1)
ax.plot(bins[:-1], fit_counts)

hist_ctr, bins = np.histogram(click_through_rates, zero_to_one) #Real Historial Data
hist_ctr = list(map(lambda x: float(x)/hist_ctr.sum(), hist_ctr)) #normalize histogram
ax.plot(bins[:-1], hist_ctr)
estimated_prior, previous_click_through_rates = ax.lines
ax.legend((estimated_prior, previous_click_through_rates),('Estimated Prior', 'Previous Click Through Rates'))
ax.grid()
ax.set_title("Comparing Empirical Prior with Previous Click Through Rates")
plt.show()


# In[18]:


print(fit_counts)


# In[19]:


obs_clicks = 7
obs_impressions = 10


# In[20]:


with pm.Model() as model:
    theta_prior = pm.Beta('prior',11.5,48.5) #Prior Beta Distribution, parameters can be fitted
    observations = pm.Binomial('obs', n=obs_impressions, p=theta_prior, observed=obs_clicks) #Likelihood
    start = pm.find_MAP() #Find good starting values for the sampling algorithm
    step = pm.NUTS() #MCMC algorithm
    trace = pm.sample(5000, step, start=start, progressbar=True)


# In[21]:


print(pm.summary(trace))
print(theta_prior.random(size=N))
print(start)


# In[22]:


from pymc3 import traceplot


# In[23]:


traceplot(trace[1500:])


# In[24]:


print(trace['prior'])
print(len(trace['prior']))


# In[25]:


#Posterior trace
pm.plot_posterior(trace)


# In[26]:


#Get histogram of samples from posterior distrubution of CTRs
posterior_counts, posterior_bins = np.histogram(trace['prior'],bins=zero_to_one)
posterior_counts = posterior_counts/float(posterior_counts.sum()) #normalized histogram
most_plausible_theta = np.mean(trace['prior']) #mean of the samples as the most plausible value
prior_counts, bins = np.histogram(prior_samples, zero_to_one)
prior_counts = list(map(lambda x:float(x)/prior_counts.sum(),prior_counts)) #normalized histogram


# In[27]:


print(posterior_counts)


# In[28]:


#Plotting
#plt.rcParams['figure.figsize']=(16,7)
f,ax = plt.subplots(1)
ax.plot(possible_theta_values, likelihoods) #Computing Likelihoods Evidence
ax.plot(bins[:-1], prior_counts, alpha=.2) #Priors from sampling
ax.plot(bins[:-1], posterior_counts) #Posterior
ax.axvline(most_plausible_theta, linestyle='--', alpha=.2)
line1, line2, line3, line4 = ax.lines
ax.legend((line1, line2, line3, line4),('Evidence','Prior','Posterior','Most-Plausible'), loc='upper left')

ax.set_xlabel("Theta")
ax.set_ylabel("Likelihoods")
ax.grid()
ax.set_title("Prior Distribution Updated with Some Evidence")
plt.show()


# In[45]:


#Different Posteriors if we observed a 0.7 click-trough rate from several impressions
#create our data:
traces = {}
for ad_impressions in [10, 100, 1000, 10000]: #maintaining observed CTR of 0.7
    clicks = np.array([ctr * ad_impressions])    #re-estimate the posterior for
    impressions = np.array([ad_impressions])    #increasing numbers of impressions
    with pm.Model() as model:
        theta_prior = pm.Beta('prior', 11.5, 48.5)
        observations = pm.Binomial('obs',n = impressions
                                   , p = theta_prior
                                   , observed = clicks)
        start = pm.find_MAP()
        step = pm.NUTS()
        trace = pm.sample(5000
                          , step
                          , start=start
                          , progressbar=True)

        traces[ad_impressions] = trace


# In[72]:


f, ax = plt.subplots(1)
ax.plot(bins[:-1],prior_counts, alpha = .9)


# In[73]:


counts = {}
for ad_impressions in [10, 100, 1000, 10000]:
    trace = traces[ad_impressions]
    posterior_counts, posterior_bins = np.histogram(trace['prior'], bins=[j/100. for j in range(100)])
    posterior_counts = posterior_counts / float(len(trace))
    ax.plot(bins[:-1], posterior_counts)


# In[74]:


print(ax.lines)


# In[75]:


line0, line1, line2, line3, line4 = ax.lines
get_ipython().run_line_magic('matplotlib', 'inline')


# In[76]:


ax.legend((line0, line1, line2, line3, line4), ('Prior Distribution'
                                                ,'Posterior after 10 Impressions'
                                                , 'Posterior after 100 Impressions'
                                                , 'Posterior after 1000 Impressions'
                                                ,'Posterior after 10000 Impressions'))
ax.set_xlabel("Theta")
ax.axvline(ctr, linestyle = "--", alpha = .5)
ax.grid()
ax.set_ylabel("Probability of Theta")
ax.set_title("Posterior Shifts as Weight of Evidence Increases")
plt.show()

