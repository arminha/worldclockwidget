#!/bin/python

import csv

def main():
    iso_code = 0
    name = 4

    countries_file = 'countryInfo.txt'
    out_file = '../worldclockwidget/res/values/country.xml'

    with open(countries_file, 'rb') as r:
        reader = csv.reader(r, delimiter='\t')
        with open(out_file, 'wb') as w:
            header = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
'''
            footer = '</resources>'
            w.write(header)
            for row in reader:
                if not row[0].startswith('#'):
                    w.write('    <string name="country_%(iso)s">%(name)s</string>\n' % \
                            {'iso': row[iso_code], 'name': row[name]})
            w.write(footer)

if __name__ == "__main__":
    main()
