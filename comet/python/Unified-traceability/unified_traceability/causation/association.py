"""
Version 1.4
@danaderp July-2018
Association file is a set of methods and models that compute posterior probabilities and inferences
for the traceability problem.
It represents the first level of Causation (The ladder of Causation by Pearl)
"""


import numpy as np
from scipy.stats import beta
from theano import shared
from pymc3.variational.callbacks import CheckParametersConvergence
from enum import Enum, auto
from pymc3 import sample_ppc
import pymc3 as pm
import logging
import pickle


class StochasticComponent:
    LINKAGE = "linkage"
    OBSERVATION = "obs"
    DOMAIN = "domain_knowledge"
    CONFIDENCE = "confidence"
    HYPERMEAN = "hyperprior_mean"
    HYPERPRECISION = "hyperprior_precision"
    HYPERMEANDOMAIN = "hyperprior_mean_domain"
    HYPERPRECISIONDOMAIN = "hyperprior_precision_domain"
    ALPHA = 'alpha'
    BETA = 'beta'
    HYPERALPHA = 'hyper_alpha'
    HYPERBETA = 'hyper_beta'
    HYPERALPHADOMAIN = 'hyper_alpha_domain'
    HYPERBETADOMAIN = 'hyper_beta_domain'
    pass


class PriorChoice(Enum):
    """Following suggested Prior Choices Recommendations from
    https://github.com/stan-dev/stan/wiki/Prior-Choice-Recommendations
    https://arxiv.org/pdf/1403.4630.pdf
    http://www.stats.org.uk/priors/noninformative/YangBerger1998.pdf
    """
    FLAT_PRIOR = auto()
    SUPER_VAGUE = auto()  # normal(0, 1e6)
    WEAKLY_INFORMATIVE = auto()  # normal(0, 10)
    GENERIC_WEAKLY_INFORMATIVE = auto()  # normal(0, 1)
    SPECIFIC_INFORMATIVE = auto()  # Empirical IR treatment
    NON_INFORMATIVE = auto()  # Jeffrey's Prior
    WEAKLY_INFORMATIVE_BL = auto()  # Blackmond Laskey Suggestion for Beta Distribution
    pass


class AssociationPrior:

    def __init__(self,
                 prior_linkage,
                 prior_domain,
                 domain_conf,
                 d_penalty,
                 empirical_sim=[]
                 ):
        """ Definition of Association Model Priors
        @empirical_sim: a vector with similarity values from empirical configurations
        @prior_choice: the type of Prior in the model
        """
        self.__prior_choice = prior_linkage
        self.__empirical_sim = empirical_sim
        self.__domain_conf = domain_conf
        self.__prior_domain = prior_domain
        self.__d_penalty = d_penalty
        pass

    #Priors Choices

    def get_prior_linkage(self) -> PriorChoice:
        return self.__prior_choice

    def get_prior_domain(self) -> PriorChoice:
        return self.__prior_domain

    def getEmpiricalSim(self):
        return self.__empirical_sim

    def get_domain_conf(self):
        return self.__domain_conf

    def get_d_penalty(self):
        return self.__d_penalty

    pass


class PriorModel:

    def __init__(self, model, a, b, mean, var):
        """"""
        self.__asm = (model, a, b, mean, var)
        pass

    def get_params(self):
        return self.__asm

    def get_linkage_mean(self):
        return self.__asm[3]

    def get_linkage_var(self):
        return self.__asm[4]

    pass


class SamplingParameters:

    def __init__(self, max_treedepth=15, target_accept=0.92, tune=1000, cores=2, chains=4):
        """
        @max_treedepth: The maximum depth of the trajectory tree """
        self.__max_treedepth = max_treedepth
        self.__target_accept = target_accept
        self.__tune = tune
        self.__cores = cores
        self.__chains = chains
        pass

    def get_max_treedepth(self):
        return self.__max_treedepth

    def set_max_treedepth(self, max_treedepth):
        self.__max_treedepth = max_treedepth

    def get_target_accept(self):
        return self.__target_accept

    def set_target_accept(self, target_accept):
        self.__target_accept = target_accept

    def get_tune(self):
        return self.__tune

    def set_tune(self, tune):
        self.__tune = tune

    def get_cores(self):
        return self.__cores

    def set_cores(self, cores):
        self.__cores = cores

    def get_chains(self):
        return self.__chains

    def set_chains(self, chains):
        self.__chains = chains


