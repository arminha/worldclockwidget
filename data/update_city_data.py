#!/usr/bin/env python

'''
Download the latest versions of cities5000.txt and countryInfo.txt from
http://download.geonames.org/export/dump/
'''

import urllib
import zipfile
import sys
import os

def main():
    retrieve('countryInfo.txt')
    citiesZip = 'cities5000.zip'
    retrieve(citiesZip)
    unzip(citiesZip)
    retrieve('AQ.zip')
    unzip('AQ.zip')

def retrieve(fileName):
    printnln('Retrieve %s..' % fileName)
    urllib.urlretrieve(BASE_URL + fileName, fileName)
    print(' Done.')

def unzip(fileName):
    printnln('Unzip %s..' % fileName)
    with zipfile.ZipFile(fileName, 'r') as zfile:
        zfile.extractall()
    os.remove(fileName)
    print(' Done.')

def printnln(text):
    sys.stdout.write(text)
    sys.stdout.flush()

BASE_URL = 'http://download.geonames.org/export/dump/'

if __name__ == "__main__":
    main()
