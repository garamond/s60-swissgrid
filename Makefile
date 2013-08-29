OUT_DIR=build

build: clean
	mkdir $(OUT_DIR)
	javac -nowarn -source 1.3 -target 1.3 -d $(OUT_DIR) -extdirs lib src/*.java
	cp resources/* $(OUT_DIR)
	cd $(OUT_DIR) && jar cvfm SwissGrid.jar MANIFEST.MF *.class
	sed -i -e "s/\(MIDlet-Jar-Size:\).*/\1 `ls -l build/SwissGrid.jar | cut -d ' ' -f 5`/" build/SwissGrid.jad
	cd build && zip SwissGrid.zip SwissGrid.jar SwissGrid.jad && mv SwissGrid.zip ../dist

clean:
	rm -rf $(OUT_DIR)
