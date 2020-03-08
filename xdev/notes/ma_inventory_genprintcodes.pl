#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use utf8;
use strict;
use warnings FATAL => 'all';
use autodie;

if($#ARGV ne 2 or $ARGV[0] eq "--help") {
	print "USAGE ma_inventory_genprintcodes FROM NUM PDFFILE\n";
	exit(0);
}


