
# coding: utf-8

# In[11]:


'''
Version 1.1
@danaderp June-2018
Intervention File is a set of methods that compute Causal Graph Inference from Observational Data
Moreover, it includes techniques to generate interventions as P(y|do(x))
'''
import pandas as pd
import random as rd
import numpy as np
from networkx import nx
import matplotlib.pyplot as plt

#Causal Library to be Employed
from causality.inference.search import IC
from causality.inference.independence_tests import RobustRegressionTest
from pprint import pprint
from causality.estimation.adjustments import AdjustForDirectCauses
from causality.estimation.nonparametric import CausalEffect
from scipy.stats import beta, halfnorm, uniform
from causality.analysis.dataframe import CausalDataFrame
from sklearn.neural_network import MLPRegressor
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.pipeline import Pipeline


class CausalGraph:
    """This class is a Directed Acyclic Graph, it includes the Inductive Causation Algorithm to learn the graph
    from observational data"""
    def __init__(self, data_links, variable_types):
            """
            @data_links: a Pandas DataFrame with the observational data from links as stochastic variables
            # load the data into a dataframe:
                X = pd.DataFrame({'x1' : x1, 'x2' : x2, 'x3' : x3, 'x4' : x4, 'x5' : x5})
            @variable_types: data type of stochastic variables provided
            # define the variable types: 'c' is 'continuous'.  The variables defined here
            # are the ones the search is performed over  -- NOT all the variables defined
            # in the data frame.
                variable_types = {'x1' : 'c', 'x2' : 'c', 'x3' : 'c', 'x4' : 'c', 'x5' : 'c'}
            """
            self.__data_links = data_links
            self.__variable_types = variable_types
            self.__learning_inductive_causation() #Calling Inductive Causation
            pass
        
    def __learning_inductive_causation(self):
            """Learning a Causal Graph by using the IC algorithm by Pearl"""
            ic_algorithm = IC(RobustRegressionTest)
            self.__graph = ic_algorithm.search(self.__data_links, self.__variable_types)
            self.printing_graph(self.__graph)
            #Create a Default DiGraph
            G = nx.DiGraph()
            G.add_edges_from(list(self.__graph.edges))
            self.__di_graph = G
            self.printing_graph(self.__di_graph)
            pass
    
    def printing_graph(self,graph):
            """Pretty Print of the Generated Graph
            """
            E = graph.edges(data=True)
            pprint(E)
            pos = nx.spring_layout(graph)
            nx.draw(graph,pos, with_labels=True, arrows=True, font_weight='bold')
            ax = plt.gca()
            ax.set_axis_off()
            plt.show()
            pass
        
    def __compute_causal_effect(self, node_x, node_y, graph):
        """
        @node_x: list of a variable to be controlled
        @node_y: list of a variable to be affected
        """
        adjustment = AdjustForDirectCauses()
        admissable_set = adjustment.admissable_set(graph, node_x, node_y)
        effect = CausalEffect(self.__data_links, node_x, node_y, 
                              variable_types=self.__variable_types, admissable_set=list(admissable_set))
        return effect
    
    def build_directed_causal_graph(self, list_nodes, list_edges):
        """ Directed Graph Structure 
        @list_nodes: a list of nodes (labels) of the graph ['x1','x2','x3','x4', 'x5']
        @list_edges: a list of edges (labels) of the graph [('x1','x2'),('x1','x3'),('x2','x4'),('x3','x4')]
        Take into account that labels must be consistent with original learned graph
        """
        g = nx.DiGraph()
        g.add_nodes_from(list_nodes)
        g.add_edges_from(list_edges)
        self.__di_graph = g
        self.printing_graph(self.__di_graph)
        pass
    
    def do_intervention_non_parametric(self, node_x, node_y, intervention):
        """ Nonparametric Effects Estimation by built DAG 
        (DAG must be a subset of learned graph)
        Making an intervention with fixed parameters P(y|do{x})
        The causal effect is the distribution of Y will take on if we intervine to 
        set the value of X to x
        @node_x: label of x variable or counfounder
        @node_y: label of y variable
        @intervention: A pandas dataframe with with exact values of x and y to compute the causal effect
        """
        effect = self.__compute_causal_effect(node_x, node_y, self.__di_graph)
        return effect.pdf(intervention)
    
    def get_causal_graph(self):
        return self.__graph


