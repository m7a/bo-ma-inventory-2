package ma.inventory.plugin.csv.import2;

import ma.inventory.plugin.csv.shr.CSVConf;

import ma.inventory.m.Checkpoint;

class CSVImportConf {

	final CSVConf general;
	final Checkpoint inventoryDateSelected;

	CSVImportConf(CSVConf general, Checkpoint inventoryDateSelected) {
		super();
		this.general = general;
		this.inventoryDateSelected = inventoryDateSelected;
	}

}
