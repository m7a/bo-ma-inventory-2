#!/usr/bin/perl
use utf8;
use strict;
use warnings FATAL => 'all';
use autodie;

require Barcode::Code128; # libbarcode-code128-perl
use Data::Dumper 'Dumper'; # debug only

binmode(STDOUT, ":encoding(UTF-8)");

my @data = split('', "    ".Barcode::Code128->new->barcode("\033r")."    ");
my @half_codes = (" ", "▐", "▌", "█");
my $line = "";
for(my $i = 0; $i < @data; $i += 2) {
	my $booli = ($data[$i    ] eq "#");
	my $booln = ($data[$i + 1] eq "#");
	my $idx = ($booli << 1) | $booln;
	$idx = ~$idx; # for typical terminal white on black.
	$line .= $half_codes[$idx];
}

print "$line\n";
print "$line\n";
print "$line\n";
print "$line\n";

#print Dumper(@data);

#print $data;