class CausalFrame: 
    """This class is a direct analysis of causal relationships within observational data"""
    def __init__(self, treatment=None, outcome=None, confounders=None, ltr=None):
        """Building the causal dataframe to manipulate variables from observable data
        @treatment: this is a numpy of data distribution for X treament
        @outcome: this is a numpy of distribution for Y outcomes
        @confounders: this is a n-tuple composed of numpy arrays for the confounders 
        """
        if ltr is None:
            dictionary = {}
            dictionary['x'] = treatment
            dictionary['y'] = outcome
            for var in confounders:
                key = 'z'+str(confounders.index(var))
                dictionary[key] = var
            self.__causal_frame = CausalDataFrame(dictionary)
        else:
            #print("LTR Causal Frame")
            self.__causal_frame = CausalDataFrame(ltr)
        pass
    
    def do_expected_value_non_parametric(self, confounders, types, sampling=500,
                                        confidence=0.95, treatment='x', outcome='y'):
        """Computing Conditional Expection of an Intervention E[Y|do(X=x)]
        It is recomended to compute interventions on categorical treatments
        @df: Causal DataFrame composed of tratments, outcomes and confounders
        @confounders: this is an array of confounders e.g ['z1', 'z2']
        @types: this is an array for the confounders types e.g {'z1': 'c', 'z2': 'c'}
        set(['o', 'u', 'c']), for 'ordered', 'unordered discrete', or 'continuous'
        """
        return self.__causal_frame.zmean(x=treatment, y=outcome,
                                         z=confounders, z_types=types, bootstrap_samples=sampling, confidence_level=confidence)
    
    def print_expectation_categorical(self, confounders, types, treatment='x', outcome='y', sampling=500, confidence=0.95):
        """Generating a bar plot for categorical treatment"""
        self.__causal_frame.zplot(x=treatment, y=outcome, z_types=types, z=confounders,
                                  kind='bar', bootstrap_samples=sampling); plt.ylabel("$E[Y|do(X=x)]$"); plt.show()
        pass

    def print_conditional_categorical(self, treatment='x', outcome='y'):
        df = self.__causal_frame
        df.groupby(treatment).mean().plot(y=outcome,
                                          kind='bar',
                                          yerr=1.96*df.groupby(treatment).std() / np.sqrt(df.groupby(treatmentfo).count())
                                          ); plt.ylabel("$E[Y|X=x]$"); plt.show()
        pass

    #Plotting confounded data
    def confounding_continuous(self,treatment='x', outcome='y'):
        return self.__causal_frame.plot(x=treatment, y=outcome, style='bo', alpha=0.1, kind='scatter')

    #Continous Recovery (without confounding)
    def recover_relation_by_perceptron(self, confounders, types, treatment='x', outcome='y'):

        model = MLPRegressor(hidden_layer_sizes=(128,128,128), max_iter=100, learning_rate_init=0.01)
        model.fit(self.__causal_frame[[treatment] + confounders], self.__causal_frame[outcome])
        return self.__causal_frame.zplot(x=treatment, y=outcome,
                                         z=confounders, z_types=types, kind='line', fitted_model=model)

    def recover_relation_by_random_forest(self, confounders, types, treatment='x', outcome='y'):

        return self.__causal_frame.zplot(x=treatment, y=outcome,
                                         z=confounders, z_types=types, kind='line')

    def recover_relation_by_kernel_regression(self, confounders, types, treatment='x', outcome='y'):

        return self.__causal_frame.zplot(x=treatment, y=outcome,
                                         z=confounders, z_types=types, kind='line', model_type='kernel')

    def recover_relation_by_lineal_regression(self, confounders, types, treatment='x', outcome='y'):

        return self.__causal_frame.zplot(x=treatment, y=outcome,
                                         z=confounders, z_types=types, kind='line', model=LinearRegression)

    def recover_relation_by_polynomial(self, confounders, types, degree=2, treatment='x', outcome='y'):
        model = Pipeline([('poly', PolynomialFeatures(degree=degree)),
                          ('linear', LinearRegression(fit_intercept=False))])

        model.fit(self.__causal_frame[[treatment] + confounders], self.__causal_frame[outcome])
        return self.__causal_frame.zplot(x=treatment, y=outcome,
                                         z=confounders, z_types=types, kind='line', fitted_model=model)
        
    def get_causal_frame(self):
        return self.__causal_frame

    def set_causal_frame(self, causal_frame):
        self.__causal_frame = causal_frame

