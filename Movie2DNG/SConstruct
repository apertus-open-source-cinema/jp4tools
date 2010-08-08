import os
from os import environ as env
from sys import platform, byteorder
from glob import glob

# command line var=value variables
ARCH_CFLAGS = ARGUMENTS.get("ARCH", "-O3 -g3")
PREFIX = ARGUMENTS.get("PREFIX", "/usr")
bin_DIR = os.path.join(PREFIX, "bin")

# system dependent variables
if platform == "darwin":
    env = Environment(ENV=env)
    env.Append(CPPDEFINES=["qMacOS=1", "qWinOS=0", "MAC_ENV=1"])
    env.Append(LINKFLAGS=["-framework", "CoreServices"])
elif platform == "linux2":
    env = Environment(ENV=env)
    env.Append(CPPDEFINES=["qMacOS=0", "qWinOS=0", "UNIX_ENV=1"])
elif platform == "win32":
    env = Environment(ENV=env, TOOLS=['mingw'])
    env.Append(CPPDEFINES=["qMacOS=0", "qWinOS=0", "UNIX_ENV=1"])
    env.Append(CPPPATH=["c:\elphel\expat\Source\lib"])
    env.Append(LIBPATH=["c:\elphel\expat\Bin"])
else:
    print "Unknwon platform."
    Exit(2)

if byteorder == "big":
    env.Append(CPPDEFINES=["qDNGBigEndian=1"])
else:
    env.Append(CPPDEFINES=["qDNGLittleEndian=1"])

# system independent variables
env.MergeFlags(ARCH_CFLAGS)
env.Prepend(CPPPATH=["extra/jpeg-6b-jp4", "extra/dng_sdk", "extra/xmp_sdk/include", "extra/xmp_sdk/common", "extra/md5"])
env.Append(LIBPATH=["#"])

# ffmpeg (libraries are added by hand)
env.ParseConfig("pkg-config --cflags --libs-only-L libavformat")
env.ParseConfig("pkg-config --cflags --libs-only-L libavcodec")
env.ParseConfig("pkg-config --cflags --libs libexif")

# DNG SDK
dng_SRC  = glob("extra/dng_sdk/*.cpp")
xmp_SRC  = ["extra/xmp_sdk/common/UnicodeConversions.cpp", "extra/xmp_sdk/common/XML_Node.cpp"]
xmp_SRC += glob("extra/xmp_sdk/XMPCore/*.cpp")
md5_SRC  = glob("extra/md5/*.cpp")

env.StaticLibrary("dngsdk", source=dng_SRC+xmp_SRC+md5_SRC)

#env.Append(CPPDEFINES=["qDNGValidateTarget=1"])
#env.Program("dng_validate", source=["tests/dng_validate.cpp"], LIBS=["libdngsdk", "libexpat"])

# JP4 modified libjpeg
jpeg_SRC = Split("jcapimin.c jcapistd.c jccoefct.c jccolor.c jcdctmgr.c jchuff.c "
                 "jcinit.c jcmainct.c jcmarker.c jcmaster.c jcomapi.c jcparam.c "
                 "jcphuff.c jcprepct.c jcsample.c jctrans.c jdapimin.c jdapistd.c "
                 "jdatadst.c jdatasrc.c jdcoefct.c jdcolor.c jddctmgr.c jdhuff.c "
                 "jdinput.c jdmainct.c jdmarker.c jdmaster.c jdmerge.c jdphuff.c "
                 "jdpostct.c jdsample.c jdtrans.c jerror.c jfdctflt.c jfdctfst.c "
                 "jfdctint.c jidctflt.c jidctfst.c jidctint.c jidctred.c jquant1.c "
                 "jquant2.c jutils.c jmemmgr.c jmemnobs.c")

jpeg_SRC = ["extra/jpeg-6b-jp4/%s" % src for src in jpeg_SRC]

# JP4
jp4_SRC = ["src/jp4.cpp"] + jpeg_SRC
env.StaticLibrary("jp4", source=jp4_SRC)

# movie2dng
movie2dng_SRC = ["src/movie2dng.cpp", "src/dngwriter.cpp"]

movieEnv = env.Clone()
movieEnv.MergeFlags("-Wall -Wextra -g3")
movieEnv.MergeFlags(ARCH_CFLAGS)
movieEnv.MergeFlags("-Wl,-rpath=%s" % movieEnv.Dir("#").abspath)
movie2dng = movieEnv.Program("movie2dng", source=movie2dng_SRC,
                             LIBS=["libjp4", "libdngsdk", "libavformat", "libavcodec", "libexpat", "libexif", "libpthread"])

movieEnv.Install(bin_DIR, movie2dng)
movieEnv.Alias("install", bin_DIR)
