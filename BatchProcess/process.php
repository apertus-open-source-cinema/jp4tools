<?

if ($handle = opendir('.')) {
	/* This is the correct way to loop over the directory. */
	while (false !== ($file = readdir($handle))) {
		if (strpos($file, ".mov")) {
		//move each quicktime *.mov into its own folder
		$dir_name = substr($file,0, strlen($file)-4);
		mkdir($dir_name);
		echo "created directory: ".$dir_name."\n";
		exec("mv ".$file." ".$dir_name."/");

		$JP4SOURCEFOLDER = $dir_name."/jp4s/";
		$DNGTARGETFOLDER = $dir_name."/dngs/";

		// create folders
		if (!file_exists($JP4SOURCEFOLDER)) {
			exec("mkdir ".$JP4SOURCEFOLDER);
			echo "created directory: : ".$JP4SOURCEFOLDER."\n";
		}
		if (!file_exists($DNGTARGETFOLDER)) {
			exec("mkdir ".$DNGTARGETFOLDER);
			echo "created directory: : ".$DNGTARGETFOLDER."\n";
		}
		
		// convert mov to jpegs
		echo $command = "./movie2dng ".$dir_name."/".$file." ".$dir_name."/".$file."-%05d.jpg\n";
		exec($command);

		// move jpegs into jp4s/ dir
		exec("mv ".$dir_name."/".$file."*.jpg ".$JP4SOURCEFOLDER);

		// convert jp4s to DNG
		$dc = opendir($JP4SOURCEFOLDER);
		while ($fn = readdir($dc)) {
			$dng_filename = preg_replace("/\\.[^.\\s]{3,4}$/", "", $fn).".dng";
			$command = "./elphel_dng 100 ".$JP4SOURCEFOLDER.$fn." ".$DNGTARGETFOLDER.$dng_filename."\n";
			echo $command;
			exec($command);
		}

		// remove jp4s
		exec("rm ".$JP4SOURCEFOLDER." -r");
		}
	}
	closedir($handle);
}

?>
