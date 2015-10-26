#!/bin/bash

for i in $(ls *.sql); do
	psql -f $i
done
