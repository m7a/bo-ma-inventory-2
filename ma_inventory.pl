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

# -- other --
my $dbh; # DBI connection
my @kinputs = qw(id_string quantity class thing location t0 origin importance comments);

# -- data --
my @tbl_internal_ids;
my $tbl_obj = Text::Table->new("ID", "Qty", "Class", "Thing", "Loc", "Imp");
my $is_in_normal_mode = 0; # bool

# -- ui --
my $curses;   # Curses UI
my $scrh;     # integer
my $scrw;     # integer
my $tbl_list; # Listbox table
my $ercl_win; # Window
my $tbl_win;  # Window
my $lbl_status; # Label

# -- current edit --
my %inputs = (
	id_string  => { label => "ID", nocheckbox => 1 },
	quantity   => { label => "Quantity", nocheckbox => 1 },
	class      => { label => "Class", autocomplete => 1 },
	thing      => { label => "Thing", autocomplete => 1 },
	location   => { label => "Location", autocomplete => 1 },
	t0         => { label => "T0", autocomplete => 1 },
	origin     => { label => "Origin", autocomplete => 1 },
	importance => { label => "Importance", autocomplete => 1, },
	comments   => { label => "Comments", height => 4 }
);
my $cedit_internal_id = -1;
my $implicit_quantity = 0;

# == implementation ==

sub inventory_main {
	# -- pre --
	if($#_ < 0 or $_[0] eq "--help") {
		print "USAGE ma_inventory DBFILE\n";
		exit(0);
	}

	$dbh = DBI->connect("dbi:SQLite:dbname=$ARGV[0]", "", "",
						{ sqlite_unicode => 1 });
	$curses = Curses::UI->new(-clear_on_exit => 1);
	$scrh = $curses->height;
	$scrw = $curses->width;

	# -- draw windows --
	$tbl_win = $curses->add("win_table", "Window", -border => 0);
	my @lbl_keys = (
		["lb_f01", "1 RCL",     0], ["lb_f02", "2 Add",   8],
		["lb_f08", "8 Delete", 56], ["lb_f10", "0 Exit", 72]
	);
	$tbl_win->add($_->[0], "Label", -text => $_->[1], -y => $scrh - 1,
						-x => $_->[2]) for(@lbl_keys);
	$tbl_list = $tbl_win->add("table", "Listbox", -y => 0,
				-values => ["(...)"], -height => $scrh - 1);

	$ercl_win = $curses->add("win_edit_rcl", "Window", -border => 0);
	inventory_draw_codes($ercl_win);

	$ercl_win->add("lb_enter_option", "Label", -text => "On Enter",
					-y => 0, -x => 22);
	$ercl_win->add("status", "Label", -text => "Status", -y => 2, -x => 22);
	my $line_width = $scrw - 33 - 18;
	$lbl_status = $ercl_win->add("status_cnt", "Label",
		-width => $line_width, -text => "unknown", -y => 2, -x => 33);

	# two loops to have a sensible tabindex order
	my $cy = 2;
	for my $key (@kinputs) {
		my $val = $inputs{$key};
		$cy += 2;
		$ercl_win->add("lbl_input_".$key, "Label",
				-text => $val->{label}, -y => $cy, -x => 22);
		my $height = $val->{height} // 1;
		$val->{text} = $ercl_win->add("text_input_".$key, "TextEditor",
			-y => $cy, -x => 33, -showlines => 1,
			-width => $line_width,
			-singleline => ($height == 1), -height => $height);
	}
	$cy = 2;
	for my $key (@kinputs) {
		my $val = $inputs{$key};
		$cy += 2;
		$val->{checkbox} = $ercl_win->add("chck_input_".$key,
					"Checkbox", -y => $cy, -x => 18)
					unless(defined($val->{nocheckbox}));
	}
	# -- Enter options --
	# Auto
	#   Enter in ID -> RCL set to edit
	#   Enter in other field -> Autocomplete if selected otherwise ignore
	# OK
	#   Enter -> OK (save i.e. add or update depending on ID)
	# Nothing
	#   Enter -> ignore
	# Recall
	#   Enter -> RCL from DB and set form to first result (edit)
	# Autocomplete
	#   Enter -> take from autocomplete use first if nothing selected
	my $enter_option = $ercl_win->add("enter_option", "Popupmenu",
		-y => 0, -x => 33, -selected => 0,
		-values => ["Auto", "Recall", "OK", "Autocomplete", "Nothing"]);
	my $keep_enter_option = $ercl_win->add("keep_enter_option", "Checkbox",
					-y => 0, -x => 18, -checked => 1);

	# only display + enable auto-complete field if at least space for three
	# entries
	if(($cy + 8) < $scrh) {
		my $autocomplete_height = $scrh - $cy - 5;
		my $autocomplete_list = $ercl_win->add("autocomplete_list",
			"Listbox", -radio => 1, values => [], -selected => 0,
			-y => $cy + 5, -x => 18, -width => $scrw - 18 - 16,
			-height => $autocomplete_height);
		for my $key (@kinputs) {
			next unless defined($inputs{$key}->{autocomplete});
			my $field = $inputs{$key}->{text};
			$field->onChange(sub {
				# TODO ASTAT SUBSTAT GETTING EMPTY RESULTS HERE (FIRST RESULT) -- SHOULD ALL BE NULL!
				# cannot use prepared statement here because
				# it will interpret key as string rather than
				# table name...
				my $stmt = $dbh->prepare(
					"SELECT DISTINCT $key FROM inventory ".
					"WHERE $key LIKE ? ".
					"ORDER BY $key ASC LIMIT ?"
				);
				$stmt->execute($field->get()."%",
							$autocomplete_height);
				my $results = [];
				while((my @row = $stmt->fetchrow_array) != 0) {
					push @{$results}, $row[0];
				}
				$autocomplete_list->values($results);
				$autocomplete_list->draw();
			});
		}
	}

	# -- add bindings --
	$tbl_win->set_binding(sub {
		my $selidx = $tbl_list->get_active_id;
		my $switch = 1;
		$switch = inventory_edit_id($tbl_internal_ids[$selidx])
					if(defined($tbl_internal_ids[$selidx]));
		inventory_display_editrcl() if($switch);
	}, Curses::UI::TextEditor::KEY_ENTER()); # Enter -- Edit
	$tbl_win->set_binding(sub {
		# TODO z
		$curses->dialog("Not implemented; Use Add dialog for recall.");
	}, 265); # F1 -- recall
	$tbl_win->set_binding(sub {
		inventory_action_reset();
		inventory_display_editrcl();
	}, 266); # F2 -- add
	$tbl_win->set_binding(sub {
		$curses->mainloopExit;
	}, 274); # F10 -- exit

	$ercl_win->set_binding(\&inventory_action_ok,     268); # F4  -- OK
	$ercl_win->set_binding(\&inventory_action_ok,     271); # F7  -- recall
	$ercl_win->set_binding(\&inventory_action_reset,  273); # F9  -- reset
	$ercl_win->set_binding(\&inventory_action_cancel, 274); # F10 -- exit
	$ercl_win->set_routine("inventory_tmp_key_esc",
				\&inventory_temporary_default_binding_escape);
	$ercl_win->set_binding(sub {
		if($is_in_normal_mode) {
			$is_in_normal_mode = 0;
			$ercl_win->clear_binding("inventory_tmp_key_esc");
		} else {
			$is_in_normal_mode = 1;
			$ercl_win->set_binding("inventory_tmp_key_esc", "");
		}
	}, Curses::UI::Common::CUI_ESCAPE()); # Escape (barcode sequence)

	# -- update data --
	inventory_update_displayed_table();

	# -- mainloop --
	$curses->mainloop();

	# -- post --
	$dbh->disconnect;
}

