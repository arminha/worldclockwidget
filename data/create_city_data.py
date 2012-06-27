#!/bin/python

import csv

#TODO individual population levels

# rows in cities15000
GEONAMEID = 0
NAME = 1
ASCIINAME = 2
ALTERNATENAMES = 3
LATITUDE = 4
LONGITUDE = 5
ISO_CODE = 8
POPULATION = 14
TIMEZONE = 17

countries_file = 'countryInfo.txt'
cities_file = 'cities15000.txt'
output_file = "../worldclockwidget/assets/city_data.csv"

class CountryInfo(object):
    def __init__(self, info_file):
        object.__init__(self)
        self.countries = dict()
        with open(info_file, 'rb') as r:
            reader = csv.reader(r, delimiter='\t')
            for row in reader:
                if not row[0].startswith('#'):
                    self.countries[row[0]] = { 
                        'iso' : row[0],
                        'name' : row[4],
                        'capital' : row[5],
                        'population' : int(row[7])
                    }

    def is_capital(self, row):
        capital = self.countries[row[ISO_CODE]]['capital']
        if capital == row[NAME] or capital == row[ASCIINAME]:
            return True
        for name in row[ALTERNATENAMES].split(','):
            if capital == name:
                return True
        return False

def main():
    columns_to_copy = [
        GEONAMEID,
        NAME,
        ASCIINAME,
        LATITUDE,
        LONGITUDE,
        ISO_CODE,
        TIMEZONE]

    ci = CountryInfo(countries_file)

    with open(cities_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        with open(output_file, 'wb') as w:
            writer = csv.writer(w, delimiter='\t')
            for row in reader:
                if select_row(ci, row):
                    newrow = []
                    for index in columns_to_copy:
                        newrow.append(row[index])
                    writer.writerow(newrow)

def select_row(ci, row):
    return int(row[POPULATION]) > 100000 or ci.is_capital(row)

if __name__ == "__main__":
    main()

