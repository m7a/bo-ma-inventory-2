#!/usr/bin/perl
# Ma_Sys.ma Inventory 2.0.0, Copyright (c) 2020 Ma_Sys.ma.
# For further info send an e-mail to Ma_Sys.ma@web.de.

use strict;
use warnings FATAL => 'all';
use autodie;

require DBI;       # libdbd-sqlite3-perl
require Text::CSV; # libtext-csv-perl
#use Data::Dumper 'Dumper'; # debug only

if($#ARGV < 0 or $ARGV[0] eq "--help") {
	print "USAGE ma_inventory_init DBFILE [CSVFILE]\n";
	exit(0);
}

my $dbfile = $ARGV[0];
die("ERROR: Database $dbfile already exists. Will not overwrite.\n")
								if(-f $dbfile);
my $dbh = DBI->connect("dbi:SQLite:dbname=$dbfile", "", "",
		{ sqlite_unicode => 1, AutoCommit => 0, RaiseError => 1 });
# This schema is entirely non-normalized for the purpose of being super easy
# to process and to aid editing the database directly.
# PRIMARY KEY implies AUTOINCREMENT here.
$dbh->do(<<~EOF);
	CREATE TABLE inventory (
		id_internal INTEGER      PRIMARY KEY,
		id_string   VARCHAR(128) UNIQUE,
		quantity    INT          NOT NULL,
		checked     DATETIME,
		class       VARCHAR(128),
		thing       VARCHAR(128),
		location    VARCHAR(128),
		t0          VARCHAR( 32),
		origin      VARCHAR( 64),
		importance  VARCHAR( 32),
		comments    VARCHAR(256)
	);
	EOF

if($#ARGV eq 1) { # parameter CSVFILE is present
	my $stmt = $dbh->prepare(<<~EOF);
		INSERT INTO inventory
			(id_string, quantity, checked, class, thing,
			location, t0, origin, importance, comments)
		VALUES
			(:id_string, :quantity, NULL, :class, :thing,
			:location, :t0, :origin, :importance, :comments);
		EOF

	my $csv = Text::CSV->new({sep_char => ";", quote_char => undef});
	open(my $fh, "<:encoding(UTF-8)", $ARGV[1]);
	my $linenumber = 0;
	while(my $line = <$fh>) {
		die("ERROR: Failed to parse line [$line]\n")
						if(not $csv->parse($line));
		my @fields = $csv->fields;
		# process line if it is not the header line
		# header line := is line number 0 and has
		#                "ID or ISBN" as first field
		if($linenumber != 0 or $fields[0] ne "ID or ISBN") {
			# CSV line format
			# [ 0]  ID or ISBN
			# [ 1]  Quantity
			# [ 2]  Type        (Book|Other)
			# -- if Book --
			# [ 3]  Author      [ 4]  Title     [ 5]  Year
			# [ 6]  Publisher   [ 7]  Pub.Loc.  [ 8]  Pages
			# -- general --
			# [ 9]  Class       [10]  Thing     [11]  Name
			# [12]  Location    [13]  T0        [14]  Origin
			# [15]  Importance  [16]  Comments

			$stmt->bind_param(":id_string", $fields[0]);
			$stmt->bind_param(":quantity",  $fields[1]);
			$stmt->bind_param(":class", $fields[9] ne ""?
					$fields[9]:
					($fields[2] eq "Book"? "Book": undef));
			$stmt->bind_param(":thing", $fields[10] ne ""?
					$fields[10]: ($fields[2] eq "Book"?
					("$fields[3]: $fields[4], ".
					"$fields[6] $fields[5]"): undef));

			my $fieldidx = 12;
			for my $i ("location", "t0", "origin",
						"importance", "comments") {
				$stmt->bind_param(":".$i,
					(defined($fields[$fieldidx]) and
					$fields[$fieldidx] ne "")?
					$fields[$fieldidx]: undef);
				$fieldidx++;
			}

			$stmt->execute;
		}
		$linenumber++;
	}
	close($fh);
	$dbh->commit();
}

$dbh->disconnect;