class VariationalParameters:

    def __init__(self, learning_rate=2e-4, itera=100000):
        """ Parameters for Variational Inference"""
        self.__learning_rate = learning_rate
        self.__iter = itera
        pass

    def get_learning_rate(self):
        return self.__learning_rate

    def set_learning_rate(self, learning_rate):
        self.__learning_rate = learning_rate

    def get_iter(self):
        return self.__iter

    def set_iter(self, itera):
        self.__iter = itera


class ProbabilisticModel:

    def __init__(self, model=None, traces=None,
                 num_samples=None, vi_approx=None, vi_tracker=None,
                 map=None
                 ):
        self.__model = model
        self.__traces = traces
        self.__num_samples = num_samples

        self.__vi_approx = vi_approx
        self.__vi_tracker = vi_tracker

        self.__map = map


    def get_pymc3_model(self):
        return self.__model

    def set_pymc3_model(self, model):
        self.__model = model

    def get_pymc3_traces(self):
        return self.__traces

    def set_pymc3_traces(self, traces):
        self.__traces = traces

    def get_num_samples(self):
        return self.__num_samples

    def set_num_samples(self, num_samples):
        self.__num_samples = num_samples


    #Inference Summary
    def get_map(self):
        """Map Analysis"""
        return self.__map

    def get_summary(self, sub_sampling):
        tr = self.__traces[sub_sampling:]
        return print(pm.summary(tr))

    def get_trace_plot(self, sub_sampling):
        tr = self.__traces[sub_sampling:]
        return pm.traceplot(tr)

    def get_vi_hist(self):
        return self.__vi_approx.hist

    def get_vi_tracker(self):
        return self.__vi_tracker

    #Expected Value
    #TODO do we really need the mean and dev for other variables apart from Linkage?
    def get_expected_linkage_value(self):
        if self.__traces is not None:
            return np.mean(self.__traces[StochasticComponent.LINKAGE])
        else:
            print("Traces does not exist")
            return 0.5

    def get_standard_dev_linkage_value(self):
        if self.__traces is not None:
            return np.std(self.__traces[StochasticComponent.LINKAGE])
        else:
            return 0.5

    def get_map_linkage_value(self):
        if self.__map is not None:
            return self.__map[StochasticComponent.LINKAGE]
        else:
            return 0.5

    # Evaluation Measure
    def get_gelman_rubin(self, sub_sampling):
        tr = self.__traces[sub_sampling:]
        return pm.gelman_rubin(tr)

    # Persistance
    def get_dataframe(self):
        return pm.backends.tracetab.trace_to_dataframe(self.__traces)

    def save_a_trace(self, file):
        with open(file,'wb') as buff:
            pickle.dump(
                {'model': self.__model,
                 'trace': self.__traces,
                 'num_samples': self.__num_samples,
                 'vi_approx': self.__vi_approx,
                 'vi_tracker': self.__vi_tracker,
                 'map': self.__map
                 },
                buff
            )
        pass

    def load_a_trace(self, file):
        with open(file,'rb') as buff:
            data = pickle.load(buff)
        self.__model, self.__traces, self.__num_samples, self.__vi_approx, self.__vi_tracker, self.__map = \
            data['model'], data['trace'], data['num_samples'], data['vi_approx'], data['vi_tracker'], data['map']

    # Predicton of Links
    def predict_links_by_confidence(self, sub_sampling):
        #Updating values and do prediction
        ppc = pm.sample_ppc(trace=self.__traces, model=self.__model, samples=sub_sampling)
        return ppc[StochasticComponent.OBSERVATION]



