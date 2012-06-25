#!/bin/python

import csv

def main():
    columns_to_copy = [0, 1, 2, 4, 5, 8, 17]
    population = 14

    cities_file = 'cities15000.txt'
    output_file = "../worldclockwidget/assets/city_data.csv"

    with open(cities_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        with open(output_file, 'wb') as w:
            writer = csv.writer(w, delimiter='\t')
            for row in reader:
                if int(row[population]) > 50000:
                    newrow = []
                    for index in columns_to_copy:
                        newrow.append(row[index])
                    writer.writerow(newrow)

if __name__ == "__main__":
    main()

