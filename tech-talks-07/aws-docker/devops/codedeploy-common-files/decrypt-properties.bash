#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

cd ${SCRIPT_PATH}

for file in data/*.properties
do
    cat ${file} | python decrypt-file.py > ${file}.decrypted
done