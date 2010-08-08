 /*
  Copyright 2010 Paulo Henrique Silva <ph.silva@gmail.com>

  This file is part of elphel-tools.

  elphel-tools is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  elphel-tools is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with elphel-tools.  If not, see <http://www.gnu.org/licenses/>.
*/

extern "C" {
#include <libavformat/avformat.h>
}

#include <cstdio>

#include <libgen.h>


int main (int argc, char** argv) {

  if (argc < 2) {
    fprintf(stderr, "Usage: %s movie.mov\n", argv[0]);
    exit(1);
  }

  // make a copies because basename/dirname changes its argument
  char source[_POSIX_PATH_MAX];

  strcpy(source, argv[1]);
  char* filename = basename(source);

  strcpy(source, argv[1]);
  char* directory = dirname(source);

  char real_directory[_POSIX_PATH_MAX];
  realpath(directory, real_directory);

  AVFormatContext* ctx;

  av_register_all();

  if (av_open_input_file(&ctx, argv[1], NULL, 0, NULL) != 0) {
    fprintf(stderr, "ERROR: Cannot open file: '%s'.\n", argv[1]);
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

  AVStream* st = ctx->streams[0];

  for (int i = 0; i < st->nb_index_entries; i++) {
    AVIndexEntry entry = st->index_entries[i];
    printf("%s %s %ld %ld %d\n", real_directory, filename, entry.timestamp, entry.pos, entry.size);
  }

  return 0;

}

