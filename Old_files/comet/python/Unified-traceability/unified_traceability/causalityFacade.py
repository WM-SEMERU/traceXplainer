"""
Version: 1.6
@danaderp July 2018
Provide a unified interface to a set of causality interfaces.
CausalityFacade is a higher-level interface that makes the causality module easier to use.
"""

from causation.association import PosteriorTraceability, AssociationPrior, PriorChoice, VariationalParameters, \
    SamplingParameters, ProbabilisticModel
from causation.intervention import CausalGraph, CausalFrame

from scipy.stats import halfnorm, uniform
import sys
import matplotlib.pyplot as plt


class PriorFacade:

    def __init__(self,
                 prior_linkage = PriorChoice.WEAKLY_INFORMATIVE_BL,
                 prior_domain = PriorChoice.WEAKLY_INFORMATIVE_BL,
                 empirical_sim=[],
                 domain_conf=0.5,
                 d_penalty=0.5
                 ):
        """ Definition of Association Model Priors
        @empirical_sim: a vector with similarity values from empirical configurations
        @prior_choice: the type of Prior in the model
        """
        self.__ass_prior = AssociationPrior(
            prior_linkage = prior_linkage,
            prior_domain = prior_domain,
            domain_conf = domain_conf,
            d_penalty = d_penalty,
            empirical_sim = empirical_sim
        )
        pass

    def get_asm_prior(self) -> AssociationPrior:
        return self.__ass_prior

    pass

class ParametersFacade:

    def __init__(
            self,
            variational_params=VariationalParameters(
                learning_rate=2e-4, itera=50000
            ),
            sampling_params=SamplingParameters(
                max_treedepth=10, target_accept=0.90, tune=500, cores=2, chains=4 #Default Parameters
            )
    ):
        self.__vi_params = variational_params
        self.__sampling_params = sampling_params
        pass

    def get_vi_params(self):
        return self.__vi_params

    def get_sampling_params(self):
        return self.__sampling_params


class LatentTraceabilityRecovery:
    def __init__(self, probabilistic_model=ProbabilisticModel()):
        self.__probabilistic_model = probabilistic_model

    def get_ltr(self):
        return self.__probabilistic_model

    def set_ltr(self, probabilistic_model):
        self.__probabilistic_model = probabilistic_model

    def get_map_linkage_value(self):
        return self.__probabilistic_model.get_map_linkage_value()

    # GetTraces
    def get_traces(self):
        return self.__probabilistic_model.get_pymc3_traces()

    #Mean and Deviation
    def get_mean_linkage(self):
        return self.__probabilistic_model.get_expected_linkage_value()

    def get_dev_linkage(self):
        return self.__probabilistic_model.get_standard_dev_linkage_value()

    #Persistance
    def save_ltr(self, file):
        """ This method saves the probabilistic model in .pkl file
        @file: path and name of the model, should include the extension .pkl"""
        self.__probabilistic_model.save_a_trace(file=file)

    def load_ltr(self, file):
        """This method loads a probabilistic model from .pkl file
        @file: path and name of the model, should include the extension .pkl"""
        self.__probabilistic_model.load_a_trace(file=file)

    # Evaluation
    def compute_gelman_rubin(self, num_samples):
        try:
            return self.__probabilistic_model.get_gelman_rubin(sub_sampling=num_samples)
        except:
            print("You must first apply LTR with complexity 3", sys.exc_info()[0])
            return 0.0

    def write_gelman_rubin_to_file(self, num_samples, title, filename, path):
        values = self.compute_gelman_rubin(num_samples)
        with open(path + filename, 'w+') as output_file:
            output_file.write(title + '\n')
            for key in values.keys():
                output_file.write(str(key) + ': ' + str(values[key]) + '\n')

    def show_elbo_hist(self):
        return self.__probabilistic_model.get_vi_hist()

    def compute_vi_tracking(self):
        return self.__probabilistic_model.get_vi_tracker()

    def show_summary(self, num_samples):
        self.__probabilistic_model.get_summary(sub_sampling=num_samples)
        pass

    def show_trace_plot(self, num_samples):
        self.__probabilistic_model.get_trace_plot(sub_sampling=num_samples)
        pass

    def show_vi_convergence(self, title=None, filename=None, path=''):

        tracker = self.compute_vi_tracking()
        hist = self.show_elbo_hist()

        fig = plt.figure(figsize=(16, 9))
        if title is not None:
            plt.suptitle(title)
        mu_ax = fig.add_subplot(221)
        std_ax = fig.add_subplot(222)
        hist_ax = fig.add_subplot(212)
        mu_ax.plot(tracker['mean'])
        mu_ax.set_title('Mean track')
        std_ax.plot(tracker['std'])
        std_ax.set_title('Std track')
        hist_ax.plot(hist)
        hist_ax.set_title('Negative ELBO track')

        if filename is None:
            plt.show()
        else:
            plt.savefig(path+filename, dpi=100)
        plt.clf()
        plt.close()


