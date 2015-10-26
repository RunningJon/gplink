#!/bin/bash
gplink_stop 8050
gplink_stop 8051
gplink_start 8050
gplink_start 8051

for i in $(ls *.sql); do
	psql -f $i
done
