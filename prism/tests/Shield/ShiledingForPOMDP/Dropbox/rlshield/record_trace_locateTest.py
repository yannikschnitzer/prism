import pandas as pd 
trace = pd.read_table('trace1.txt')
for i in range(len(trace)):
	print(i, trace.loc[i,"state"], trace.loc[i,"beliefSupport"])