class CausalityFacadeAssociation:

    def __init__(self, confirmed_links, progressbar=False, ir_asm_prior=PriorFacade(),
                 hyper_params=ParametersFacade()):
        """Performing Posterior Traceability from Observational D.ata
        @ir_asm_prior: prior information of LTR
        @hyper_params: hyperparameters of inference techniques
        @confirmed_links: numpy vector of binary information about link obvs.
        """
        self.__compute_posteriors = PosteriorTraceability(
            #trials,
            #succ_links,
            ir_asm_prior=ir_asm_prior.get_asm_prior(),
            progressbar=progressbar,
            variational_params=hyper_params.get_vi_params(),
            sampling_params=hyper_params.get_sampling_params()
        )

        self.__ir_asm_prior = ir_asm_prior
        self.__hyper_params = hyper_params

        self.__confirmed_links = confirmed_links

        self.__latent_trace_model_1 = LatentTraceabilityRecovery()
        self.__latent_trace_model_2 = LatentTraceabilityRecovery()
        self.__latent_trace_model_3 = LatentTraceabilityRecovery()
        self.__latent_trace_model_4 = LatentTraceabilityRecovery()
        self.__latent_trace_model_0 = LatentTraceabilityRecovery()
        pass

    def apply_latent_traceability_recovery(
            self,
            ass_complexity=1,
            num_samples=5000,
            inference_key='a',

            execution_traces=None,
            transitive_belief=0.5,

            transitive_req_list=None,
            sim_req=None
    ):
        if execution_traces is None:
            execution_traces = []

        if ass_complexity == 1:
            self.__apply_association_model_1(num_samples=num_samples, inference_key=inference_key)
        elif ass_complexity == 2:
            self.__apply_association_model_2(num_samples=num_samples, inference_key=inference_key)
        elif ass_complexity == 3:
            if not execution_traces:
                print("List of execution_traces is empty :(")
            self.__apply_association_model_3(execution_traces = execution_traces, inference_key=inference_key,
                                             num_samples=num_samples, transitive_belief=transitive_belief)
        elif ass_complexity == 4:
            self.__apply_transitive_req2re2(transitive_req_list=transitive_req_list, sim_req=sim_req, inference_key=inference_key,
                                            num_samples=num_samples, transitive_belief=transitive_belief)
        elif ass_complexity == 0:
            self.__apply_holistic_association_model(execution_traces=execution_traces, transitive_req_list=transitive_req_list,
                                                    sim_req_list=sim_req, inference_key=inference_key,
                                                    num_samples=num_samples, transitive_belief=transitive_belief)
        else:
            self.__apply_association_model_1(num_samples=num_samples, inference_key=inference_key)
        pass

    def __apply_association_model_1(self, num_samples, inference_key):
        """Performing inference from empirical priors
        @num_samples: number of samples to be generated (default 10000)
        @inference_key: Type of Inference Method (default b) 'a'=NUTS 'b'=VI 'c'=MAP
        """
        try:
            ltr = self.__compute_posteriors.association_model_1(
                inference_key = inference_key,
                num_samples = num_samples,
                confirmed_links=self.__confirmed_links
            )
            self.__latent_trace_model_1.set_ltr(probabilistic_model=ltr)
        except:
            print("Latent Traceability Recovery Model Error [Complexity 1]:", sys.exc_info()[0])
        pass

    def __apply_association_model_2(self, num_samples, inference_key):
        """Performing inference from empirical priors and domain knowledge
        @num_samples: number of samples to be generated (default 10000)
        @domain_conf: probability that two artifacts are liked according to the domain expert
        @d_penalty: proportion of the penalty for linkage
        """
        #try:
        ltr = self.__compute_posteriors.association_model_2(
                inference_key = inference_key,
                num_samples = num_samples,
                confirmed_links=self.__confirmed_links
            )
        self.__latent_trace_model_2.set_ltr(probabilistic_model=ltr)
        #except:
        #    print("Latent Traceability Recovery Model Error [Complexity 2]:", sys.exc_info()[0])
        #pass

    def __apply_association_model_3(self, execution_traces, inference_key, num_samples,
                                    transitive_belief):
        """Performing inference from execution traces
        @num_samples: number of samples to be generated (default 10000)
        @execution_belief: a belief value of how much the user trusts execution traces
        @execution_traces: a list of tuples [(mu, sd)] of complementary links
        """
        try:
            ltr = self.__compute_posteriors.association_model_3(
                inference_key=inference_key,
                execution_traces=execution_traces,
                num_samples=num_samples,
                transitive_belief=transitive_belief,
                confirmed_links=self.__confirmed_links
            )
            self.__latent_trace_model_3.set_ltr(probabilistic_model=ltr)
        except:
            print("Latent Traceability Recovery Model Error [Complexity 3]:", sys.exc_info()[0])
        pass

    def __apply_transitive_req2re2(self, transitive_req_list, sim_req, inference_key, num_samples,
                                   transitive_belief):
        """Performing inference from execution traces
        @num_samples: number of samples to be generated (default 10000)
        @execution_belief: a belief value of how much the user trusts execution traces
        @execution_traces: a list of tuples [(mu, sd)] of complementary links
        """
        try:
            ltr = self.__compute_posteriors.association_model_transitive_req(
                inference_key = inference_key,
                transitive_req_list = transitive_req_list,
                num_samples = num_samples,
                sim_req_list= sim_req,
                transitive_belief = transitive_belief,
                confirmed_links=self.__confirmed_links
            )
            self.__latent_trace_model_4 .set_ltr(probabilistic_model=ltr)
        except:
            print("Latent Traceability Recovery Model Error Transitive Link[Complexity 4]:", sys.exc_info()[0])
        pass

    def __apply_holistic_association_model(self,
                                           execution_traces,
                                           transitive_req_list,
                                           sim_req_list,
                                           inference_key,
                                           num_samples,
                                           transitive_belief
                                           ):
        """Performing inference in a holistic model
        @num_samples: number of samples to be generated (default 10000)
        @execution_belief: a belief value of how much the user trusts execution traces
        """
        try:
            ltr = self.__compute_posteriors.association_model_holistic(
                inference_key=inference_key,
                num_samples=num_samples,
                transitive_belief=transitive_belief,
                confirmed_links=self.__confirmed_links,
                execution_traces=execution_traces,
                transitive_req_list=transitive_req_list,
                sim_req_list=sim_req_list
            )

            self.__latent_trace_model_0.set_ltr(probabilistic_model=ltr)
        except:
            print("Latent Traceability Recovery Model Error [Complexity 0]:", sys.exc_info()[0])
        pass

    # Plausability Getters (does not support map)
    def getPlausibleValues_ltr(self, complexity):
        return self.get_ltr(complexity).get_expected_linkage_value()

    def getPlausibleValues_ltr_1(self):
        return self.__latent_trace_model_1.get_ltr().get_expected_linkage_value()

    def getPlausibleValues_ltr_2(self):
        return self.__latent_trace_model_2.get_ltr().get_expected_linkage_value()

    def getPlausibleValues_ltr_3(self):
        return self.__latent_trace_model_3.get_ltr().get_expected_linkage_value()

    def getPlausibleValues_transitive_req2req_ltr(self):
        return self.__latent_trace_model_4.get_ltr().get_expected_linkage_value()

    def getPlausibleValues_holistic_ltr(self):
        return self.__latent_trace_model_0.get_ltr().get_expected_linkage_value()

    # Probabilistic Model Getters
    def get_ltr(self, complexity):
        if complexity == 1:
            return self.get_ltr_1()
        elif complexity == 2:
            return self.get_ltr_2()
        elif complexity == 3:
            return self.get_ltr_3()
        elif complexity == 4:
            return self.get_ltr_transitive_req2req()
        elif complexity == 0:
            return self.get_ltr_holistic()

    def get_ltr_1(self):
        return self.__latent_trace_model_1

    def get_ltr_2(self):
        return self.__latent_trace_model_2

    def get_ltr_3(self):
        return self.__latent_trace_model_3

    def get_ltr_transitive_req2req(self):
        return self.__latent_trace_model_4

    def get_ltr_holistic(self):
        return self.__latent_trace_model_0


