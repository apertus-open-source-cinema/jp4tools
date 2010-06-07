Despite the name this app is not yet capable of doing a full convert to DNG, currently it can convert quicktime *.mov to a jpeg sequence as first step.

This program uses ffmpeg to parse the movie file then saves raw frames

Building:
=========
It uses scons to he build, try writing scons on you shell and Ubuntu
will tell you how to install if you don't have it.

You'll also need ffmpeg installed, libavcodec-dev and libavformat-dev
packages on Ubuntu do the job.

just type "scons" and you'll end up with a movi2dng executable.

Usage:
======
./movie2dng filename.mov some-fancy-frame-%05d.jpg

And you'll have a bunch of fancy frames :)
