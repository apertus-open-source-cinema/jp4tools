.PHONY: clean all

elphel-movie-indexer: elphel-movie-indexer.cpp
	g++ -o $@ $? `pkg-config --libs --cflags libavcodec` `pkg-config --libs --cflags libavformat`

clean:
	rm -f elphel-movie-indexer

all: elphel-movie-indexer

