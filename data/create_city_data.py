#!/usr/bin/env python

import csv
import locale

#TODO individual population levels

# columns in cities15000
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

manually_added_cities = \
    [('2179670', 'Wanganui')
    ,('4168228', 'Pensacola')
    ]

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
                        'population' : int(row[7]),
                        'cities' : []
                    }

    def is_capital(self, row):
        '''
        Returns true if the row refers to a capital.
        '''
        capital = self.countries[row[ISO_CODE]]['capital']
        if capital == row[NAME] or capital == row[ASCIINAME]:
            return True
        for name in row[ALTERNATENAMES].split(','):
            if capital == name:
                return True
        return False

    def add_city(self, row):
        '''
        Add a row to the city list of the specific country.
        Replaces the iso code with the country name.
        '''
        country = self.countries[row[4]]
        cities = country['cities']
        row[4] = country['name']
        cities.append(row)

def main():
    columns_to_copy = [
        NAME,
        ASCIINAME,
        LATITUDE,
        LONGITUDE,
        ISO_CODE,
        TIMEZONE]

    ci = CountryInfo(countries_file)

    with open(cities_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        for row in reader:
            if select_row(ci, row):
                newrow = []
                for index in columns_to_copy:
                    newrow.append(row[index])
                ci.add_city(newrow)
    with open(output_file, 'wb') as w:
        writer = csv.writer(w, delimiter='\t')
        # sort countries by name
        for country in sorted(ci.countries.values(), key=lambda c:c['name']):
            locale.setlocale(locale.LC_ALL, '')
            # sort cities by name (1st column)
            for city in sorted(country['cities'], key=lambda c:locale.strxfrm(c[0])):
                writer.writerow(city)
        # add GMT
        writer.writerow(['GMT', 'UTC Zulu', '0', '0', 'Greenwich Mean Time', 'GMT'])

def select_row(ci, row):
    if int(row[POPULATION]) > 100000 or ci.is_capital(row):
        return True
    if is_manually_added(row):
        return True
    return False

def is_manually_added(row):
    for (id, name) in manually_added_cities:
        if id == row[GEONAMEID]:
            if name != row[ASCIINAME]:
                print 'WARNING: name of city [%s] has changed from "%s" to "%s"' % (id, name, row[ASCIINAME])
            print 'manually added city: %s, %s [%s]' % (row[ASCIINAME], row[ISO_CODE], id)
            return True
    return False


if __name__ == "__main__":
    main()

