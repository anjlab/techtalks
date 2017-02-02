import re
import os
import sys
from subprocess import Popen, PIPE

script_path=os.path.dirname(os.path.abspath(__file__))
decrypt=os.path.join(script_path, 'decrypt')

input_str = sys.stdin.read()

def my_replace(match):
    match = match.group(1)
    p = Popen([decrypt, match], stdin=PIPE, stdout=PIPE, stderr=PIPE)
    output, err = p.communicate()
    if p.returncode != 0:
        raise ValueError('Error decrypting "'+ match +'"\n' + err)
    return output

print re.sub(r'aws:kms:[^:]+:(.*)', my_replace, input_str)
