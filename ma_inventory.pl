#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use utf8;
use strict;
use warnings FATAL => 'all';
use autodie;

require DBI;                 # libdbd-sqlite3-perl
require Text::Table;         # libtext-table-perl
require Barcode::DataMatrix; # libbarcode-datamatrix-perl

use Try::Tiny;
use File::Basename;
use Data::Dumper 'Dumper'; # debug only

use lib dirname(__FILE__); # load locally changed modules below...
require Curses::UI; # libcurses-ui-perl

# -- constant --
my @kinputs = qw(id_string quantity class thing location t0 origin importance comments);
my $table_ui_fields = "id_internal, id_string, quantity, class, thing, ".
						"location, importance";
my $padding     = 14;           # checkbox offset
my $offset      = $padding + 5; # label offset
my $field_start = $offset + 11; # field offset

################################################################################
## SHARED APPLICATION STATE ####################################################
################################################################################

# -- DBI connection --
my $dbh;

# -- data --
my @tbl_internal_ids; # array of assoc internal ids
my $is_in_normal_mode = 0; # bool

# -- UI --
my $curses;       # Curses UI
my $scrh;         # integer
my $scrw;         # integer
my $tbl_list;     # Listbox table
my $ercl_win;     # Window
my $tbl_win;      # Window
my $lbl_status;   # Label
my $enter_option; # Popupmenu

# -- current edit --
my %inputs = (
	id_string  => { label => "ID",         nocheckbox   => 1 },
	quantity   => { label => "Quantity",   nocheckbox   => 1 },
	class      => { label => "Class",      autocomplete => 1 },
	thing      => { label => "Thing",      autocomplete => 1 },
	location   => { label => "Location",   autocomplete => 1 },
	t0         => { label => "T0",         autocomplete => 1 },
	origin     => { label => "Origin",     autocomplete => 1 },
	importance => { label => "Importance", autocomplete => 1 },
	comments   => { label => "Comments",   height       => 4 }
);
my $cedit_internal_id = -1;
my $cedit_changed = 0;
my $drop_next_enter = 0; # required to cut off trailing \n from scanner...

################################################################################
## MAIN AND INIT ###############################################################
################################################################################

sub inventory_main {
	inventory_init(@_);
	inventory_draw_window_tbl();
	inventory_add_tbl_win_bindings();
	inventory_draw_window_ercl();
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
		{ sqlite_unicode => 1, AutoCommit => 1, RaiseError => 1 });
	$curses = Curses::UI->new(-clear_on_exit => 1);
	$scrh = $curses->height;
	$scrw = $curses->width;
}

################################################################################
## WINDOW DEFINITION: TABLE WINDOW / MAIN SCREEN ###############################
################################################################################

sub inventory_draw_window_tbl {
	$tbl_win = $curses->add("win_table", "Window", -border => 0);
	$tbl_win->add("lb_fnum", "Label", -y => $scrh - 1, -x => 0,
		-text => "1 GenID 2 ARCL                                 ".
						"8 Delete         0 Exit");
	$tbl_list = $tbl_win->add("table", "Listbox", -y => 0,
				-values => ["(...)"], -height => $scrh - 1);
}

sub inventory_add_tbl_win_bindings {
	$tbl_win->set_binding(sub {
		if($drop_next_enter) {
			$drop_next_enter = 0;
			return;
		}
		my $selidx = $tbl_list->get_active_id;
		if(defined($tbl_internal_ids[$selidx]) and
				inventory_edit_id($tbl_internal_ids[$selidx])) {
			$cedit_changed = 0;
			inventory_display_editrcl();
		}
	}, Curses::UI::TextEditor::KEY_ENTER()); # Enter -- Edit
	$tbl_win->set_binding(\&inventory_generate_ids, 265); # F1 -- generate
	$tbl_win->set_binding(sub {
		$drop_next_enter = 0;
		$cedit_changed = 0;
		inventory_action_reset();
		inventory_display_editrcl();
	}, 266); # F2 -- add
	$tbl_win->set_binding(\&inventory_action_delete, 272); # F8 -- delete
	$tbl_win->set_binding(sub {
		$drop_next_enter = 0;
		$curses->mainloopExit;
	}, 274); # F10 -- exit
}

sub inventory_display_editrcl {
	$tbl_win->lose_focus;
	$ercl_win->focus;
}

################################################################################
## WINDOW DEFINITION: EDIT/RECALL SCREEN #######################################
################################################################################

