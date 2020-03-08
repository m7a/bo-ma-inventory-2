#!/bin/sh -e
# Barcode in terminal testing application 1.0.0, Copyright (c) 2019 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.
#
# test_full             Provide Code128 in terminal with full-size characters
# test_half             Code128 with two lines per character
# test_datamatrix       Datamatrix with squares of width=2,height=1
# test_datamatrix_half  Datamatrix with squares of width=1,height=1/2

test_full() {
	for k in a b c d e f; do
		printf "█████"
		blk=1
		for i in 2 1 1 2 1 4 1 2 1 1 2 4 1 2 1 1 2 4 1 1 1 3 4 1 2 3 3 1 1 1 2; do
			j=0
			if [ "$blk" = 1 ]; then
				while [ "$j" -lt "$i" ]; do
					printf " "
					j=$((j + 1))
				done
				blk=0
			else
				while [ "$j" -lt "$i" ]; do
					printf "█"
					j=$((j + 1))
				done
				blk=1
			fi
		done
		printf "█████\n"
	done
}

test_half() {
	line=
	blk=1;
	# short: 2 1 1 2 1 4 1 2 1 1 2 4 1 2 1 1 2 4 1 1 1 3 4 1 2 3 3 1 1 1 2
	# long: 2 1 1 2 1 4 1 1 2 2 1 4 1 1 4 2 1 2 1 4 1 1 2 2 1 2 2 1 1 4 1 2 2 1 1 4 2 3 1 1 1 3 4 2 1 2 1 1 2 4 1 1 1 2 2 3 3 1 1 1 2
	for i in 2 1 1 2 1 4 1 1 2 2 1 4 1 1 4 2 1 2 1 4 1 1 2 2 1 2 2 1 1 4 1 2 2 1 1 4 2 3 1 1 1 3 4 2 1 2 1 1 2 4 1 1 1 2 2 3 3 1 1 1 2; do
		j=0
		if [ "$blk" = 1 ]; then
			while [ "$j" -lt "$i" ]; do
				line="${line}_"
				j=$((j + 1))
			done
			blk=0
		else
			while [ "$j" -lt "$i" ]; do
				line="${line}w"
				j=$((j + 1))
			done
			blk=1
		fi
	done
	baklin="$line"
	echo ████████████████████████████████████████████████████████████
	for i in a b c; do
		line="$baklin"
		printf "██"
		while [ -n "$line" ]; do
			curchr="$(echo "$line" | cut -c 1-2)"
			line="$(echo "$line" | cut -c 3-)"
			case "$curchr" in
			(w|ww) printf "█";;
			(w_) printf "▌";;
			(__) printf " ";;
			(_w|_) printf "▐";;
			(*)  printf "<E%s>" "$curchr";;
			esac
		done
		printf "██\n"
	done
	echo ████████████████████████████████████████████████████████████
}

test_datamatrix() {
	# full-variante. Eine half-variante wäre denkbar mit 2D Ersetzung 11\n10 -> ▛
	# 0 blak 1 white
	sed -e 's/0/  /g' -e 's/1/██/g' <<EOF
1111111111111111
1010101010101011
1010110101111101
1000111110011011
1001000110000101
1000011011100011
1001011011011001
1001111001011011
1000101000110001
1000010101000111
1001001010010001
1000111110000011
1011001001111101
1010001110100111
1000000000000001
1111111111111111
EOF
}

test_datamatrix_half() {
	for i in 1111111111111111:1010101010101011 1010110101111101:1000111110011011 1001000110000101:1000011011100011 1001011011011001:1001111001011011 1000101000110001:1000010101000111 1001001010010001:1000111110000011 1011001001111101:1010001110100111 1000000000000001:1111111111111111; do
		l1="$(echo "$i" | sed 's/\([0-9]\)/\1\1/g' | cut -d: -f1)"
		l2="$(echo "$i" | sed 's/\([0-9]\)/\1\1/g' | cut -d: -f2)"
		while [ -n "$l1" ]; do
			cur1="$(echo "$l1" | cut -c -2)"
			cur2="$(echo "$l2" | cut -c -2)"
			l1="$(echo "$l1" | cut -c 3-)"
			l2="$(echo "$l2" | cut -c 3-)"
			case "$cur1$cur2" in
			(0000) printf " ";;
			(0001) printf "▗";;
			(0010) printf "▖";;
			(0011) printf "▃";;
			(0100) printf "▝";;
			(0101) printf "▐";;
			(0110) printf "▞";;
			(0111) printf "▟";;
			(1000) printf "▘";;
			(1001) printf "▚";;
			(1010) printf "▌";;
			(1011) printf "▙";;
			(1100) printf "▀";;
			(1101) printf "▜";;
			(1110) printf "▛";;
			(1111) printf "█";;
			esac
		done
		echo
	done
}

#test_half
#test_datamatrix
test_datamatrix_half
