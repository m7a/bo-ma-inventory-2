#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use utf8;
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

my $tbl_obj = Text::Table->new("ID", "Qty", "Class", "Thing", "Loc", "Imp");
my $dbh;      # DBI connection
my $scrh;     # integer
my $scrw;     # integer
my $tbl_list; # Listbox table

sub inventory_main {
	# -- pre --
	if($#_ < 0 or $_[0] eq "--help") {
		print "USAGE ma_inventory DBFILE\n";
		exit(0);
	}

	$dbh = DBI->connect("dbi:SQLite:dbname=$ARGV[0]", "", "",
						{ sqlite_unicode => 1 });
	my $curses = Curses::UI->new(-clear_on_exit => 1,
							-mouse_support => 0);
	$scrh = $curses->height;
	$scrw = $curses->width;

	# -- draw windows --
	my $tbl_win = $curses->add("win_table", "Window", -border => 0);
	$tbl_win->add("lb_f01", "Label",
			-text => "1 RCL",    -y => $scrh - 1, -x => 0);
	$tbl_win->add("lb_f02", "Label",
			-text => "2 Add",    -y => $scrh - 1, -x => 8);
	$tbl_win->add("lb_f08", "Label",
			-text => "8 Delete", -y => $scrh - 1, -x => 56);
	$tbl_win->add("lb_f10", "Label",
			-text => "0 Exit",   -y => $scrh - 1, -x => 72);
	$tbl_list = $tbl_win->add("table", "Listbox",
			-y => 0, -values => ["(...))"], -height => $scrh - 1);

	my $ercl_win = $curses->add("win_edit_rcl", "Window", -border => 0);
	inventory_draw_codes($ercl_win);

	# -- add bindings --
	$tbl_win->set_binding(sub {
		$tbl_win->lose_focus;
		$ercl_win->focus;
	}, Curses::UI::TextEditor::KEY_ENTER()); # Enter -- Edit
	$tbl_win->set_binding(sub {
		$curses->mainloopExit;
	}, 274); # F10 -- exit

	$ercl_win->set_binding(sub {
		$ercl_win->lose_focus;
		$tbl_win->focus;
	}, 274); # F10 -- exit

	# -- update data --
	inventory_update_displayed_table();

	# -- post --
	$curses->mainloop();
	$dbh->disconnect;
}

sub inventory_update_displayed_table {
	$tbl_obj->clear;
	# -5 for five column separator spaces
	my $wavail = $scrw - 16 - 3 - 9 - 3 - 5;
	my @colmaxlen = (16, 3, int($wavail * 1 / 2),
						int($wavail * 1 / 2), 9, 3);
	my $stmt = $dbh->prepare("SELECT id_internal, id_string, quantity, ".
			"class, thing, location, importance FROM inventory;");
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
}

# TODO z CODES TBD
sub inventory_draw_codes {
	my $ercl_win = shift;
	inventory_draw_code($ercl_win, "   Reset (F9)", 8, 0, 0, [
		"█▀█▀█▀█▀█▀█▀█▀██",
		"█ ▀ ██▃█▃▀▀██▀▃█",
		"█  ▀ ▃▃▀█▃▃  ▀▃█",
		"█  █▃██ ▀█ ██ ▃█",
		"█   ▀▃▀▃ ▃▀▀ ▃▃█",
		"█  ▀▃▃█▃█  ▀  ▃█",
		"█ █▀  █▃▃▀█▀▀█▃█",
		"█▃▃▃▃▃▃▃▃▃▃▃▃▃▃█"
	]);
	inventory_draw_code($ercl_win, "  Recall (F7)", 8, $scrw - 16, 0, [
		"█▀█▀█▀█▀█▀█▀█▀██",
		"█ ▀ ██▃█▃▀▀██▀▃█",
		"█  ▀ ▃▃▀█▃▃  ▀▃█",
		"█  █▃██ ▀█ ██ ▃█",
		"█   ▀▃▀▃ ▃▀▀ ▃▃█",
		"█  ▀▃▃█▃█  ▀  ▃█",
		"█ █▀  █▃▃▀█▀▀█▃█",
		"█▃▃▃▃▃▃▃▃▃▃▃▃▃▃█"
	]);
	inventory_draw_code($ercl_win, "  Cancel (F10)", $scrh - 9, 0,
								$scrh - 8, [
		"█▀█▀█▀█▀█▀█▀█▀██",
		"█ ▀ ██▃█▃▀▀██▀▃█",
		"█  ▀ ▃▃▀█▃▃  ▀▃█",
		"█  █▃██ ▀█ ██ ▃█",
		"█   ▀▃▀▃ ▃▀▀ ▃▃█",
		"█  ▀▃▃█▃█  ▀  ▃█",
		"█ █▀  █▃▃▀█▀▀█▃█",
		"█▃▃▃▃▃▃▃▃▃▃▃▃▃▃█"
	]);
	inventory_draw_code($ercl_win, "    OK (F4)", $scrh - 9, $scrw - 16,
								$scrh - 8, [
		"█▀█▀█▀█▀█▀█▀█▀██",
		"█ ▀ ██▃█▃▀▀██▀▃█",
		"█  ▀ ▃▃▀█▃▃  ▀▃█",
		"█  █▃██ ▀█ ██ ▃█",
		"█   ▀▃▀▃ ▃▀▀ ▃▃█",
		"█  ▀▃▃█▃█  ▀  ▃█",
		"█ █▀  █▃▃▀█▀▀█▃█",
		"█▃▃▃▃▃▃▃▃▃▃▃▃▃▃█"
	]);
}

# $_[0] window
# $_[1] label
# $_[2] y
# $_[3] x
# $_[4] y0 (for code)
# $_[5] code (reference)
sub inventory_draw_code {
	my ($ercl_win, $label, $y, $x, $y0code, $code) = @_;
	$ercl_win->add("lb_gen_${y}_${x}", "Label", -text => $label,
							-y => $y, -x => $x);
	$y = $y0code;
	for my $line (@{$code}) {
		$ercl_win->add("lb_gen2_${y}_${y0code}_$x", "Label",
					-text => $line, -y => $y, -x => $x);
		$y++;
	}
}

inventory_main @ARGV;