# $_[0] internal ID to RCL
sub inventory_edit_id {
	$cedit_internal_id = shift;
	my $stmt = $dbh->prepare(
		"SELECT id_string, quantity, class, thing, location, t0, ".
			"origin, importance, comments ".
		"FROM inventory ".
		"WHERE id_internal = ?;"
	);
	$stmt->execute($cedit_internal_id);
	my $row = $stmt->fetchrow_hashref;
	if(not(defined($row))) {
		$curses->error("Failed to query DB for $cedit_internal_id.");
		$cedit_internal_id = -1;
		return 0;
	}
	$inputs{$_}->{text}->text($row->{$_} // "") for (@kinputs);
	$implicit_quantity = $row->{quantity};
	inventory_update_status("EDIT");
	$inputs{id_string}->{text}->focus();
	return 1;
}

# $_[0] mode (EDIT, ARCL)
sub inventory_update_status {
	my $mode = shift;
	$lbl_status->text("$mode dID=$cedit_internal_id ".
						"iQTY=$implicit_quantity");
}

sub inventory_display_editrcl {
	$tbl_win->lose_focus;
	$ercl_win->focus;
}

sub inventory_temporary_default_binding_escape {
	my ($_binding, $key, @_extra) = @_; # Widget.pm~977
	$is_in_normal_mode = 0;
	$ercl_win->clear_binding("inventory_tmp_key_esc");
	# O: "OK"           disambiguate or complete
	# C: "Cancel"       cancel
	# D: "Disambiguate" disambiguate only aka. Recall
	# R: "Reset"        reset
	if($key eq "o") {
		inventory_action_ok();
	} elsif($key eq "c") {
		inventory_action_cancel();
	} elsif($key eq "d") {
		inventory_action_recall();
	} elsif($key eq "r") {
		inventory_action_reset();
	} # else ignore unknown escape command
}

sub inventory_action_reset {
	# clear form and disassociate from edit. keep enter option and reset
	# all other checkboxes.
	$inputs{$_}->{text}->text("") for (@kinputs);
	$cedit_internal_id = -1;
	$implicit_quantity = 0;
	inventory_update_status("ARCL");
	$inputs{id_string}->{text}->focus();
}

sub inventory_action_recall {
	# match all fields against DB (like %%, ID needs to be absent or exact)
}

sub inventory_action_ok {
	# ... TODO ASTAT: SAVE STEP!
}

sub inventory_action_cancel {
	# TODO z NOT IMMEDIATE?
	$ercl_win->lose_focus;
	$tbl_win->focus;
}

sub inventory_update_displayed_table {
	# -5 for five column separator spaces
	my $wavail = $scrw - 16 - 3 - 9 - 3 - 5;
	my @colmaxlen = (16, 3, int($wavail * 1 / 2),
						int($wavail * 1 / 2), 9, 3);
	my $stmt = $dbh->prepare("SELECT id_internal, id_string, quantity, ".
			"class, thing, location, importance FROM inventory;");
	$stmt->execute;
	$tbl_obj->clear;
	@tbl_internal_ids = (undef);
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
