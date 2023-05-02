import csv

with open('ecg_data.csv', 'r') as csvfile:
    csvreader = csv.reader(csvfile)
    with open('output.csv', 'w', newline='') as outputfile:
        csvwriter = csv.writer(outputfile)
        for row in csvreader:
            second_value = row[1]
            output_line = ','.join([second_value])
            outputfile.write(output_line+".00,")
