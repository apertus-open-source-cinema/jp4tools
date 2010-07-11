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
  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "dngwriter.h"

extern "C" {
#include <libavformat/avformat.h>
}

#include <cstdio>

int main (int argc, char** argv) {

  if (argc < 4) {
    fprintf(stderr, "Usage: %s filename.mov frame-%%05d.jpg frame-%%05.dng\n", argv[0]);
    return -1;
  }

  AVFormatContext* ctx;
  char jp4Filename[255];
  char dngFilename[255];

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

  AVPacket packet;

  int frame = 0;

  while (av_read_frame(ctx, &packet) >= 0) {
    snprintf(jp4Filename, 255, argv[2], frame);
    snprintf(dngFilename, 255, argv[3], frame);

    FILE* fd = fopen(jp4Filename, "w");
    fwrite(packet.data, packet.size, 1, fd);
    fclose(fd);

    // convert to DNG
    DNGWriter::write(jp4Filename, dngFilename);

    av_free_packet(&packet);

    fprintf(stdout, "Converting frame %d...\r", frame);
 
    frame++;
    break;
  }

  fprintf(stdout, "\n");

  return 0;

}
