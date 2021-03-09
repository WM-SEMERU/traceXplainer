'''
Daniel McCrystal
June 2018

'''

from .IR_Method import IR_Method

from statistics import mean, stdev

class Orthogonal_IR(IR_Method):

    def generate_model(self, model_A, model_B, parameters=None):
        """
        Computes the similarity values by combining two orthogonal IR methods.

        Arguments:
            parameters (dict of str:obj, optional): Optional parameter dictionary for
                computing orthogonal similarity. If no parameter dictionary is given, default
                values will be used. Below are the orghogonal specific parameters.

                'lambda' (str or list): lambda value to be used, or list of lambda
                    values, in which case a list of models will be returned

        Returns:
            Trace_Model: A new IR model containing the generated similarity values
        """

        print("Generating new Orthogonal model: " + model_A.get_name() + "+" + model_B.get_name())

        default_parameters = dict()
        default_parameters['lambda'] = 0.5
        if len(model_A._parameters) > 0:
            default_parameters[model_A.get_name()] = model_A._parameters
        if len(model_B._parameters) > 0:
            default_parameters[model_B.get_name()] = model_B._parameters

        if parameters is not None:
            for key in parameters:
                if key in default_parameters:
                    default_parameters[key] = parameters[key]
                else:
                    print("Ignoring unrecognized Orthogonal_IR parameter [" + str(key) + "]")
        parameters = default_parameters

        if model_A.get_corpus_name() != model_B.get_corpus_name():
            raise ValueError("Orghogonal IR methods must operate on the same corpus")

        all_A_vals = model_A.get_all_values()
        all_B_vals = model_B.get_all_values()

        mean_A = mean(all_A_vals)
        stdev_A = stdev(all_A_vals)

        mean_B = mean(all_B_vals)
        stdev_B = stdev(all_B_vals)

        lambda_ = parameters['lambda']

        source_names = model_A.get_source_names()
        target_names = model_A.get_target_names()

        if type(lambda_) is float:
            combined_model = self._new_model(model_A.get_name() + "+" + model_B.get_name(), parameters=parameters)
            for source, target in model_A.get_links():
                sim_A = (model_A.get_value(source, target) - mean_A) / stdev_A

                sim_B = (model_B.get_value(source, target) - mean_B) / stdev_B

                sim_combined = (lambda_ * sim_A) + ((1 - lambda_) * sim_B)

                combined_model.set_value(source, target, sim_combined)

            threshold_technique = self.get_optimal_threshold_technique(model_A, model_B, lambda_)

            if threshold_technique is not None:
                combined_model.set_default_threshold_technique(threshold_technique)
            print("Done generating orthogonal model: " + model_A.get_name() + "+" + model_B.get_name())
            return combined_model

        elif type(lambda_) is list:
            combined_models = []
            for lambda_val in lambda_:
                combined_model = self._new_model(model_A.get_name() + "+" + model_B.get_name())
                for source in source_names:
                    for target in target_names:
                        sim_A = (model_A.get_value(source, target) - mean_A) / stdev_A

                        sim_B = (model_B.get_value(source, target) - mean_B) / stdev_B

                        sim_combined = (lambda_val * sim_A) + ((1 - lambda_val) * sim_B)

                        combined_model.set_value(source, target, sim_combined)
                combined_models.append(combined_model)

            return combined_models

    def get_optimal_threshold_technique(self, model_A, model_B, lambda_):
        if lambda_ != 0.5:
            return None

        names = {model_A.get_name(), model_B.get_name()}
        if 'JS' in names:
            if 'LDA' in names:
                return 'link_est'
            elif 'NMF' in names:
                return 'link_est'
            elif 'VSM' in names:
                return 'min-max'
        elif 'VSM' in names:
            if 'LDA' in names:
                return 'link_est'
            elif 'NMF' in names:
                return 'link_est'
        else:
            return None
