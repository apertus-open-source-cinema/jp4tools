<?php
/*
  Copyright 2010 Paulo Henrique Silva <ph.silva@gmail.com>

  This file is part of movie2dng.

  movie2dng is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  movie2dng is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with movie2dng.  If not, see <http://www.gnu.org/licenses/>.
*/

function help() {

$host = "http://".$_SERVER["HTTP_HOST"];
$url = $host . $_SERVER["REQUEST_URI"];
$imgsrv = $host.":8081/bimg";

echo <<<HELP
<h3>JP4 image proxy</h3>
<p>Usage:</p>
<p><a href="{$url}?url={$imgsrv}">{$url}?url={$imgsrv}</a></p>
HELP;

}

if (!array_key_exists("url", $_REQUEST)) {
  help();
  exit(1);
}

$url = $_REQUEST["url"];

$input_fp = fopen($url, "r");
if (!is_resource($input_fp))
  trigger_error("Unable to fetch image at " . $url, E_USER_ERROR);

$proc_descriptors = array(0 => array("pipe", "r"),
                          1 => array("pipe", "w"));

$proc = proc_open("movie2dng --jpeg --stdout -",
                  $proc_descriptors, $pipes, NULL, NULL, array("binary_pipes"=>TRUE));

if (!is_resource($proc))
  trigger_error("Unable to spawn JP4 proxy", E_USER_ERROR);

stream_copy_to_stream($input_fp, $pipes[0]);
fclose($pipes[0]);
fclose($input_fp);

$output = stream_get_contents($pipes[1]); 
fclose($pipes[1]);

proc_close($proc);

header("Content-type: image/jpeg");
print($output);

?>
