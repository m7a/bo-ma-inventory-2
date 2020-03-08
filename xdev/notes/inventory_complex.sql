BEGIN TRANSACTION;

CREATE TABLE fields (
	fid      INTEGER      PRIMARY KEY,
	internal VARCHAR(64)  UNIQUE,
	visual   VARCHAR(128) NOT NULL
);

CREATE TABLE field_equivalence (
	fidl INTEGER REFERENCES fields.fid,
	fidr INTEGER REFERENCES fields.fid
);

CREATE TABLE inventory_types (
	itid INTEGER PRIMARY KEY,
	visual VARCHAR(64) UNIQUE,
);

CREATE TABLE inventory_type_fields (
	itid INTEGER REFERENCES inventory_types.itid,
	fid INTEGER REFERENCES fidlds.fid,
	PRIMARY KEY(itid, fid)
);

CREATE TABLE inventory_items (
	iid LONG PRIMARY KEY,
	itid INTEGER REFERENCES inventory_types.itid,
	qty INTEGER NOT NULL,
	-- common other?
);

CREATE TABLE inventory_items_fields (
	iid LONG REFERENCES inventory_items.iid,
	fid INTEGER REFERENCES fields.fid,
	idid INTEGER REFERENCES inventory_dates.idid,
	value VARCHAR(256) NOT NULL,
	PRIMARY KEY(iid, fid)
);

CREATE TABLE inventory_dates (
	idid INTEGER PRIMARY KEY
	-- ...
);

COMMIT;
