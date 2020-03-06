#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use utf8;
use strict;
use warnings FATAL => 'all';
use autodie;

require DBI;         # libdbd-sqlite3-perl
require Text::Table; # libtext-table-perl

use Try::Tiny;
use File::Basename;
use Data::Dumper 'Dumper'; # debug only

# load locally changed modules below...
use lib dirname(__FILE__);
require Curses::UI;  # libcurses-ui-perl

# -- other --
my $dbh; # DBI connection
my @kinputs = qw(id_string quantity class thing location t0 origin importance comments); # const
my $table_ui_fields = "id_internal, id_string, quantity, class, thing, ".
						"location, importance"; # const

# -- data --
my @tbl_internal_ids;
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

sub inventory_main {
	inventory_init(@_);
	inventory_draw_window_tbl();
	inventory_draw_window_ercl();
	inventory_add_tbl_win_bindings();
	inventory_add_ercl_win_bindings();
	inventory_update_displayed_table();
	$curses->mainloop();
	$dbh->disconnect;
}

sub inventory_init {
	if($#_ < 0 or $_[0] eq "--help") {
		print "USAGE ma_inventory DBFILE\n";
		exit(0);
	}
	$dbh = DBI->connect("dbi:SQLite:dbname=$ARGV[0]", "", "",
						{ sqlite_unicode => 1 });
	$curses = Curses::UI->new(-clear_on_exit => 1);
	$scrh = $curses->height;
	$scrw = $curses->width;
}

sub inventory_draw_window_tbl {
	$tbl_win = $curses->add("win_table", "Window", -border => 0);
	my @lbl_keys = (["lb_f01", "1 RCL",     0], ["lb_f02", "2 Add",   8],
	                ["lb_f08", "8 Delete", 56], ["lb_f10", "0 Exit", 72]);
	$tbl_win->add($_->[0], "Label", -text => $_->[1], -y => $scrh - 1,
						-x => $_->[2]) for(@lbl_keys);
	$tbl_list = $tbl_win->add("table", "Listbox", -y => 0,
				-values => ["(...)"], -height => $scrh - 1);
}

sub inventory_add_ercl_win_bindings {
	$ercl_win->set_binding(\&inventory_action_ok,     268); # F4  -- OK
	$ercl_win->set_binding(\&inventory_action_recall, 271); # F7  -- recall
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
}

sub inventory_add_tbl_win_bindings {
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
}

sub inventory_draw_window_ercl {
	$ercl_win = $curses->add("win_edit_rcl", "Window", -border => 0);
	inventory_draw_codes($ercl_win);

	$ercl_win->add("lb_enter_option", "Label", -text => "On Enter",
					-y => 0, -x => 22);
	$ercl_win->add("status", "Label", -text => "Status", -y => 2, -x => 22);
	my $line_width = $scrw - 33 - 18;
	$lbl_status = $ercl_win->add("status_cnt", "Label",
		-width => $line_width, -text => "unknown", -y => 2, -x => 33);

	my $cy = inventory_draw_inputs($line_width);

	# only display + enable auto-complete field if at least space for three
	# entries
	inventory_add_autocomplete($cy) if(($cy + 8) < $scrh);
}

# $_[0] line width
sub inventory_draw_inputs {
	my $line_width = shift;
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

	return $cy;
}

