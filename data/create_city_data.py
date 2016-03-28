#!/usr/bin/env python

import csv
import locale

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
cities_files = ['cities5000.txt', 'AQ.txt']
output_file = "../worldclockwidget/src/main/assets/city_data.csv"

manually_added_cities = \
    [('2179670', 'Wanganui')
    ,('4168228', 'Pensacola')
    ,('6324733', 'St. John\'s')
    ,('3042237', 'Douglas')
    ,('3372783', 'Ponta Delgada')
    ,('6362987', 'Ceuta')
    ,('3838859', 'Rio Gallegos')
    ,('3833367', 'Ushuaia')
    ,('5955960', 'Fort St. John')
    ,('3664321', 'Eirunepe')
    ,('4266307', 'Vincennes')
    ,('5554072', 'Juneau')
    ,('3513563', 'Kralendijk')
    ,('5690366', 'Mandan')
    ,('6076211', 'Moncton')
    ,('3984237', 'Santa Isabel')
    ,('6166142', 'Thunder Bay')
    ,('6180550', 'Whitehorse')
    ,('6185377', 'Yellowknife')
    ,('2032614', 'Baruun-Urt')
    ,('2123628', 'Magadan')
    ,('2173911', 'Broken Hill')
    ,('1516048', 'Khovd')
    ,('3164603', 'Venice')
    ,('3166548', 'Siena')
    ,('6620778', 'Palmer Station')
    ,('6299995', 'Amundsen-Scott South Pole Station')
    ,('6620770', 'McMurdo Station')
    ,('4062577', 'Florence')
    ,('5877641', 'Wasilla')
    ]

time_zone_replacements = {
    'Arctic/Longyearbyen': 'Europe/Oslo',
}

levels = {
    'CH' : 55000,
    'US' : 80000,
    'CA' : 60000,
    'AU' : 70000,
    'CN' : 120000,
    'IN' : 110000,
    'AF' : 140000,
    'CD' : 140000,
    'IT' : 70000,
    'HR' : 60000,
    'NZ' : 20000,
    'ES' : 65000,
    'VN' : 70239,
}

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
        if not capital:
            return False
        name_match = False
        if capital == row[NAME] or capital == row[ASCIINAME]:
            name_match = True
        for name in row[ALTERNATENAMES].split(','):
            if capital == name:
                name_match = True
        if name_match:
            population = self.countries[row[ISO_CODE]]['population']
            return population < 3000000
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
    ci = CountryInfo(countries_file)

    for cities_file in cities_files:
        read_cities_file(ci, cities_file)
    write_output_file(ci)

def read_cities_file(ci, cities_file):
    columns_to_copy = [
        NAME,
        ASCIINAME,
        LATITUDE,
        LONGITUDE,
        ISO_CODE,
        TIMEZONE]
    with open(cities_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t', quoting=csv.QUOTE_NONE)
        for row in reader:
            if select_row(ci, row):
                newrow = []
                for index in columns_to_copy:
                    newrow.append(row[index])
                ci.add_city(newrow)

def write_output_file(ci):
    with open(output_file, 'wb') as w:
        writer = csv.writer(w, delimiter='\t', quoting=csv.QUOTE_NONE)
        # sort countries by name
        for country in sorted(ci.countries.values(), key=lambda c:c['name']):
            locale.setlocale(locale.LC_ALL, '')
            # sort cities by name (1st column)
            for city in sorted(country['cities'], key=lambda c:locale.strxfrm(c[0])):
                if city[5] in time_zone_replacements:
                    city[5] = time_zone_replacements[city[5]]
                writer.writerow(city)
        # add GMT
        writer.writerow(['UTC', 'GMT Zulu', '0', '0', 'Coordinated Universal Time', 'GMT'])

def select_row(ci, row):
    if checkPopulationLevel(row):
        return True
    if ci.is_capital(row):
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

def checkPopulationLevel(row):
    iso_code = row[ISO_CODE]
    if iso_code in levels:
        return int(row[POPULATION]) >= levels[iso_code];
    return int(row[POPULATION]) >= 100000

if __name__ == "__main__":
    main()

