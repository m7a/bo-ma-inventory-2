BEGIN TRANSACTION;

CREATE SEQUENCE items_seq;

CREATE TABLE items (
	iid            BIGINT        PRIMARY KEY,
	kind           INTEGER       NOT NULL,
	qty            INTEGER       NOT NULL,
	userSuppliedId VARCHAR(128),
);

CREATE SEQUENCE inventory_dates_seq START WITH 1;

CREATE TABLE IF NOT EXISTS inventory_dates (
	idid INTEGER  PRIMARY KEY,
	t    DATETIME NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS item_fields (
	iid  BIGINT  NOT NULL,
	idid INTEGER NOT NULL,
	k    INTEGER NOT NULL,
	v    TEXT,

	FOREIGN KEY(idid) REFERENCES inventory_dates(idid),
	FOREIGN KEY(iid) REFERENCES items(iid),
	PRIMARY KEY(iid, idid, k)
);

INSERT INTO inventory_dates(idid, t) VALUES (0, NOW());

-- a sequence starting beyond the last EAN for safety
CREATE SEQUENCE masysma_id_seq START WITH 1040000000000001;

COMMIT;
