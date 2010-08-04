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

#include "dngwriter.h"

extern "C" {
#include <libavformat/avformat.h>
#include <getopt.h>
}

#include <cstdio>
#include <cstdlib>

const char* MOVIE2DNG_VERSION = "0.7";


void help(const char* program_name) {
  printf("Usage:\n\n"
         "%s [options] SOURCE DEST\n\n"
         "This program will convert the SOURCE JP4 movie to individual frames named\n"
         "DEST-NNNNNN.dng, where NNNNNN will be replaced by the frame number starting\n"
         "at 1. Note that there is no need to specify the .dng extension on the frame\n"
         "name, it will be added automatically. If you want to save frames on a different\n"
         "directory, use something like DIRECTORY/DEST, for example.\n\n"
         "[options]\n"
         "\t-k, --keep-jp4        keep intermediate JP4 frames.\n"
         "\t--shift N, --shift=N  Bayer shift, 0-3.\n"
         "\t-v, --version         display program version information.\n"
         "\t-h, --help            show this help message.\n", program_name);
}

void version() {
  printf("movie2dng %s\n"
         "Copyright (C) 2010 Paulo Henrique Silva\n"
         "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>.\n"
         "This is free software: you are free to change and redistribute it.\n"
         "There is NO WARRANTY, to the extent permitted by law.\n", MOVIE2DNG_VERSION);
}

int main (int argc, char** argv) {

  struct option cmd_options[] = {{"help", 0, NULL, 'h'},
                                 {"version", 0, NULL, 'v'},
                                 {"shift", 1, NULL, 's'},
                                 {"keep-jp4", 0, NULL, 'k'},
                                 {0, 0, 0, 0}};
  int option = 0;
  int option_index;

  int bayer_shift = -1;
  bool keep_jp4 = false;

  opterr = 0;

  while ((option = getopt_long(argc, argv, "hvk", cmd_options, &option_index)) != -1) {
    switch (option) {
    case 'h':
      help(argv[0]);
      exit(0);
    case 'v':
      version();
      exit(0);
    case 's':
      bayer_shift = atoi(optarg);
      if (bayer_shift < 0 || bayer_shift > 3) {
        printf("Ivalid shift mode, shift=[0-3]\n");
        exit(1);
      }
      break;
    case 'k':
      keep_jp4 = true;
      break;
    default:
      printf("Unknown option.\n\n");
      help(argv[0]);
      exit(1);
    }
  }

  if (argc-optind != 2) {
    help(argv[0]);
    exit(1);
  } 

  const char* sourceFilename = argv[optind];
  const char* frameName = argv[optind+1];

  // Check if sourceFilename is a movie or a single frame
  const char* jp4 = strcasestr(sourceFilename, ".jp4");
  const char* jp46 = strcasestr(sourceFilename, ".jp46");

  if (jp4 || jp46) {

    DNGWriter::write(sourceFilename, frameName, bayer_shift);

  } else {

    AVFormatContext* ctx;
    char jp4FilenameFmt[255];
    char dngFilenameFmt[255];

    snprintf(jp4FilenameFmt, 255, "%s-%%06d.jpg", frameName);
    snprintf(dngFilenameFmt, 255, "%s-%%06d.dng", frameName);

    char jp4Filename[255];
    char dngFilename[255];

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

    while (av_read_frame(ctx, &packet) >= 0) {

      snprintf(jp4Filename, 255, jp4FilenameFmt, frame);
      snprintf(dngFilename, 255, dngFilenameFmt, frame);

      FILE* fd = fopen(jp4Filename, "w");
      if (fd == NULL) {
        fprintf(stderr, "ERROR: Could not open %s for writing. Aborting...\n", jp4Filename);
        return -3;
      }

      fwrite(packet.data, packet.size, 1, fd);
      fclose(fd);

      // convert to DNG
      DNGWriter::write(jp4Filename, dngFilename, bayer_shift);

      // remove temporary jp4 file
      if (!keep_jp4)
        unlink(jp4Filename);

      av_free_packet(&packet);

      fprintf(stdout, "Converting frame %d...\r", frame);
      fflush(stdout);
 
      frame++;
    }

    fprintf(stdout, "\n");

  }
    
  return 0;

}
