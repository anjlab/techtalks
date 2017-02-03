#!/bin/bash
set -e

SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

URL="https://my-ap-MyALB-10L15DBDUL0W9-435170741.eu-central-1.elb.amazonaws.com?${@}"

PREV_RESPONSE=
COOKIES_FILE=${SCRIPT_PATH}/cookies.tmp$(date +%s)

while true
do
    TIMESTAMP=`date +%H:%M:%S`
    printf "${TIMESTAMP} => "

    OUTPUT=$(curl --silent --insecure ${URL} --write-out %{http_code} \
	--cookie ${COOKIES_FILE} --cookie-jar ${COOKIES_FILE})

    # Move HTTP code from the end to the start of the output string
    RESPONSE="$(echo ${OUTPUT} | gsed -E 's/(.*)\s([0-9]*)$/\2 \1/g')"

    if [[ ${PREV_RESPONSE} != "" ]] && [[ ${PREV_RESPONSE} != ${RESPONSE} ]]; then
        echo -e "\r           \n${TIMESTAMP} => ${RESPONSE}"
    else
        echo ${RESPONSE}
    fi

    PREV_RESPONSE=${RESPONSE}

    sleep 1
done

