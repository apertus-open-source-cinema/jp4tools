import sys
from glob import glob

env = Environment()

env.MergeFlags("-Wall -Wextra -O3 -ffast-math")

movie2dng_LIBS = []

if sys.platform == "darwin":
   env.Prepend(CPPPATH=["/opt/local/include"])
   env.Append(LIBPATH=["/opt/local/lib", "."])
   env.Append(LIBS=["avformat", "avcodec"])
   
else:
   env.MergeFlags(["!pkg-config --libs --cflags libavcodec libavformat"])

movie2dng_SRC = ["movie2dng.cpp"]

env.Program("movie2dng", source=movie2dng_SRC)

