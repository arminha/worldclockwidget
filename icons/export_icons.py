#!/usr/bin/env python

import glob
import os.path
import subprocess
import sys

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

def main(argv):
    if len(argv) == 1:
        input_files = glob.glob('ic_*.svg') + glob.glob('weather_*.svg')
    else:
        input_files = argv[1:]
    for in_file in input_files:
        for res, size in resolutions:
            out_file = get_out_file(in_file)
            out_file = os.path.join(res_dir, 'drawable-' + res, out_file)
            export(in_file, out_file, size)

if __name__ == "__main__":
    main(sys.argv)
