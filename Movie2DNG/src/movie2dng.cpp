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
#include "dngwriter.h"

extern "C" {
#include <libavformat/avformat.h>
#include <getopt.h>
#include <libgen.h>
#include <dirent.h>
#include <sys/stat.h>
}

#include <cstdio>
#include <cstdlib>

const char* MOVIE2DNG_VERSION = "0.8";


void help(const char* program_name) {
  printf("Usage:\n\n"
         "%s [options] (at least one of --dng|--jp4) SOURCE [DEST]\n\n"
         "This program will convert the SOURCE JP4 movie to individual frames named\n"
         "DEST-NNNNNN.dng, where NNNNNN will be replaced by the frame number starting\n"
         "at 1. Note that there is no need to specify the .dng extension on the frame\n"
         "name, it will be added automatically. If you want to save frames on a different\n"
         "directory, use something like DIRECTORY/DEST, for example.\n"
         "If you want to convert individual frames, pass they as SOURCE, DEST will be filled for you.\n\n"
         "[options]\n"
         "\t--jp4        save frames in JP4 format.\n"
         "\t--dng        save frames in DNG format after deblock and linearization.\n"
         "\t--gui        output information in a format suitable for a GUI program.\n"
         "\t--frames N   convert only the N-th first frames.\n"
         "\t-v, --version         display program version information.\n"
         "\t-h, --help            show this help message.\n", program_name);
}

void version(const char* program_name) {
  printf("%s %s\n"
         "Copyright (C) 2010 Paulo Henrique Silva\n"
         "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>.\n"
         "This is free software: you are free to change and redistribute it.\n"
         "There is NO WARRANTY, to the extent permitted by law.\n", program_name, MOVIE2DNG_VERSION);
}

const char CMD_JP4      = -100;
const char CMD_DNG      = -101;
const char CMD_GUI      = -102;
const char CMD_HELP     = -103;
const char CMD_VERSION  = -104;
const char CMD_N_FRAMES = -105;

int main (int argc, char** argv) {

  struct option cmd_options[] = {{"help", 0, NULL, CMD_HELP},
                                 {"version", 0, NULL, CMD_VERSION},
                                 {"jp4", 0, NULL, CMD_JP4},
                                 {"dng", 0, NULL, CMD_DNG},
                                 {"gui", 0, NULL, CMD_GUI},
                                 {"frames", 1, NULL, CMD_N_FRAMES},
                                 {0, 0, 0, 0}};
  int option = 0;
  int option_index;

  bool save_jp4 = false;
  bool save_dng = false;
  bool gui = false;
  long int n_frames = 0;

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
    case CMD_DNG:
      save_dng = true;
      break;
    case CMD_GUI:
      gui = true;
      break;
    case CMD_N_FRAMES:
      n_frames = atoi(optarg);
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

  if (!save_dng && !save_jp4) {
   help(argv[0]);
   exit(1);
  }

  // Check if sourceFilename is a movie or a single frame
  char* sourceFilename = argv[optind];

  const char* is_jp4 = strcasestr(sourceFilename, ".jp4");
  const char* is_jp46 = strcasestr(sourceFilename, ".jp46");

  char frameName[_POSIX_PATH_MAX];

  int n_args = argc-optind;

  if (n_args == 1) {
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
  char dngFilenameFmt[_POSIX_PATH_MAX];

  snprintf(jp4FilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.jp4", frameName);
  snprintf(dngFilenameFmt, _POSIX_PATH_MAX, "%s-%%06d.dng", frameName);

  char jp4Filename[_POSIX_PATH_MAX];
  char dngFilename[_POSIX_PATH_MAX];

  if (is_jp4 || is_jp46) {

      strncpy(jp4Filename, sourceFilename, _POSIX_PATH_MAX);

      char* ext = strrchr(sourceFilename, '.');
      // cut string at extension point
      if (ext) *ext=0;

      snprintf(dngFilename, _POSIX_PATH_MAX, "%s.dng", sourceFilename);

      JP4 jp4;
      jp4.open(jp4Filename);

      if (save_dng)
        DNGWriter::write(jp4, dngFilename);

  } else {

    AVFormatContext* ctx;

    av_register_all();

    if (av_open_input_file(&ctx, sourceFilename, NULL, 0, NULL) != 0) {
      fprintf(stderr, "ERROR: Cannot open file: '%s'.\n", sourceFilename);
      return -1;
    }

    if (av_find_stream_info(ctx) < 0) {
      fprintf(stderr, "ERROR: Cannot find stream info.\n");
      return -2;
    }

    AVCodecContext* codecCtx = ctx->streams[0]->codec;
    AVCodec* codec = avcodec_find_decoder(codecCtx->codec_id);
    if (!codec) {
      fprintf(stderr, "ERROR: Cannot find codec.");
      return -3;
    }

    if (avcodec_open(codecCtx, codec) < 0) {
      fprintf(stderr, "ERROR: Cannot open codec.");
      return -3;
    }

    AVPacket packet;

    int frame = 1;

    // number of frames to convert
    n_frames = n_frames? n_frames: ctx->streams[0]->nb_frames; 

    if (gui)
      fprintf(stdout, "%ld\n", n_frames);
    else
      fprintf(stdout, "#frames: %ld\n", n_frames);
    fflush(stdout);


    while (av_read_frame(ctx, &packet) >= 0 && frame <= n_frames) {

      snprintf(jp4Filename, _POSIX_PATH_MAX, jp4FilenameFmt, frame);
      snprintf(dngFilename, _POSIX_PATH_MAX, dngFilenameFmt, frame);

      FILE* fd = fopen(jp4Filename, "w");
      if (fd == NULL) {
        fprintf(stderr, "ERROR: Could not open %s for writing. Aborting...\n", jp4Filename);
        return -3;
      }

      fwrite(packet.data, packet.size, 1, fd);
      fclose(fd);

      JP4 jp4;
      jp4.open(jp4Filename);

      // convert to DNG
      if (save_dng)
        DNGWriter::write(jp4, dngFilename);

      // remove temporary jp4 file
      if (!save_jp4)
        unlink(jp4Filename);

      av_free_packet(&packet);

      if (gui)
        fprintf(stdout, "%d\n", frame);
      else
        fprintf(stdout, "Converting frame %d...\r", frame);
      fflush(stdout);
 
      frame++;
    }

    fprintf(stdout, "\n");

  }
    
  return 0;

}
