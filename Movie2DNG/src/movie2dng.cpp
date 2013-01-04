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

#include "jp4.h"
#include "movie.h"
#include "dngwriter.h"

extern "C" {
#include <getopt.h>
#include <libgen.h>
#include <dirent.h>
#include <sys/stat.h>
}

#include <cstdio>
#include <cstdlib>

const char* MOVIE2DNG_VERSION = "0.12";


void help(const char* program_name) {
  printf("Usage:\n\n"
         "%s [options] (at least one of --jp4, --jpeg, --dng or --pgm) SOURCE [DEST]\n\n"
         "This program will convert the SOURCE JP4 movie to individual frames named\n"
         "DEST-NNNNNN.dng, where NNNNNN will be replaced by the frame number starting\n"
         "at 1. Note that there is no need to specify the .dng extension on the frame\n"
         "name, it will be added automatically. If you want to save frames on a different\n"
         "directory, use something like DIRECTORY/DEST, for example.\n"
         "If you want to convert individual frames, pass they as SOURCE, DEST will be filled for you.\n\n"
         "[output formats]\n"
         "\t--jp4              save frames in JP4 format.\n"
         "\t--jpeg             save frames in JPEG format (JP4 after deblock) format.\n"
         "\t--dng              save frames in DNG format.\n"
         "\t--pgm              save frames in 16 bit PGM (ASCII) format.\n"
         "[options]\n"
         "\t--stdout           write frame data to stdout (only for single format output and JP4 or JP46 inputs).\n"
         "\t--gui              output information in a format suitable for a GUI program.\n"
         "\t--start N          convert from the N-th frame.\n"
         "\t--frames N         convert only N frames.\n"
         "\t--count-start N    override the frame numbers so that generated files will count from N instead.\n"
         "\t--shift N,         Bayer shift, 0-3 (default: detect from MakerNote).\n"
         "\t--jpeg-quality N   set --jpeg quality factor (1...100), default=100.\n"
         "\t-v, --version      display program version information.\n"
         "\t-h, --help         show this help message.\n", program_name);
}

void version(const char* program_name) {
  printf("%s %s\n"
         "Copyright (C) 2010 Paulo Henrique Silva\n"
         "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>.\n"
         "This is free software: you are free to change and redistribute it.\n"
         "There is NO WARRANTY, to the extent permitted by law.\n", program_name, MOVIE2DNG_VERSION);
}

const char CMD_JP4          = -100;
const char CMD_DNG          = -101;
const char CMD_GUI          = -102;
const char CMD_N_FRAMES     = -103;
const char CMD_HELP         = 'h';
const char CMD_VERSION      = 'v';
const char CMD_PGM          = -104;
const char CMD_STDOUT       = -105;
const char CMD_JPEG         = -106;
const char CMD_JPEG_QUALITY = -107;
const char CMD_BAYER_SHIFT  = -108;
const char CMD_START_FRAME  = -109;
const char CMD_COUNT_FRAME  = -110;