sub inventory_draw_window_ercl {
	$ercl_win = $curses->add("win_edit_rcl", "Window", -border => 0);
	inventory_draw_codes($ercl_win);

	$ercl_win->add("lb_enter_option", "Label", -text => "On Enter",
					-y => 0, -x => $offset);
	$ercl_win->add("status", "Label", -text => "Status", -y => 2,
								-x => $offset);
	my $line_width = $scrw - $field_start - $padding;
	$lbl_status = $ercl_win->add("status_cnt", "Label",
		-width => $line_width, -text => "unknown", -y => 2,
		-x => $field_start);

	my $cy = inventory_draw_inputs($line_width);

	# only display + enable auto-complete field if at least space for three
	# entries
	my $autocomplete_is_available = ($cy + 8) < $scrh;
	inventory_add_autocomplete($cy) if($autocomplete_is_available);

	# add enter handler for remaining entries and all entries if no
	# autocomplete enabled.
	inventory_add_enter_handler($_, undef)
		for(grep { (not $inputs{$_}->{autocomplete} or
		            not $autocomplete_is_available) } @kinputs);
}

# $_[0] line width
sub inventory_draw_inputs {
	my $line_width = shift;
	# two loops to have a sensible tabindex order
	my $cy = 2;
	for my $key (@kinputs) {
		my $val = $inputs{$key};
		$cy += 2;
		$ercl_win->add("lbl_input_".$key, "Label", -x => $offset,
					-y => $cy, -text => $val->{label});
		my $height = $val->{height} // 1;
		$val->{text} = $ercl_win->add("text_input_".$key, "TextEditor",
			-y => $cy, -x => $field_start, -showlines => 1,
			-width => $line_width,
			-singleline => ($height == 1), -height => $height);
	}
	$cy = 2;
	for my $key (@kinputs) {
		my $val = $inputs{$key};
		$cy += 2;
		$val->{checkbox} = $ercl_win->add("chck_input_".$key,
					"Checkbox", -y => $cy, -x => $padding)
					unless(defined($val->{nocheckbox}));
	}

	# -- Enter options --
	# Auto  if in ID          -> RCL
	#       if in other field -> autocomplete if selection or ignore
	# OK                      -> OK
	# Nothing                 -> ignore
	# Recall                  -> RCL
	# Autocomplete            -> autocomplete use first if nothing selected
	$enter_option = $ercl_win->add("enter_option", "Popupmenu",
		-y => 0, -x => $field_start, -selected => 0,
		-values => ["Auto", "Recall", "OK", "Autocomplete", "Nothing"]);

	return $cy;
}

sub inventory_add_ercl_win_bindings {
	$ercl_win->set_binding(\&inventory_action_ok,     268); # F4  -- OK
	$ercl_win->set_binding(\&inventory_action_recall, 271); # F7  -- recall
	$ercl_win->set_binding(\&inventory_action_reset,  273); # F9  -- reset
	$ercl_win->set_binding(\&inventory_action_cancel, 274); # F10 -- exit
	$ercl_win->set_routine("inventory_tmp_key_esc",
				\&inventory_temporary_default_binding_escape);
	$ercl_win->set_binding(sub {
		$drop_next_enter = 0;
		if($is_in_normal_mode) {
			$is_in_normal_mode = 0;
			$ercl_win->clear_binding("inventory_tmp_key_esc");
		} else {
			$is_in_normal_mode = 1;
			$ercl_win->set_binding("inventory_tmp_key_esc", "");
		}
	}, Curses::UI::Common::CUI_ESCAPE()); # Escape (barcode sequence)
}

################################################################################
## AUTOCOMPLETE ################################################################
################################################################################

# $_[0] current y
sub inventory_add_autocomplete {
	my $cy = shift;
	my $autocomplete_height = $scrh - $cy - 5;
	my $autocomplete_list = $ercl_win->add("autocomplete_list", "Listbox",
			-radio => 1, values => [], -selected => 0,
			-y => $cy + 5, -x => $padding,
			-width => $scrw - $padding - 16,
			-height => $autocomplete_height);
	# last: keep track of which field last did some changes to the
	#       autocomplete list. We identify this by the fields name, space
	#       and then the current query text.
	my $last = "";
	for my $key (@kinputs) {
		next unless defined($inputs{$key}->{autocomplete});
		my $field = $inputs{$key}->{text};
		$field->onChange(sub {
			my $val = $field->get();
			my $curq = $key." ".$val;
			return if($curq eq $last);
			# cannot use prepared statement here because
			# it will interpret key as string rather than
			# table name...
			my $stmt = $dbh->prepare(
				"SELECT DISTINCT $key FROM inventory ".
				"WHERE $key LIKE ? ORDER BY $key ASC LIMIT ?"
			);
			$stmt->execute($val."%", $autocomplete_height);
			my $results = [];
			while((my @row = $stmt->fetchrow_array) != 0) {
				push @{$results}, $row[0];
			}
			$autocomplete_list->values($results);
			$autocomplete_list->process_bindings("1");
			$autocomplete_list->draw();
			$last = $curq;
		});
		$field->set_binding(sub {
			# up (go upwards, select)
			$drop_next_enter = 0;
			$autocomplete_list->process_bindings("k");
			$autocomplete_list->process_bindings("1");
		}, Curses::UI::TextEditor::KEY_UP());
		$field->set_binding(sub {
			# down (go downwards, select)
			$drop_next_enter = 0;
			$autocomplete_list->process_bindings("j");
			$autocomplete_list->process_bindings("1");
		}, Curses::UI::TextEditor::KEY_DOWN());
		inventory_add_enter_handler($key, sub {
			$field->text($autocomplete_list->get_active_value());
		});
	}
}

