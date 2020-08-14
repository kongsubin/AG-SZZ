build:
	gradle build
	unzip ./build/distributions/AG-SZZ.zip

unzip: 
	rm -rf AG-SZZ
	unzip ./build/distributions/AG-SZZ.zip

run:
	./AG-SZZ/bin/AG-SZZ $(args) 

clean:
	rm -rf AG-SZZ
	rm -rf build