int main (int argc, char** argv) {

  struct option cmd_options[] = {{"help", 0, NULL, CMD_HELP},
                                 {"version", 0, NULL, CMD_VERSION},
                                 {"jp4", 0, NULL, CMD_JP4},
                                 {"jpeg", 0, NULL, CMD_JPEG},
                                 {"dng", 0, NULL, CMD_DNG},
                                 {"pgm", 0, NULL, CMD_PGM},
                                 {"stdout", 0, NULL, CMD_STDOUT},
                                 {"gui", 0, NULL, CMD_GUI},
                                 {"frames", 1, NULL, CMD_N_FRAMES},
                                 {"start", 1, NULL, CMD_START_FRAME},
                                 {"count-start", 1, NULL, CMD_COUNT_FRAME},
                                 {"shift", 1, NULL, CMD_BAYER_SHIFT},
                                 {"jpeg-quality", 1, NULL, CMD_JPEG_QUALITY},
                                 {0, 0, 0, 0}};
  int option = 0;
  int option_index;

  bool save_jp4 = false;
  bool save_dng = false;
  bool save_pgm = false;
  bool save_jpeg = false;

  bool save_to_stdout = false;

  bool gui = false;
  long int n_frames = 0;
  long int start_frame = 0;
  long int count_frame = -1;
  int bayer_shift = -1;
  unsigned int jpeg_quality = 100;

  opterr = 0;

  while ((option = getopt_long(argc, argv, "hv", cmd_options, &option_index)) != -1) {
    switch (option) {
    case CMD_HELP:
      help(argv[0]);
      exit(0);
    case CMD_VERSION:
      version(argv[0]);
      exit(0);
    case CMD_JP4:
      save_jp4 = true;
      break;
    case CMD_JPEG:
      save_jpeg = true;
      break;
    case CMD_JPEG_QUALITY:
      jpeg_quality = atoi(optarg);
      if (jpeg_quality < 1 || jpeg_quality > 100) {
        fprintf(stderr, "Wrong JPEG quality factor, should be between 1 and 100.\n");
        exit(1);
      }
      break;
    case CMD_DNG:
      save_dng = true;
      break;
    case CMD_PGM:
      save_pgm = true;
      break;
    case CMD_STDOUT:
      save_to_stdout = true;
      break;
    case CMD_GUI:
      gui = true;
      break;
    case CMD_N_FRAMES:
      n_frames = atoi(optarg);
      break;
    case CMD_START_FRAME:
      start_frame = atoi(optarg);
      break;
    case CMD_COUNT_FRAME:
      count_frame = atoi(optarg);
      break;
    case CMD_BAYER_SHIFT:
      bayer_shift = atoi(optarg);
      if (bayer_shift < 0 || bayer_shift > 3) {
        fprintf(stderr, "Ivalid shift mode, shift=[0-3]\n");
        exit(1);
      }
      break;
    default:
      printf("Unknown option.\n\n");
      help(argv[0]);
      exit(1);
    }
  }

  if (argc-optind < 1) {
   help(argv[0]);
   exit(1);
  }

  if (!save_dng && !save_jp4 && !save_pgm && !save_jpeg) {
   help(argv[0]);
   exit(1);
  }

  // Check if sourceFilename is a movie or a single frame
  char* sourceFilename = argv[optind];

  const char* is_jp4 = strcasestr(sourceFilename, ".jp4");
  const char* is_jp46 = strcasestr(sourceFilename, ".jp46");
  bool is_stdin = strcmp(sourceFilename, "-") == 0;

  char frameName[_POSIX_PATH_MAX];

  int n_args = argc-optind;

  if (n_args == 1 && !(is_jp4 || is_jp46) && !save_to_stdout) {
    // use sourceFilename without extension as frame name
    strncpy(frameName, sourceFilename, _POSIX_PATH_MAX);

    char* ext = strrchr(frameName, '.');
    // cut string at extension point
    if (ext) *ext=0;

    char basenameCopy[_POSIX_PATH_MAX];
    strncpy(basenameCopy, frameName, _POSIX_PATH_MAX);
    char* frameFilename = basename(basenameCopy);

    strcat(frameName, "/");
    DIR* dstDir = opendir(frameName);
    if (!dstDir) {
      int res = mkdir(frameName, 0777);
      if (res != 0) {
        fprintf(stderr, "Could not create output directory '%s' (%s).", frameName, strerror(errno));
        exit(1);
      }
    }
 
    strcat(frameName, frameFilename);

  } else if (n_args >= 2) {
    strncpy(frameName, argv[optind+1], _POSIX_PATH_MAX);
  }

  char jp4FilenameFmt[_POSIX_PATH_MAX];
  char jpegFilenameFmt[_POSIX_PATH_MAX];
  char dngFilenameFmt[_POSIX_PATH_MAX];
  char pgmFilenameFmt[_POSIX_PATH_MAX];

  snprintf(jp4FilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.jp4", frameName);
  snprintf(jpegFilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.jpeg", frameName);
  snprintf(dngFilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.dng", frameName);
  snprintf(pgmFilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.pgm", frameName);

  char jp4Filename[_POSIX_PATH_MAX];
  char jpegFilename[_POSIX_PATH_MAX];
  char dngFilename[_POSIX_PATH_MAX];
  char pgmFilename[_POSIX_PATH_MAX];

  if (is_jp4 || is_jp46 || is_stdin) {

    if ((save_dng && save_pgm) || (save_dng && save_jpeg) || (save_pgm && save_jpeg)) {
        fprintf(stderr, "Cannot choose --stdout and multiple output formats.\n");
        exit(1);
      }

      if (is_stdin)
        snprintf(sourceFilename, _POSIX_PATH_MAX, "/dev/stdin");

      strncpy(jp4Filename, sourceFilename, _POSIX_PATH_MAX);

      char* ext = strrchr(sourceFilename, '.');
      // cut string at extension point
      if (ext) *ext=0;

      if (save_to_stdout) {
        snprintf(jpegFilename,_POSIX_PATH_MAX, "/dev/stdout");
        snprintf(dngFilename, _POSIX_PATH_MAX, "/dev/stdout");
        snprintf(pgmFilename, _POSIX_PATH_MAX, "/dev/stdout");
      } else {
        snprintf(jpegFilename, _POSIX_PATH_MAX, "%s.jpeg", sourceFilename);
        snprintf(dngFilename, _POSIX_PATH_MAX, "%s.dng", sourceFilename);
        snprintf(pgmFilename, _POSIX_PATH_MAX, "%s.pgm", sourceFilename);
      }

      JP4 jp4;
      jp4.open(jp4Filename);

      if (save_dng)
        DNGWriter::write(jp4, dngFilename, bayer_shift);

      if (save_pgm)
        jp4.writePGM(pgmFilename);

      if (save_jpeg)
        jp4.writeJPEG(jpegFilename, jpeg_quality);

  } else {

    Movie movie;

    if (!movie.open(sourceFilename))
      exit(1);

    if (start_frame && start_frame > movie.nFrames()) {
      fprintf(stderr, "ERROR: start frame %ld after end frame %u.\n", start_frame, movie.nFrames());
      exit(1);
    } else
      start_frame = std::max((const long int) 1, (const long int) start_frame);

    count_frame = count_frame >= 0? count_frame : start_frame;

    // number of frames to convert. We silently crop the frame number if it goes above last frame.
    n_frames = n_frames && start_frame + n_frames <= movie.nFrames() + 1 ? n_frames: movie.nFrames() - start_frame + 1;

    if (gui)
      fprintf(stdout, "%ld\n", n_frames);
    else
      fprintf(stdout, "#frames: %ld to %ld (%ld of %u frames)\n", start_frame, start_frame + n_frames - 1, n_frames, movie.nFrames());
    fflush(stdout);

    unsigned int frame = 0;
    void* frameData = NULL;
    unsigned int frameSize = 0;

    MovieIterator it(&movie, start_frame, start_frame + n_frames - 1);

    while (it.hasNext()) {

      it.next(&frame, &frameData, &frameSize);

      snprintf(jp4Filename, _POSIX_PATH_MAX, jp4FilenameFmt, count_frame);
      snprintf(jpegFilename, _POSIX_PATH_MAX, jpegFilenameFmt, count_frame);
      snprintf(dngFilename, _POSIX_PATH_MAX, dngFilenameFmt, count_frame);
      snprintf(pgmFilename, _POSIX_PATH_MAX, pgmFilenameFmt, count_frame);

      FILE* fd = fopen(jp4Filename, "w");
      if (fd == NULL) {
        fprintf(stderr, "ERROR: Could not open %s for writing. Aborting...\n", jp4Filename);
        return -3;
      }

      fwrite(frameData, frameSize, 1, fd);
      fclose(fd);

      JP4 jp4;
      jp4.open(jp4Filename);

      // convert to DNG
      if (save_dng)
        DNGWriter::write(jp4, dngFilename, bayer_shift);

      if (save_pgm)
        jp4.writePGM(pgmFilename);

      if (save_jpeg)
        jp4.writeJPEG(jpegFilename, jpeg_quality);

      // remove temporary jp4 file
      if (!save_jp4)
        unlink(jp4Filename);

      if (gui)
        fprintf(stdout, "%d\n", frame);
      else if (frame == count_frame)
        fprintf(stdout, "Converting frame %d...\r", frame);
      else
        fprintf(stdout, "Converting frame %d, renumbered as %ld...\r", frame, count_frame);
      fflush(stdout);

      count_frame++;
    }

    fprintf(stdout, "\n");

  }
    
  return 0;

}
