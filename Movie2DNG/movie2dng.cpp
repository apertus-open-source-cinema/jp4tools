
extern "C" {
#include <stdio.h>
#include <libavformat/avformat.h>
}

int main (int argc, char** argv) {

  if (argc < 3) {
    fprintf(stderr, "Usage: %s filename.mov frame-%%05d.jpg\n", argv[0]);
    return -1;
  }

  AVFormatContext* ctx;
  char filename[255];

  av_register_all();

  if (av_open_input_file(&ctx, argv[1], NULL, 0, NULL) != 0) {
    fprintf(stderr, "ERROR: Cannot open file: '%s'.\n", argv[1]);
    return -1;
  }

  if (av_find_stream_info(ctx) < 0) {
    fprintf(stderr, "ERROR: Cannot find stream info.\n");
    return -2;
  }

  //dump_format(ctx, 0, argv[1], false);

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

    snprintf(filename, 255, argv[2], frame);

    FILE* fd = fopen(filename, "w");
    fwrite(packet.data, packet.size, 1, fd);
    fclose(fd);

    av_free_packet(&packet);

    fprintf(stdout, "Converting frame %d...\r", frame);
 
    frame++;

  }

  fprintf(stdout, "\n");

  return 0;

}
