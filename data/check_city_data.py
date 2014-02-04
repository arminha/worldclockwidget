#!/usr/bin/env python

import csv

# columns in cities15000
TIMEZONE = 17

TIMEZONE_OUTPUT = 5

cities_file = 'cities15000.txt'
output_file = "../worldclockwidget/assets/city_data.csv"


def main():
    time_zones = set();

    # add all time zones in the source file
    with open(cities_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        for row in reader:
            time_zones.add(row[TIMEZONE])
    
    with open(output_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        for row in reader:
            timezone = row[TIMEZONE_OUTPUT]
            if timezone in time_zones:
                time_zones.remove(timezone)

    print('%d missed time zones:' % len(time_zones))
    for tz in sorted(time_zones):
        print('\t%s' % tz)

if __name__ == "__main__":
    main()

