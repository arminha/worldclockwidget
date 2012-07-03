#!/usr/bin/env python

import glob
import os.path
import subprocess

res_dir = '../worldclockwidget/res/'
resolutions = [
    ('mdpi', 48),
    ('hdpi', 72),
    ('xhdpi', 96),
]

# inkscape --export-png=output.png --export-width=72 --export-height=72 input.svg
def export(in_file, out_file, size):
    print 'export %s with size %d to %s' % (in_file, size, out_file)
    subprocess.check_call([
        'inkscape',
        '--export-png=%s' % out_file, 
        '--export-width=%d' % size,
        '--export-height=%d' % size,
        in_file])

def get_out_file(in_file):
    _, out_file = os.path.split(in_file)
    out_file, _ = os.path.splitext(out_file)
    out_file += '.png'
    return out_file

def main():
    for in_file in glob.glob('weather_*.svg'):
        for res, size in resolutions:
            out_file = get_out_file(in_file)
            out_file = os.path.join(res_dir, 'drawable-' + res, out_file)
            export(in_file, out_file, size)

if __name__ == "__main__":
    main()
