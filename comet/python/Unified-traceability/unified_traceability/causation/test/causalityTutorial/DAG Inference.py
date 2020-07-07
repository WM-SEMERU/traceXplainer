
# coding: utf-8

# In[2]:


get_ipython().run_line_magic('matplotlib', 'inline')

import numpy as np 
import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
from causality.inference.search import IC
from causality.inference.independence_tests import RobustRegressionTest
from pprint import pprint
import matplotlib.pyplot as pp
from causality.analysis.dataframe import CausalDataFrame


# In[7]:


#Testing Binomial Distribution with Z as confounder
N = 1000
z = np.random.normal(1., size=N)
xx = np.random.binomial(1, p=1./(1. + np.exp(z/.1)))
y = xx + z + np.random.normal(size=N)


# In[10]:


print(list(y))
print(y)


# In[ ]:


# It's easy to create a data frame
df = pd.DataFrame({'x': xx, 'y': y, 'z': z})


# In[ ]:


print(df)


# In[ ]:


# define the variable types: 'c' is 'continuous'.  The variables defined here
# are the ones the search is performed over  -- NOT all the variables defined
# in the data frame.
variable_types = {'x' : 'c', 'y' : 'c', 'z' : 'c'}


# In[ ]:


# run the search
ic_algorithm = IC(RobustRegressionTest)
graph = ic_algorithm.search(df, variable_types)


# In[ ]:


E = graph.edges(data=True)


# In[ ]:


pprint(E)


# In[ ]:


#Accesing Arrows
pprint(graph)


# In[ ]:


# generate some toy data:
SIZE = 2000
x1 = np.random.normal(size=SIZE)
x2 = x1 + np.random.normal(size=SIZE)
x3 = x1 + np.random.normal(size=SIZE)
x4 = x2 + x3 + np.random.normal(size=SIZE)
x5 = x4 + np.random.normal(size=SIZE)
# load the data into a dataframe:
X = pd.DataFrame({'x1' : x1, 'x2' : x2, 'x3' : x3, 'x4' : x4, 'x5' : x5})


# In[ ]:


# define the variable types: 'c' is 'continuous'.  The variables defined here
# are the ones the search is performed over  -- NOT all the variables defined
# in the data frame.
variable_types = {'x1' : 'c', 'x2' : 'c', 'x3' : 'c', 'x4' : 'c', 'x5' : 'c'}


# In[ ]:


# run the search
ic_algorithm = IC(RobustRegressionTest)
graph = ic_algorithm.search(X, variable_types)


# In[ ]:


E = graph.edges(data=True)


# In[ ]:



print(E)


# In[ ]:


pprint(graph)


# In[ ]:


#plotting the graph
print(graph.number_of_nodes())
print(graph.number_of_edges())
print(list(graph.edges))

plt.subplot(121)
G = nx.DiGraph()
G.add_edges_from(list(graph.edges))
nx.draw(G, with_labels=True, arrows=True, font_weight='bold')
plt.subplot(122)
pos = nx.spring_layout(graph, iterations=5)
nx.draw(graph,pos, with_labels=True, font_weight='bold')


# In[ ]:


pos = nx.spring_layout(graph)
nodes = nx.draw_networkx_nodes(graph, pos, node_size=1, node_color='blue')
edges = nx.draw_networkx_edges(graph, pos, node_size=1, arrowstyle='->',
                               arrowsize=10,
                               edge_cmap=plt.cm.Blues, width=2)
ax = plt.gca()
ax.set_axis_off()
plt.show()


# In[ ]:


from causality.estimation.adjustments import AdjustForDirectCauses
from networkx import DiGraph


# In[ ]:


g = DiGraph()

g.add_nodes_from(['x1','x2','x3','x4', 'x5'])
g.add_edges_from([('x1','x2'),('x1','x3'),('x2','x4'),('x3','x4')])
adjustment = AdjustForDirectCauses()


# In[ ]:


adjustment.admissable_set(g, ['x2'], ['x3'])
set(['x1'])


# In[ ]:


from causality.estimation.nonparametric import CausalEffect


# In[ ]:


admissable_set = adjustment.admissable_set(g,['x2'], ['x3'])
effect = CausalEffect(X, ['x2'], ['x3'], variable_types=variable_types, admissable_set=list(admissable_set))
x = pd.DataFrame({'x2' : [0.], 'x3' : [0.]})

effect.pdf(x)


# In[ ]:


y = pd.DataFrame({'x2' : [1.], 'x3' : [1.]})
effect.pdf(y)


# In[ ]:


z = pd.DataFrame({'x2' : [0.], 'x3' : [1.]})
effect.pdf(z)


# In[ ]:


print(X['x2'].mean())


# In[16]:


#Presenting Results of Interventions
N = 1000

z = np.random.normal(1., size=N)
x = np.random.binomial(1, p=1./(1. + np.exp(-z/.1)))
y = x + z + np.random.normal(size=N)

# It's easy to create a data frame
df = CausalDataFrame({'x': x, 'y': y, 'z': z})


# In[17]:


pprint(x)


# In[3]:


df.zplot(x='x', y='y', z_types={'z': 'c'}, z=['z'], kind='bar', bootstrap_samples=500); pp.ylabel("$E[Y|do(X=x)]$"); pp.show()


# In[6]:


#controlling the confidence
df.zplot(x='x', y='y', z=['z'], z_types={'z': 'c'}, kind='bar', bootstrap_samples=500, confidence_level=0.80)


# In[4]:


df.groupby('x').mean().reset_index().plot(x='x', y='y', kind='bar'); pp.ylabel("$E[Y|X=x]$"); pp.show()


# In[5]:


df.groupby('x').mean().plot(y='y', 
                            kind='bar', 
                            yerr=1.96*df.groupby('x').std() / np.sqrt(df.groupby('x').count())); 
pp.ylabel("$E[Y|X=x]$"); pp.show()


# In[8]:


#zmean method to keep values
summary = df.zmean(x='x', y='y', z=['z'], z_types={'z': 'c'}, bootstrap_samples=500, confidence_level=0.95)


# In[10]:


pprint(summary)


# In[3]:


#Same experiment but with other distributions
#Presenting Results of Interventions
N = 1000

z = np.random.normal(1., size=N)
x = z + np.random.normal(size=N)
y = x + z + np.random.normal(size=N)

# It's easy to create a data frame
df = CausalDataFrame({'x': x, 'y': y, 'z': z})


# In[4]:


df.zplot(x='x', y='y', z=['z'], z_types={'z': 'c'}, kind='line', model_type='kernel')


# In[6]:


df.zmean(x='x', y='y', z=['z'], z_types={'z': 'c'}, bootstrap_samples=50, confidence_level=0.95)


# In[15]:


#zmean method to keep values
#df.zmean(x='x', y='y', z=['z'], z_types={'z': 'c'}, bootstrap_samples=500, confidence_level=0.95)
df.plot(x='x', y='y', style='bo', alpha=0.2, kind='scatter')


# In[ ]:


#Regression Test
import pandas as pd
import numpy as np
from causality.inference.search import IC
from causality.inference.independence_tests import RobustRegressionTest


# In[ ]:


size = 1000
x = np.random.normal(size=size)
y = x + np.random.normal(size=size)
z = y + np.random.normal(size=size)
X = pd.DataFrame({'x': x, 'y': y, 'z': z})


# In[ ]:


X.head()


# In[ ]:


from statsmodels.regression.linear_model import OLS


# In[ ]:


model = OLS(X['z'], X[['y']])
result = model.fit()
result.summary()

