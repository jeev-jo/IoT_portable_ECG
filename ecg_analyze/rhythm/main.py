import wfdb
import pandas as pd

# Load the ECG data from the CSV file
ecg_data = pd.read_csv('ecg_data.csv')

# Get the sampling frequency and ECG signal from the data
fs = 360
ecg_signal = ecg_data['ECG'].values

# Apply a bandpass filter to the ECG signal to remove noise
lc = 100
hc = 600
filtered_signal = wfdb.processing.bandpass_filter(ecg_signal, lc, hc, fs)

# Apply the Pan-Tompkins QRS detection algorithm to detect R-peaks in the ECG signal
qrs_indices = wfdb.processing.qrs.detect_peaks(filtered_signal, fs)

# Calculate the RR intervals between consecutive R-peaks
rr_intervals = [t - s for s, t in zip(qrs_indices, qrs_indices[1:])]

# Calculate the mean RR interval and convert it to a heart rate in bpm
mean_rr = sum(rr_intervals) / len(rr_intervals)
heart_rate = 60 / mean_rr

# Determine the rhythm based on the heart rate
if heart_rate < 60:
    rhythm = 'Sinus bradycardia'
elif 60 <= heart_rate <= 100:
    rhythm = 'Normal sinus rhythm'
else:
    rhythm = 'Sinus tachycardia'

# Print the results
print('Heart rate:', round(heart_rate, 2), 'bpm')
print('Rhythm:', rhythm)
