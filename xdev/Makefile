JAVAC = /usr/lib/jvm/java-8-openjdk-amd64/bin/javac
JAVA = /usr/lib/jvm/java-8-openjdk-amd64/bin/java

CPB = lib/okapibarcode.jar
CPR = $(CPB):lib/postgresql.jar:lib/h2.jar

all:
	$(JAVAC) -cp .:$(CPB) -Xlint:unchecked $$(find ma -name '*.java') \
		$$(find np -name '*.java') $$(find tornadofx -name '*.java')

test: all
	$(JAVA) -cp .:$(CPR) -Dprism.order=sw ma.inventory.Main # alt. j2d instead of sw

run:
	$(JAVA) -cp $(CPR):ma_inventory.jar -Dprism.order=sw ma.inventory.Main

jar: all
	jar cfve ma_inventory.jar ma.inventory.Main ma np tornadofx

clean:
	find ma np tornadofx -name '*.class' -delete

run-h2:
	java -jar h2.jar -url jdbc:h2:$$(pwd)/testdir/test

run-okapibarcode:
	$(JAVA) -cp $(CPB):/usr/share/java/jcommander.jar \
					uk.org.okapibarcode.OkapiBarcode $(ARGS)

run-import:
	$(JAVA) ma.inventory.plugin.isbndb.ImportISBNDB \
			/home/linux-fan/wd/dnb | pv | psql -q ma_inventory

test-export:
	cp ma/inventory/plugin/barcode/barcodes.tex x_rest/testexport
	cp -r x_rest/testexport /tmp && ./export_to_pdf.sh /tmp/testexport

test-barcode:
	cd x_rest/pous/test_barcode && $(JAVAC) \
			-cp .:../../../lib/okapibarcode.jar TestBarcode.java

test-barcode-run:
	cd x_rest/pous/test_barcode && $(JAVA) \
			-cp .:../../../lib/okapibarcode.jar TestBarcode

todo:
	grep -RF TO''DO .
