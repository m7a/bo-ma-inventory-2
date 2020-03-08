#!/usr/bin/perl
use utf8;
use strict;
use warnings FATAL => 'all';
use autodie;

require Barcode::DataMatrix; # libbarcode-datamatrix-perl
use Data::Dumper 'Dumper'; # debug only

binmode(STDOUT, ":encoding(UTF-8)");

my $data = Barcode::DataMatrix->new->barcode("\033r");

for my $row (@$data) {
	print ">";
	print for map { $_ ? "#" : ' ' } @$row;
	print "<\n";
}

my @half_codes = (" ", "▗", "▖", "▃", "▝", "▐", "▞", "▟", "▘", "▚", "▌", "▙",
							"▀", "▜", "▛", "█");
#push @drawd, 0 for(1..(2*@{@{$data}[0]} + 2));
my $width = @{@{$data}[0]} * 2 + 4;
my @drawd = ([(0) x $width]);
for my $row (@$data) {
	push @drawd, [0, 0, (map { ($_, $_) } @$row), 0, 0];
}
push @drawd, [(0) x $width];
for(my $i = 0; $i < @drawd; $i += 2) {
	for(my $j = 0; $j < $width; $j += 2) {
		my $idx =
			($drawd[$i    ]->[$j    ] << 3) |
			($drawd[$i    ]->[$j + 1] << 2) |
			($drawd[$i + 1]->[$j    ] << 1) |
			($drawd[$i + 1]->[$j + 1]);
		$idx = ~$idx; # invert for black font on white background
		print $half_codes[$idx];
	}
	print "\n";
}
