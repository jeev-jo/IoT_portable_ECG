import pandas as pd

# Load ECG data from CSV file
ecg_data = pd.read_csv('ecg_data.csv')

# Extract lead values from ECG data
lead_I = ecg_data['I'].tolist()
lead_II = ecg_data['II'].tolist()
lead_III = ecg_data['III'].tolist()

# Define Modified Sgarbossa Criteria weights
m1_weight = 5
m2_weight = 2
m3_weight = 5

# Calculate Modified Sgarbossa Criteria scores for each lead
m1_score_I = m1_weight if lead_I[0] >= 1 else 0
m1_score_II = m1_weight if lead_II[0] <= -1 else 0
m1_score_III = m1_weight if lead_III[0] >= 1 else 0

m2_score_I = m2_weight if (lead_I[0] >= 1 and lead_III[0] <= -1) else 0
m2_score_II = m2_weight if (lead_II[0] >= 1 and lead_III[0] <= -1) else 0
m2_score_III = m2_weight if (lead_III[0] >= 1 and lead_I[0] <= -1) else 0

m3_score_I = m3_weight if (lead_I[0] <= -1 and abs(lead_I[0]) >= (abs(lead_II[0]) * 0.25)) else 0
m3_score_II = m3_weight if (lead_II[0] <= -1 and abs(lead_II[0]) >= (abs(lead_I[0]) * 0.25)) else 0
m3_score_III = m3_weight if (lead_III[0] >= 1 and abs(lead_III[0]) >= (abs(lead_II[0]) * 0.25)) else 0

# Calculate Modified Sgarbossa Criteria total score
total_score = m1_score_I + m1_score_II + m1_score_III + m2_score_I + m2_score_II + m2_score_III + m3_score_I + m3_score_II + m3_score_III

# Interpret ECG result based on Modified Sgarbossa Criteria score
if total_score >= 3:
    print('Significant ST segment elevation suggestive of acute MI')
elif total_score == 2:
    print('Indeterminate score, consider repeating ECG or obtaining serial cardiac biomarkers')
else:
    print('Low probability of acute MI')
