import numpy as np
import matplotlib.pyplot as plt
import subprocess
import csv

def discretize_and_assign_probabilities(a, b, n, plot=False):
    # Step size
    step_size = (b - a) / n
    mu = (a+b)/2
    sigma=(b-a)/6
    # Discretize the interval [a, b] into n points
    bins = np.linspace(a, b, n)
    
    # Compute the normal distribution probabilities for each bin
    probabilities = (1 / (sigma * np.sqrt(2 * np.pi))) * np.exp(-0.5 * ((bins - mu) / sigma)**2)
    
    # Normalize the probabilities so they sum to 1
    probabilities /= probabilities.sum()

    if plot:
        plt.figure(figsize=(10, 6))

        # Plot histogram of expected values with the corresponding probabilities as weights
        plt.hist(bins, weights=probabilities, alpha=0.7, color='blue', edgecolor='black')

        # Add labels and title
        plt.xlabel('Expected Value')
        plt.ylabel('Probability')
        plt.title('Histogram of Expected Values with Probabilities as Weights')
        plt.show()
    
    return bins, probabilities


def distr_over_para(param1, prob1, param2, prob2):
    distr = []
    for i in range(len(param1)):
        for j in range(len(param2)):
            comb = (param1[i], param2[j])
            probs = prob1[i]*prob2[j]
            distr.append([comb, probs])
    return distr


def run_prism_model(parametersN, parametersV, model_file, property_file,debug=False):
    # Construct PRISM command
    base_cmd = "~/prism/prism/bin/prism -javastack 100m -javamaxmem 14g "
    options = model_file + " " + property_file + " -prop 1 -const "
    for i in range(len(parametersN)):
        options += f"{parametersN[i]}={parametersV[i]}"
        if (i != len(parametersN) - 1):
            options += ","
    prism_command = base_cmd + options + " -v -ex -exportstrat stdout"
    result = subprocess.run(prism_command, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error running PRISM: {result.stderr}")
        return None
    for line in result.stdout.splitlines():
        if "Result:" in line:
            return float(line.split(":")[1].strip().split(" ")[0])
    return None
    #if debug:
    #    print(f"Running PRISM command: {prism_command}")
    
    # Execute PRISM command and capture output
    #result = subprocess.run(prism_command, shell=True, capture_output=True, text=True)
    
    # Check for successful completion
    #if result.returncode != 0:
    #    print(f"Error running PRISM: {result.stderr}")
    #    return None
    
    # Extract the expected value from PRISM's output (assuming the expected value is printed in the result)
    #for line in result.stdout.splitlines():
    #    if "Result:" in line:
    #        return float(line.split(":")[1].strip())  # Extract the result value

    #return None

def computed_expected_value(distr_list, model_file, property_file, debug=False):
    result = []
    for i in range(len(distr_list)):
        result.append(run_prism_model(["p", "p_prime"], distr_list[i][0], model_file, property_file))
    return result

def export_csv(distr_list, result, filename):
    data = []
    data.append(["p","p_prime","probability","expected value"])
    for i in range(len(distr_list)):
        data.append([distr_list[i][0][0],distr_list[i][0][1],distr_list[i][1],result[i]])
    probabilities = [row[2] for row in data[1:]]
    expected_values = [row[3] for row in data[1:]]

    # Create a histogram with expected values on the x-axis and probabilities on the y-axis
    plt.figure(figsize=(10, 6))

    # Plot histogram of expected values with the corresponding probabilities as weights
    plt.hist(expected_values, weights=probabilities, bins=10, alpha=0.7, color='blue', edgecolor='black')

    # Add labels and title
    plt.xlabel('Expected Value')
    plt.ylabel('Probability')
    plt.title('Histogram of Expected Values with Probabilities as Weights')
    #plt.savefig(filename)
    plt.show()
    return data


modelFile = "~/prism/prism/tests/transit_dtmc_example/transit.prism"
propertyFile = "~/prism/prism/tests/transit_dtmc_example/transit.props"
logFile = "~/prism/prism/tests/experiments/transit_dtmc_example/transit_dtmc_example.log"
saveFig = "~/prism/prism/tests/experiments/transit_dtmc_example/bothParam.png"
bins1, probs1 = discretize_and_assign_probabilities(0.4,0.8,6, plot=True)
bins2, probs2 = discretize_and_assign_probabilities(0.1,0.5,5, plot=True)
#bins2 = np.full_like(bins1, 0.2)
#probs2 = np.full_like(probs1, 1)
distrP = distr_over_para(bins1,probs1,bins2,probs2)
result= computed_expected_value(distrP, modelFile, propertyFile, True)
export_csv(distrP, result, saveFig)



