#!/bin/sh -e
# Ma_Sys.ma Inventory 1.0.0.0 -- PDF Export Facility,
# Copyright (c) 2016 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

head -n 4 "$0" | tail -n 2
echo

if [ $# = 0 -o "$1" = "--help" ]; then
	echo USAGE $0 DIRECTORY 1>&2
	exit 1
fi

target="$1"

if [ ! -d "$target" ]; then
	echo Target directory $target does not exist. 1>&2
	exit 1
fi

cd "$target"

find . -maxdepth 1 -type f -name 'bc_*.svg' | {
	pids=
	while read -r line; do
		svg2pdf "$line" "${line%.*}.pdf" &
		pids="$pids $!"
	done
	wait $pids || true
}

pdflatex barcodes
pdflatex barcodes