class PosteriorTraceability:
    """A class to compute Posterior Probabilities given observational data
    Information Retrival Prior is mandatory in the constructor
    Other Priors are configured in specific Association Models
    """

    def __init__(self,
                 #trials,
                 #succ_links,
                 ir_asm_prior,
                 progressbar=False,
                 variational_params=VariationalParameters(),
                 sampling_params=SamplingParameters()
                 ):
        """Performing Posterior Traceability from Observational Data

        each empirical configuration must provide only one value per link (mean, median, or summation)
        @trials: number of observed techniques applied
        @succ_links: number of successful observed links from trials
        @learning_rate:variational inference learning rate
        @iter: number of iterations in variational inference
        @empirical_sim: a vector with values from empirical configurations
        @prior_choice: the type of Prior in the model
        """
        # logitInv = lambda x: np.exp(x)/(1.0+np.exp(x)) #Sigmoid Function

        # Logger
        self.__progressbar = progressbar
        logger = logging.getLogger("pymc3")
        logger.propagate = self.__progressbar

        #self.__trials = trials  # Number of Trials or Observed Techniques
        #self.__succ_links = succ_links  # Successfull links from Observed Data

        # Sampling Inference
        self.__sampling_params = sampling_params

        # VI Inference
        self.__variational_params = variational_params

        # Priors
        self.__priors = ir_asm_prior

        # Prior Linkage Configuration
        if PriorChoice.SPECIFIC_INFORMATIVE == ir_asm_prior.get_prior_linkage():
            self.__init_linkage_specific_informative_prior(ir_asm_prior)
        elif PriorChoice.WEAKLY_INFORMATIVE_BL == ir_asm_prior.get_prior_linkage():
            self.__init_linkage_weakly_informative_bl_prior()
        else:  # Weakly Informative is the default prior
            self.__init_linkage_weakly_informative_bl_prior()
        pass

    def __init_linkage_weakly_informative_bl_prior(self):
        """Information Retrieval (or Latent Probability of Linkage) definition of priors with
        weakly informative approach
        More info
        -https://www.researchgate.net/post/What_priors_should_I_use_for_Beta_parameters
        -http://seor.vse.gmu.edu/~klaskey/SYST664/Bayes_Unit7.pdf
        """
        with pm.Model() as model:
            # HyperPriors
            U = pm.Uniform(StochasticComponent.HYPERMEAN, lower=0, upper=1)
            # Gamma(1,20) = Gamma(k,θ) = Gamma(shape,scale) = Gamma(mu,sd)
            V = pm.Gamma(StochasticComponent.HYPERPRECISION, mu=1, sd=20)  # Characterization using shape k and scale θ

            # Calculate Hyperparameters alpha and beta based on mu and nu
            alpha = pm.Deterministic(StochasticComponent.HYPERALPHA, U * V)
            beta = pm.Deterministic(StochasticComponent.HYPERBETA, (1. - U) * V)

        link_model = PriorModel(model, alpha, beta, U, V)
        self.__linkage_model = link_model
        pass

    def __init_linkage_specific_informative_prior(self, asm_prior):
        """Accounting for empirical similarities from Information Retrieval,
        however, we are able to accept any other info as empirical prior"""
        # link_ratios = list(map(logitInv,s)) #History of Ratios of One Link
        (f_alpha, f_beta, f_mean, f_var) = self.__fitting_beta_ratios(
            asm_prior.getEmpiricalSim())  # Activating Fitting Beta

        with pm.Model() as model:
            pass
            # alpha = pm.Normal(StochasticComponent.HYPERALPHA, mu=f_alpha, sd=0.01)
            # beta = pm.Normal(StochasticComponent.HYPERBETA, mu=f_beta, sd=0.01)

        link_model = PriorModel(model, f_alpha, f_beta, f_mean, f_var)
        self.__linkage_model = link_model
        pass

    def __fitting_beta_ratios(self, link_ratios):
        """Fitting a beta distribution to the rates"""
        prior_parameters = beta.fit(link_ratios, floc=0, fscale=1)  # extract a,b from fit
        prior_a, prior_b = prior_parameters[0:2]
        # Mean and Dispersion of Linkage
        mean, var, skew, kurt = beta.stats(prior_a, prior_b, moments='mvsk')
        return prior_a, prior_b, mean, var

    def __inference_vi_empirical(self, num_samples, model):

        with model:
            # Inference
            advi = pm.ADVI()

            tracker = pm.callbacks.Tracker(
                mean=advi.approx.mean.eval,  # callable that returns mean
                std=advi.approx.std.eval  # callable that returns std
            )
            approx = advi.fit(
                n=self.__variational_params.get_iter(),
                progressbar=self.__progressbar,
                # method = 'advi',
                # model = model,
                obj_optimizer=pm.adagrad_window(learning_rate=self.__variational_params.get_learning_rate()),
                callbacks=[tracker]
                # total_grad_norm_constraint=10
            )
            tracevar = approx.sample(num_samples)

        # print("PASO POR VI")
        #Probabilistic model generation
        prob_model = ProbabilisticModel(model=model, traces=tracevar,
                                        num_samples=num_samples, vi_approx=approx, vi_tracker=tracker)
        return prob_model

    def __inference_nuts_empirical(self, num_samples, model):
        with model:
            # Inference
            step = pm.NUTS(
                max_treedepth=self.__sampling_params.get_max_treedepth()
            )  # Sampling with MCMC
            trace = pm.sample(num_samples,
                              step,
                              progressbar=self.__progressbar,
                              target_accept=self.__sampling_params.get_target_accept(),
                              tune=self.__sampling_params.get_tune(),
                              cores=self.__sampling_params.get_cores(),
                              nchains=self.__sampling_params.get_chains()
                              )
        # print("PASO POR NUTS")

        #Probabilistic model generation
        prob_model = ProbabilisticModel(model=model, traces=trace, num_samples=num_samples)
        return prob_model

    def __inference_map_empirical(self, model):
        with model:
            # Inference
            start = pm.find_MAP()  # Find good starting values for the sampling algorithm
            #self.__map = start
        # print("Paso por MAP")
        prob_model = ProbabilisticModel(model=model, map=start)
        return prob_model

    def __choice_inference(self, x, num_samples, model):
        if x == 'a':
            return self.__inference_nuts_empirical(num_samples, model)
        elif x == 'b':
            return self.__inference_vi_empirical(num_samples, model)
        elif x == 'c':
            return self.__inference_map_empirical(model)
        else:
            return self.__inference_map_empirical(model)

    def association_model_1(self, inference_key, num_samples, confirmed_links):
        """Performing inference from empirical priors
        @num_samples: number of samples to be generated (default 10000)
        """
        (linkage_model, alpha, beta, mean, var) = self.__linkage_model.get_params()

        with linkage_model:
            # Prior
            theta_prior = pm.Beta(
                StochasticComponent.LINKAGE,
                alpha,
                beta
            )
            # Likelihood
            observations = pm.Bernoulli(StochasticComponent.OBSERVATION,theta_prior,observed=confirmed_links)

        # Assuming that the likelihood is Binomial, then the prior should be a Beta
        inference = self.__choice_inference(inference_key, num_samples, linkage_model)
        return inference

    def __prior_domain_knowledge_weakly_inf_bl(self,DomainKnowledgeModel):
        with DomainKnowledgeModel:
            # HyperPriors
            U = pm.Uniform(StochasticComponent.HYPERMEANDOMAIN, lower=0, upper=1)
            # Gamma(1,20) = Gamma(k,θ) = Gamma(shape,scale) = Gamma(mu,sd)
            V = pm.Gamma(StochasticComponent.HYPERPRECISIONDOMAIN, mu=1, sd=20)

            # Calculate Hyperparameters alpha and beta based on mu and nu
            alpha = pm.Deterministic(StochasticComponent.HYPERALPHADOMAIN, U * V)
            beta = pm.Deterministic(StochasticComponent.HYPERBETADOMAIN, (1. - U) * V)

            #Computing Confidence
            confidence = pm.Beta(StochasticComponent.CONFIDENCE, alpha=alpha, beta=beta)

        return DomainKnowledgeModel, confidence

    def __prior_domain_knowledge_specific_inf(self,DomainKnowledgeModel):
        """This Prior can be Modified"""
        self.__confidence_shared = shared( self.get_priors().get_domain_conf() ) #<-- Shared Domain Knowledge to make new predictions
        with DomainKnowledgeModel:
            # Domain-Knowledge
            confidence = pm.Beta(StochasticComponent.CONFIDENCE, mu=self.__confidence_shared, sd=0.01)

        return DomainKnowledgeModel, confidence

    def __exogenous_domain_knowledge(self, linkage_mean, DomainKnowledgeModel):
        """Probability distribution that represents the exogenous causes of Domain Knowledge"""
        # TODO in this version we do not account for several domain knowledge sources (like two dvlops)

        # Prior Domain Configuration
        domain_prior = self.get_priors().get_prior_domain()
        if PriorChoice.SPECIFIC_INFORMATIVE == domain_prior:
            priors = self.__prior_domain_knowledge_specific_inf(DomainKnowledgeModel)
        elif PriorChoice.WEAKLY_INFORMATIVE_BL == domain_prior:
            priors = self.__prior_domain_knowledge_weakly_inf_bl(DomainKnowledgeModel)
        else:  # Weakly Informative is the default prior
            priors = self.__prior_domain_knowledge_weakly_inf_bl(DomainKnowledgeModel)

        (model, confidence) = priors

        with model:
            # Domain-Knowledge
            domain = pm.Bernoulli(StochasticComponent.DOMAIN, p=confidence)
            penalty = linkage_mean * self.get_priors().get_d_penalty() * (domain - 1)
            reward = (1 - linkage_mean) * self.get_priors().get_d_penalty() * domain

        return model, penalty, reward

    def association_model_2(self, inference_key, num_samples, confirmed_links):
        """Domain Knowledge Component as Causal Var for Linkage Probability
        @domain_conf: probability that two artifacts are liked according to the domain expert
        @d_penalty: proportion of the penalty for linkage
        """

        (linkage_model, alpha, beta, linkage_mean, linkage_var) = self.__linkage_model.get_params()

        (DomainKnowledgeModel, penalty, reward) = self.__exogenous_domain_knowledge(linkage_mean, linkage_model)

        with DomainKnowledgeModel:
            # Uniform priors on the mean and the variance of the Beta distributions

            mu_new = pm.Normal("mu_new", mu=linkage_mean + penalty + reward, sd=0.01)
            nu = linkage_var

            # Calculate Hyperparameters alpha and beta based on mu and nu
            alpha = pm.Deterministic(StochasticComponent.ALPHA, mu_new / (nu * nu))
            beta = pm.Deterministic(StochasticComponent.BETA, (1. - mu_new) / (nu * nu))
            # Priors
            theta = pm.Beta(StochasticComponent.LINKAGE, alpha, beta)
            # Data likelihood
            observations = pm.Bernoulli(StochasticComponent.OBSERVATION, theta, observed=confirmed_links)
        # 2 Inference
        inference = self.__choice_inference(inference_key, num_samples, DomainKnowledgeModel)
        return inference

    def __exogenous_execution_traces(self, execution_traces, model):
        """Probability distribution that represents the exogenous causes of Execution Traces"""
        # TODO, execution traces can be extended beyond Source Code -> Test Cases
        # TODO, priors are not configured (hardcoded)
        # TODO check on execution_traces elements, they must not include a None element!
        ncomp = len(execution_traces)

        # Is it a Mixture Model?
        if ncomp == 1:
            with model:
                mix_beta = pm.Beta('mix_beta', mu=execution_traces[0][0], sd=execution_traces[0][1])
        else:
            with model:
                # Mixture Params
                w = pm.Dirichlet('w', np.ones(ncomp))
                comp_dist = [pm.Beta.dist(mu=execution_traces[i][0], sd=execution_traces[i][1])
                             for i in range(ncomp)]
                mix_beta = pm.Mixture('mix_beta', w=w, comp_dists=comp_dist)
        return model, mix_beta

    def association_model_3(self, inference_key, execution_traces, num_samples, transitive_belief, confirmed_links):
        """Execution Traces Component affecting the T. Linkage Probability
        @execution_traces: a list of pairs (mu,sd) from complementary links
        """

        (linkage_model, alpha, beta, linkage_mean, linkage_var) = self.__linkage_model.get_params()

        (ExecutionTracesModel, mix_beta) = self.__exogenous_execution_traces(
            execution_traces, linkage_model
        )

        with ExecutionTracesModel:
            # Theta Linkage Prior
            mu = transitive_belief * mix_beta + (1 - transitive_belief) * linkage_mean
            nu = linkage_var

            mu_new = pm.Normal("mu_new", mu=mu, sd=0.01)

            #alpha = pm.Deterministic(StochasticComponent.ALPHA, mu_new / (nu * nu))
            #beta = pm.Deterministic(StochasticComponent.BETA, (1. - mu_new) / (nu * nu))

            #theta_prior = pm.Beta(StochasticComponent.LINKAGE, alpha, beta)
            theta_prior = pm.Beta(StochasticComponent.LINKAGE, mu=mu_new, sd=nu)
            # Likelihood
            observations = pm.Bernoulli(StochasticComponent.OBSERVATION,theta_prior,observed=confirmed_links)
        # 2 Inference
        inference = self.__choice_inference(inference_key, num_samples, ExecutionTracesModel)
        return inference

    def __exogenous_transitive_req(self, transitive_req_list, sim_req_list, model):
        ncomp_trans = len(transitive_req_list)
        ncom_sim = len(sim_req_list)

        #print("Transitive Dimension SIm: " + str(ncom_sim))

        if ncom_sim == 1:
            with model:
                #V = pm.Gamma('V', mu=1, sd=20)  # Characterization using shape k and scale θ
                mix_normal_sim = pm.Normal('mix_normal_sim', mu=sim_req_list[0], sd=0.01)
        else:
            with model:
                # Mixture Params for transitive
                w_s = pm.Dirichlet('w_s', np.ones(ncom_sim))
                sim_req_beta = [pm.Normal.dist(mu=sim_req_list[i], sd=0.01) for i in range(ncom_sim)]
                mix_normal_sim = pm.Mixture('mix_normal_sim', w=w_s, comp_dists=sim_req_beta)

        if ncomp_trans == 1:
            with model:
                mix_beta_transitive = pm.Beta('mix_beta_trans_req', mu=transitive_req_list[0][0],
                                              sd=transitive_req_list[0][1])
        else:
            with model:
                # Mixture Params
                w_t = pm.Dirichlet('w_t', np.ones(ncomp_trans))  #Plus the sim_req
                comp_dist = [pm.Beta.dist(mu=transitive_req_list[i][0], sd=transitive_req_list[i][1])
                             for i in range(ncomp_trans)]
                mix_beta_transitive = pm.Mixture('mix_beta_trans_req', w=w_t, comp_dists=comp_dist)

        with model:
            #Unique support from transitive links
            mix_beta = pm.Deterministic('mixture_transitive', 0.7*mix_beta_transitive + 0.3*mix_normal_sim)

        return model, mix_beta

    def association_model_transitive_req(self, inference_key, transitive_req_list, sim_req_list,
                                         num_samples, transitive_belief, confirmed_links):
        """ Transitive Model of Requirements
        @transitive_req_list: a list of pairs (mu,sd) from transitive links
        @sim_req_list:
        """

        (linkage_model, alpha, beta, linkage_mean, linkage_var) = self.__linkage_model.get_params()

        (TransitiveModel, mix_beta) = self.__exogenous_transitive_req(
            transitive_req_list=transitive_req_list, sim_req_list=sim_req_list, model=linkage_model
        )

        with TransitiveModel:

            # Theta Linkage Prior
            mu = transitive_belief * mix_beta + (1 - transitive_belief) * linkage_mean
            nu = linkage_var

            mu_new = pm.Normal("mu_new", mu=mu, sd=0.01)

            #alpha = pm.Deterministic(StochasticComponent.ALPHA, mu_new / (nu * nu))
            #beta = pm.Deterministic(StochasticComponent.BETA, (1. - mu_new) / (nu * nu))

            #theta_prior = pm.Beta(StochasticComponent.LINKAGE, alpha, beta)
            theta_prior = pm.Beta(StochasticComponent.LINKAGE, mu=mu_new, sd=nu)
            # Likelihood
            observations = pm.Bernoulli(StochasticComponent.OBSERVATION, theta_prior, observed=confirmed_links)

        # 2 Inference
        inference = self.__choice_inference(inference_key, num_samples, TransitiveModel)
        return inference

    def association_model_holistic(self,
                                   inference_key,
                                   num_samples,
                                   transitive_belief,
                                   confirmed_links,
                                   execution_traces = None,
                                   transitive_req_list = None,
                                   sim_req_list = None
                                   ):

        #print("-->Registered Domain: " + str(self.get_priors().get_domain_conf()))
        (linkage_model, alpha, beta, linkage_mean, linkage_var) = self.__linkage_model.get_params()

        if execution_traces is not None:
            print('execution traces in holistic')
            (HolisticModel, mix_beta) = self.__exogenous_execution_traces(execution_traces, linkage_model)
        elif transitive_req_list is not None and sim_req_list is not None:
            print('transitive traces in holistic')
            (HolisticModel, mix_beta) = self.__exogenous_transitive_req(
                transitive_req_list=transitive_req_list, sim_req_list=sim_req_list, model=linkage_model)
        else:
            print('Houston, we have a problem in the transitive links')
            HolisticModel = linkage_model #Dafault model
            mix_beta = linkage_mean #Defautl mixing parameter

        with HolisticModel:
            execution_mu = transitive_belief * mix_beta + (1 - transitive_belief) * linkage_mean

        (HolisticModel, penalty, reward) = self.__exogenous_domain_knowledge(execution_mu, HolisticModel)
        with HolisticModel:

            #penalty = execution_mu * d_penalty * (domain - 1)
            #reward = (1 - execution_mu) * d_penalty * domain

            mu = execution_mu + penalty + reward
            nu = linkage_var

            mu_new = pm.Normal("mu_new", mu=mu, sd=0.01)

            alpha = pm.Deterministic(StochasticComponent.ALPHA, mu_new / (nu * nu))
            beta = pm.Deterministic(StochasticComponent.BETA, (1. - mu_new) / (nu * nu))

            theta_prior = pm.Beta(StochasticComponent.LINKAGE, alpha, beta)
            # Likelihood
            #observations = pm.Binomial(StochasticComponent.OBSERVATION,
            #                           n=self.__trials, p=theta_prior, observed=self.__succ_links)
            observations = pm.Bernoulli(StochasticComponent.OBSERVATION,theta_prior,observed=confirmed_links)

        # 2 Inference
        inference = self.__choice_inference(inference_key, num_samples, HolisticModel)
        return inference

    # Inference Configuration
    def get_sampling_param(self):
        return self.__sampling_params

    def set_sampling_param(self, sampling_param):
        self.__sampling_params = sampling_param

    # Latent Prior Linkage Model
    def get_linkage_model(self):
        return self.__linkage_model

    def get_priors(self) -> AssociationPrior:
        return self.__priors

    #Special Prediction
    #TODO I do not know how to migrate the prediction
    # to the probabilitic model since domain knowledge has a shared variable :(
    def predict_links_by_confidence(self, trace, samples, confidence, model):
        #changing values of Domain Knowledge Confidence
        self.__confidence_shared.set_value(confidence)

        #Updating values and do prediction
        ppc = pm.sample_ppc(trace=trace, model=model, samples=samples)
        #print("size ppc: "+str(len(ppc[StochasticComponent.OBSERVATION])))
        return ppc[StochasticComponent.OBSERVATION]


    pass
