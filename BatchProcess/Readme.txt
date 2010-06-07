process.php can be used to convert lots of JP46 shot quicktime moves into dng sequences when run from the commandline: "php process.php"

Requirements:
=============
2 binary files are required in the same directory as process.php:
elphel_dng
movie2dng


What the script does:
=============
-) Read the current directory 
-) Create a new directory for every quicktime *.mov found in the current directory with the same name as the *.mov
-) Move the *.mov into the newly created subdirectory
-) create 2 folders inside each movs subdirectory called jp4s/ and dngs/
-) extract all frames from the *.mov as jpeg sequence into the jp4s/ directory
-) convert each jpeg from the jp4s/ directory to a dng into dngs/
-) remove the jp4s/ folder 