################################################################################
## APPLICATION LOGIC / INVENTORY ACTIONS #######################################
################################################################################

sub inventory_action_delete {
	$drop_next_enter = 0;
	my $id_to_delete = $tbl_internal_ids[$tbl_list->get_active_id];
	return unless defined $id_to_delete;
	my $stmt = $dbh->prepare("DELETE FROM inventory WHERE id_internal = ?");
	$stmt->execute($id_to_delete);
	inventory_update_displayed_table();
}

sub inventory_action_reset {
	# clear form and disassociate from edit.
	# keep enter option and reset all other values independently of their
	# checkboxes.
	$drop_next_enter = 0;
	$inputs{$_}->{text}->text("") for (@kinputs);
	inventory_disassociate();
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
		$stmt->execute(map { $inputs{$_}->{text}->get()."%" }
				qw(class thing location origin importance));
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

sub inventory_action_ok {
	$drop_next_enter = 0;

	my @args = map {
		my $val = $inputs{$_}->{text}->get;
		$val eq ""? undef: $val;
	} @kinputs;

	my $stmt;
	if($cedit_internal_id == -1) {
		$stmt = $dbh->prepare(
			"INSERT INTO inventory (id_string, quantity, checked, ".
					"class, thing, location, t0, origin, ".
					"importance, comments) ".
			"VALUES (?, ?, datetime('now', 'utc'), ".
							"?, ?, ?, ?, ?, ?, ?)"
		);
	} else {
		$stmt = $dbh->prepare(
			"UPDATE inventory SET id_string = ?, quantity = ?, ".
				"checked = datetime('now', 'utc'), class = ?, ".
				"thing = ?, location = ?, t0 = ?, origin = ?, ".
				"importance = ?, comments = ? ".
			"WHERE id_internal = ?"
		);
		push @args, $cedit_internal_id;
	}

	$stmt->execute(@args);

	for (@kinputs) {
		$inputs{$_}->{text}->text("") if($inputs{$_}->{nocheckbox} or
					not $inputs{$_}->{checkbox}->get());
	}
	inventory_disassociate();

	$cedit_changed = 1;
}

sub inventory_action_cancel {
	$drop_next_enter = 0;
	$ercl_win->lose_focus;
	inventory_update_displayed_table() if($cedit_changed);
	$tbl_win->focus;
}

sub inventory_disassociate {
	$cedit_internal_id = -1;
	$lbl_status->text("DISSOC dID=$cedit_internal_id iQTY=0");
	$inputs{id_string}->{text}->focus();
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
	for (@kinputs) {
		my $val = $row->{$_} // "";
		# Set quantity to 1 if input quantity is 0 as to allow add
		# function by disambiguation of already existent prepared
		# entries.
		$val = 1 if($_ eq "quantity" and $val == 0);
		$inputs{$_}->{text}->text($val);
	}
	$lbl_status->text("ASSOC  dID=$cedit_internal_id ".
						"iQTY=$row->{quantity}");
	$inputs{id_string}->{text}->focus();
	return 1;
}

################################################################################
## ENTER HANDLING ##############################################################
################################################################################

# $_[0]: key
# $_[1]: autocomplete procedure [may be undef]
sub inventory_add_enter_handler {
	my ($key, $autocomplete) = @_;
	$inputs{$key}->{text}->set_binding(sub {
		if($drop_next_enter) {
			$drop_next_enter = 0;
			return;
		}
		my $eo = $enter_option->get();
		if($eo eq "Auto") {
			if($key eq "id_string") {
				inventory_action_recall();
			} elsif(defined $autocomplete) {
				$autocomplete->();
			}
		} elsif($eo eq "OK") {
			inventory_action_ok();
		} elsif($eo eq "Recall") {
			inventory_action_recall();
		} elsif($eo eq "Autocomplete" and defined($autocomplete)) {
			$autocomplete->();
		}
	}, Curses::UI::TextEditor::KEY_ENTER());
}

################################################################################
## DISAMBIGUATE SCREEN #########################################################
################################################################################

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

################################################################################
## INVENTORY DISPLAYED TABLE MANAGEMENT ########################################
################################################################################

sub inventory_update_displayed_table {
	my $stmt = $dbh->prepare("SELECT $table_ui_fields FROM inventory;");
	$stmt->execute();
	$tbl_list->values(inventory_add_table_to_list(
			$stmt->fetchall_arrayref(), \@tbl_internal_ids));
	$tbl_list->draw();
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

################################################################################
## BARCODE USER INTERFACE ######################################################
################################################################################

sub inventory_draw_codes {
	my $ercl_win = shift;
	my @codes = (
		# label 0         y 1        x 2         ycode 3    value 4
		[" Reset (F9)",   6,         0,          0,         "\033r"],
		[" Recall (F7)",  6,         $scrw - 12, 0,         "\033d"],
		["Cancel (F10)",  $scrh - 7, 0,          $scrh - 6, "\033c"],
		["   OK (F4)",    $scrh - 7, $scrw - 12, $scrh - 6, "\033o"]
	);

	my @half_codes = (" ", "▗", "▖", "▃", "▝", "▐", "▞", "▟", "▘", "▚", "▌",
						"▙", "▀", "▜", "▛", "█");
	for my $code (@codes) {
		$ercl_win->add("lb_gen_$code->[1]_$code->[2]", "Label",
					-text => $code->[0],
					-y => $code->[1], -x => $code->[2]);
		my $y = $code->[3];
		my $data = Barcode::DataMatrix->new->barcode($code->[4]);
		my $width = @{@{$data}[0]} * 2 + 4;
		my @drawd = ([(0) x $width]);
		for my $row (@$data) {
			push @drawd, [0, 0, (map { ($_, $_) } @$row), 0, 0];
		}
		push @drawd, [(0) x $width];
		for(my $i = 0; $i < @drawd; $i += 2) {
			my $line = "";
			for(my $j = 0; $j < $width; $j += 2) {
				my $idx =
					($drawd[$i    ]->[$j    ] << 3) |
					($drawd[$i    ]->[$j + 1] << 2) |
					($drawd[$i + 1]->[$j    ] << 1) |
					($drawd[$i + 1]->[$j + 1]);
				# invert for black font on white background
				$idx = ~$idx;
				$line .= $half_codes[$idx];
			}
			$ercl_win->add("lb_gen2_${y}_$code->[3]_$code->[2]",
					"Label", -text => $line, -y => $y,
					-x => $code->[2]);
			$y++;
		}
	}
}

sub inventory_temporary_default_binding_escape {
	my ($_binding, $key, @_extra) = @_; # Widget.pm~977
	$is_in_normal_mode = 0;
	$ercl_win->clear_binding("inventory_tmp_key_esc");
	$ercl_win->set_routine("inventory_tmp_stop_drop",
			\&inventory_temporary_default_binding_stop_drop);
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

	$drop_next_enter = 1;
}

sub inventory_temporary_default_binding_stop_drop {
	my ($_binding, $key, @_extra) = @_; # Widget.pm~977
	$drop_next_enter = 0;
	$ercl_win->clear_binding("inventory_tmp_stop_drop");
	$ercl_win->process_bindings($key);
}

################################################################################
## GENERATE IDS ################################################################
################################################################################

sub inventory_generate_ids {
	my $num_to_gen = 128;

	my $stmt = $dbh->prepare("SELECT id_string FROM inventory ".
		"WHERE length(id_string) = 16 ORDER BY id_string DESC LIMIT 1");
	$stmt->execute();
	my @result = $stmt->fetchrow_array();
	my $current_max_id = $result[0];

	my $dialog = $tbl_win->add("dialog_generate", "Dialog::Basic",
		-message => "Generate 128 IDs from $current_max_id exclusive?",
		-buttons => ["yes", "no"]);
	$dialog->modalfocus;
	my $answer = $dialog->get();
	$tbl_win->delete("dialog_generate");
	$curses->leave_curses();
	if($answer) {
		$stmt = $dbh->prepare("INSERT INTO inventory ".
				"(id_string, quantity) VALUES ".
				("(?, 0),") x ($num_to_gen - 1)." (?, 0);");
		my @ids = ();
		for(my $i = 1; $i <= $num_to_gen; $i++) {
			push @ids, $current_max_id + $i;
		}
		$stmt->execute(@ids);
		inventory_update_displayed_table;
	} else {
		$tbl_win->draw();
	}
}

################################################################################
## END OF PROCEDURES / EXEC TO MAIN ############################################
################################################################################

inventory_main @ARGV;