class CausalityFacadeIntervention:

    def __init__(self, obs_data, var_types, treatment, outcome, confounders):
        """@obs_data: a Pandas DataFrame with the observational data as stochastic variables
            # load the data into a dataframe:
                X = pd.DataFrame({'x1' : x1, 'x2' : x2, 'x3' : x3, 'x4' : x4, 'x5' : x5})
            @var_types: data type of stochastic variables provided
            # define the variable types: 'c' is 'continuous'.  The variables defined here
            # are the ones the search is performed over  -- NOT all the variables defined
            # in the data frame.
                variable_types = {'x1' : 'c', 'x2' : 'c', 'x3' : 'c', 'x4' : 'c', 'x5' : 'c'}
            @treatment: this is a numpy of data distribution for X treament
            @outcome: this is a numpy of distribution for Y outcomes
            @confounders: this is a n-tuple composed of numpy arrays for the confounders
        """
        self.__learningGraphModel = CausalGraph(obs_data, var_types)
        self.__interventionModel = CausalFrame(treatment, outcome, confounders)
        pass

    def __interventionEffect(self, listNodes, listEdges, intervention, node_x, node_y):
        self.__learningGraphModel.build_directed_causal_graph(listNodes, listEdges)
        return self.__learningGraphModel.do_intervention_non_parametric(node_x, node_y, intervention)

    def graphOperations(self, listNodes, listEdges):
        self.__learn_graph = self._learningGraphModel.get_causal_graph()
        #TODO I don not know whats going on here
        #self.__effect_pdf = self.__interventionEffect(listNodes, listEdges, intervention, node_x, node_y)
        pass

    def frameOperations(self, confounders, types):
        self.__do_expected_value_non_p = self._interventionModel.do_expected_value_non_parametric(confounders, types)

    # Getters
    def getLearntGraphFromData(self):
        return self.__learn_graph

    def getInterventionEffect(self):
        return self.__effect_pdf

    def getFrameExpectedValueNonP(self):
        return self.__do_expected_value_non_p

    def getPlotCategoricalExpectationNonP(self, confounders, types):
        return self.__interventionModel.print_expectation_categorial(confounders, types)

    # Setters
    def setInterventionEffect(self, listNodes, listEdges, intervention, node_x, node_y):
        self.__interventionEffect(listNodes, listEdges, intervention, node_x, node_y)
        pass
