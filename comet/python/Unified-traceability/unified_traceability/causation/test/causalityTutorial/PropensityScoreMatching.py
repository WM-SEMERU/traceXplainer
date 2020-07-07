
# coding: utf-8

# In[16]:


get_ipython().run_line_magic('matplotlib', 'inline')
import pandas as pd
import numpy as np
from causality.estimation.parametric import PropensityScoreMatching

N = 10000
z1 = np.random.normal(size=N)
z2 = np.random.normal(size=N)
z3 = np.random.normal(size=N)


# In[17]:


p_d = 1. / (1. + np.exp(-(z1 + z2 + z3)/4.))
d = np.random.binomial(1, p=p_d)


# In[18]:


y0 = np.random.normal()
y1 = y0 + z1 + z2 + z3


# In[19]:


y = (d==1)*y1 + (d==0)*y0
X = pd.DataFrame({'d': d, 'z1': z1, 'z2': z2, 'z3': z3, 'y': y, 'y0': y0, 'y1': y1, 'p': p_d})


# In[20]:


X[X['d'] == 1].mean()['y'] - X[X['d'] == 0].mean()['y']


# In[21]:


(y1 - y0).mean()


# In[22]:


matcher = PropensityScoreMatching()


# In[23]:


matcher.estimate_ATE(X, 'd', 'y', {'z1': 'c', 'z2': 'c', 'z3': 'c'})


# In[24]:


matcher.check_support(X, 'd', {'z1': 'c', 'z2': 'c', 'z3': 'c'})


# In[25]:


matcher.assess_balance(X, 'd', {'z1': 'c', 'z2': 'c', 'z3': 'c'})


# In[26]:


X = matcher.score(X, assignment='d', confounder_types={'z1': 'c', 'z2': 'c', 'z3': 'c'})


# In[27]:


treated, control = matcher.match(X, assignment='d')


# In[28]:


matcher.assess_balance(treated.append(control), 'd', {'z1': 'c', 'z2': 'c', 'z3': 'c'})


# In[29]:


matcher.assess_balance(X, 'd', {'z1': 'c', 'z2': 'c', 'z3': 'c', 'propensity score': 'c'})


# In[30]:


matcher.assess_balance(treated.append(control), 'd', {'z1': 'c', 'z2': 'c', 'z3': 'c', 'propensity score': 'c'})

