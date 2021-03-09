
# coding: utf-8

# In[1]:


get_ipython().run_line_magic('matplotlib', 'inline')
import warnings
warnings.filterwarnings('ignore')
import numpy as np
import pandas as pd
import statsmodels.api as sm
import scipy.stats
import matplotlib.pyplot as plt


# In[2]:



df1 = pd.DataFrame({
    'A': [1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0],
    'Y': [
        200, 150, 220, 110, 50, 180, 90, 170,
        170, 30, 70, 110, 80, 50, 10, 20
    ]
})


# In[3]:



fig, ax = plt.subplots()

ax.scatter(df1['A'], df1['Y'])
ax.set_xlabel('A')
ax.set_ylabel('Y');


# In[4]:


df1.groupby('A').describe()


# In[5]:


#"Now suppose treatment A is a polytomous variable that can take 4 possible values"
df2 = pd.DataFrame({
    'A': [
        1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 3, 3, 4, 4, 4, 4
    ],
    'Y': [
        110, 80, 50, 40, 170, 30, 70, 50,
        110, 50, 180, 130, 200, 150, 220, 210
    ]
})


# In[6]:



fig, ax = plt.subplots()

ax.scatter(df2['A'], df2['Y'])
ax.set_xlabel('A')
ax.set_ylabel('Y');


# In[7]:


df2.groupby('A').describe()


# In[8]:


A, Y = zip(*(
    (3, 21),
    (11, 54),
    (17, 33),
    (23, 101),
    (29, 85),
    (37, 65),
    (41, 157),
    (53, 120),
    (67, 111),
    (79, 200),
    (83, 140),
    (97, 220),
    (60, 230),
    (71, 217),
    (15, 11),
    (45, 190),
))


# In[9]:


df3 = pd.DataFrame({'A': A, 'Y': Y, 'constant': np.ones(16)})


# In[10]:


fig, ax = plt.subplots()

ax.scatter(df3.A, df3.Y)
ax.set_xlabel('A')
ax.set_ylabel('Y');


# In[11]:


ols = sm.OLS(Y, df3[['constant', 'A']])
res = ols.fit()


# In[12]:


summary = res.summary()
summary.tables[1]


# In[13]:


n = df3.shape[0]
yvar = (res.resid * res.resid).sum() / (n - 2)  # = res.mse_resid
xval = np.array([[1, 90]])
X = df3[['constant', 'A']]
XpXinv = np.linalg.inv(np.dot(X.T, X))
se_mean = np.sqrt(yvar * np.dot(xval, np.dot(XpXinv, xval.T)))[0, 0]


# In[14]:



t = scipy.stats.t.ppf(0.975, n - 2)
ypred = res.predict([[1, 90]])[0]
print('           estimate      95% C.I.')
print(
    'E[Y|A=90]   {:>6.2f}   ({:>6.2f}, {:>6.2f})'.format(
        ypred, ypred - t * se_mean, ypred + t * se_mean
))


# In[15]:


fig, ax = plt.subplots()

ax.scatter(df3.A, df3.Y)
ax.plot(df3.A, res.predict(df3[['constant', 'A']]))
ax.set_xlabel('A')
ax.set_ylabel('Y');

