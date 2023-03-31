import numpy as np
import pandas as pd
import biosppy.signals.ecg as ecg

# Load ECG data from CSV file
df = pd.read_csv('ecg_data.csv', header=None)

# Convert data to NumPy array
data = np.array(df)

# Extract R peaks using biosppy
out = ecg.ecg(signal=data[:, 1], sampling_rate=1000, show=False)

# Print QRS duration
print('QRS duration:', out['templates_ts'][0][-1] - out['templates_ts'][0][0])

# Extract additional features
ts, filtered, rpeaks, templates_ts, templates, heart_rate_ts, heart_rate = ecg.extract_heartbeats(signal=data[:, 1], rpeaks=out['rpeaks'], sampling_rate=1000)
print('Heart rate:', np.mean(heart_rate))
print('RR intervals:', np.diff(rpeaks))
