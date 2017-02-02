#!/bin/bash

gosu postgres createdb db \
    --encoding='utf-8' \
    --locale=en_US.utf8 \
    --template=template0 \
    --owner=app