# $_[0] current y
sub inventory_add_autocomplete {
	my $cy = shift;
	my $autocomplete_height = $scrh - $cy - 5;
	my $autocomplete_list = $ercl_win->add("autocomplete_list", "Listbox",
			-radio => 1, values => [], -selected => 0,
			-y => $cy + 5, -x => 18, -width => $scrw - 18 - 16,
			-height => $autocomplete_height);
	for my $key (@kinputs) {
		next unless defined($inputs{$key}->{autocomplete});
		my $field = $inputs{$key}->{text};
		$field->onChange(sub {
			# cannot use prepared statement here because
			# it will interpret key as string rather than
			# table name...
			my $stmt = $dbh->prepare(
				"SELECT DISTINCT $key FROM inventory ".
				"WHERE $key LIKE ? ".
				"ORDER BY $key ASC LIMIT ?"
			);
			$stmt->execute($field->get()."%", $autocomplete_height);
			my $results = [];
			while((my @row = $stmt->fetchrow_array) != 0) {
				push @{$results}, $row[0];
			}
			$autocomplete_list->values($results);
			$autocomplete_list->draw();
		});
	}
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
	my $id = $inputs{id_string}->{text}->get();
	my $stmt;
	if($id eq "") {
		$stmt = $dbh->prepare(
			"SELECT $table_ui_fields FROM inventory ".
			"WHERE class LIKE ? AND thing LIKE ? AND ".
				"location LIKE ? AND origin LIKE ? AND ".
				"importance LIKE ?"
		);
		$stmt->execute(
			$inputs{class}->{text}->get()."%",
			$inputs{thing}->{text}->get()."%",
			$inputs{location}->{text}->get()."%",
			$inputs{origin}->{text}->get()."%",
			$inputs{importance}->{text}->get()."%"
		);
	} else {
		$stmt = $dbh->prepare("SELECT id_internal FROM inventory ".
							"WHERE id_string = ?");
		$stmt->execute($id);
	}

	my $all_results = $stmt->fetchall_arrayref();
	my $num_results = scalar @{$all_results};

	if($num_results == 0) {
		$curses->dialog("No matching item found.");
		return;
	}

	my $id_internal_selected = ($num_results == 1)? $all_results->[0]->[0]:
			inventory_disambiguate_recall_results($all_results);

	inventory_edit_id($id_internal_selected)
					if defined $id_internal_selected;
}

# $_[0] all results arrayref
sub inventory_disambiguate_recall_results {
	my $all_results = shift;
	my $window = $curses->add("win_disambiguate", "Window", -border => 0);
	$window->add("lb_disambiguate", "Label", -y => $scrh - 1, -text =>
				"-- DISAMBIGUATE --   ENTER: select item".
				"                                0 Cancel");
	my @internal_ids = ();
	my $tbl = $window->add("table", "Listbox", -y => 0, -height => $scrh-1,
		-values => inventory_add_table_to_list($all_results,
							\@internal_ids));

	my $selection = undef;

	my $local_exit = sub {
		$window->lose_focus;
		$curses->delete("win_disambiguate");
		$ercl_win->focus;
	};
	$window->set_binding(sub {
		my $selidx = $tbl->get_active_id();
		$selection = $internal_ids[$selidx] unless ($selidx == 0);
		$local_exit->();
	}, Curses::UI::TextEditor::KEY_ENTER()); # Enter -- select
	$window->set_binding($local_exit, 274); # F10 -- cancel

	$window->modalfocus;
	return $selection;
}

sub inventory_action_ok {
	# ... TODO SAVE STEP! (ADD or EDIT, handle implicit QTY like in old version)
}

sub inventory_action_cancel {
	# TODO z NOT IMMEDIATE?
	$ercl_win->lose_focus;
	$tbl_win->focus;
}

sub inventory_update_displayed_table {
	my $stmt = $dbh->prepare("SELECT $table_ui_fields FROM inventory;");
	$stmt->execute();
	$tbl_list->values(inventory_add_table_to_list(
			$stmt->fetchall_arrayref(), \@tbl_internal_ids));
}

# $_[0] statement
# $_[1] ref internal ids
# return values array reference
sub inventory_add_table_to_list {
	my ($all_results, $internal_ids) = @_;
	# -5 for five column separator spaces
	my $wavail = $scrw - 16 - 3 - 9 - 3 - 5;
	my @colmaxlen = (16, 3, int($wavail * 1 / 3),
						int($wavail * 2 / 3), 9, 3);
	my $tbl_obj = Text::Table->new("ID", "Qty", "Class", "Thing", "Loc",
									"Imp");
	@{$internal_ids} = (undef);
	for my $rrow (@{$all_results}) {
		push @{$internal_ids}, $rrow->[0];
		my @outrow;
		for(0 .. 5) {
			my $curr = defined($rrow->[$_+1])? $rrow->[$_+1]: "";
			push @outrow, (length($curr) > $colmaxlen[$_]?
				substr($curr, 0, $colmaxlen[$_]): $curr);
		}
		$tbl_obj->add(@outrow);
	}
	return [$tbl_obj->table];
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
