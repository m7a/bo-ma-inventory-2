#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use strict;
use warnings FATAL => 'all';
use autodie;

require DBI;         # libdbd-sqlite3-perl
require Text::Table; # libtext-table-perl

use File::Basename;
use Data::Dumper 'Dumper'; # debug only

# load locally changed modules below...
use lib dirname(__FILE__);
require Curses::UI;  # libcurses-ui-perl

if($#ARGV < 0 or $ARGV[0] eq "--help") {
	print "USAGE ma_inventory DBFILE\n";
	exit(0);
}

my $dbh = DBI->connect("dbi:SQLite:dbname=$ARGV[0]", "", "",
						{ sqlite_unicode => 1 });
my $curses = Curses::UI->new(-clear_on_exit => 1, -mouse_support => 0);
my $scrh = $curses->height;
my $scrw = $curses->width;

my $tbl_win = $curses->add("win_table", "Window", -border => 0);
$tbl_win->add("lb_f02", "Label", -text => "2 Add",    -y => $scrh-1, -x => 8);
$tbl_win->add("lb_f08", "Label", -text => "8 Delete", -y => $scrh-1, -x => 56);
$tbl_win->add("lb_f10", "Label", -text => "0 Exit",   -y => $scrh-1, -x => 72);
my $tbl_list = $tbl_win->add("table", "Listbox", -y => 0, -values => ["(...))"],
							-height => $scrh - 1);
$tbl_win->set_binding(sub {
	$curses->mainloopExit;
}, 274); # F10 -- exit

my $tbl_obj = Text::Table->new("ID", "Qty", "Class", "Thing", "Loc", "Imp");

# --
my $wavail = $scrw - 16 - 3 - 9 - 3 - 5; # -5 for five column separator spaces
my @colmaxlen = (16, 3, int($wavail * 1 / 2), int($wavail * 1 / 2), 9, 3);
my $stmt = $dbh->prepare("SELECT id_internal, id_string, quantity, class, ".
				"thing, location, importance FROM inventory;");
$stmt->execute;
my @tbl_internal_ids;
while((my @row = $stmt->fetchrow_array) != 0) {
	push @tbl_internal_ids, $row[0];
	my @outrow;
	for(0 .. 5) {
		my $curr = $row[$_ + 1];
		push @outrow, (length($curr) > $colmaxlen[$_]?
				substr($curr, 0, $colmaxlen[$_]): $curr);
	}
	$tbl_obj->add(@outrow);
}
$tbl_list->values([$tbl_obj->table]);
# --

$curses->mainloop();
$dbh->disconnect;
