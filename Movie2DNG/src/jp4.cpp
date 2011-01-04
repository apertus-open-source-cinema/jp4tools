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

extern "C" {
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jpeglib.h>
#include <libexif/exif-data.h>
#include <libexif/exif-utils.h>
}

#include "jp4.h"

static long get_long(unsigned char* d) {
  return ((d[0] & 0xff) << 24) | ((d[1] & 0xff) << 16) | ((d[2] & 0xff) << 8) | (d[3] & 0xff);

}

JP4::JP4() {}

JP4::~JP4() {
  if (_data)
    delete[] _data;
  if (_raw_app1)
    delete[] _raw_app1;
}

const string& JP4::filename() const {
  return _filename;
}

unsigned int JP4::width() const {
  return _width;
}

unsigned int JP4::height() const {
  return _height;
}

unsigned short* JP4::data() const {
  return _data;
}

const ElphelMakerNote& JP4::makerNote() const {
  return _makerNote;
}

bool JP4::linear() const {
  return _linear;
}

void JP4::open(const string& _filename) {

  this->_filename = string(_filename);

  struct jpeg_error_mgr jerr;
  struct jpeg_decompress_struct dinfo;

  JSAMPARRAY buffer;

  FILE *ifp = NULL;

  dinfo.err = jpeg_std_error (&jerr);

  ifp = fopen(filename().c_str(), "rb");

  jpeg_create_decompress (&dinfo);
  jpeg_stdio_src (&dinfo, ifp);
  // instruct it to save EXIF at APP1 (0xe1) data (up to 64k)
  jpeg_save_markers(&dinfo, 0xe1, 0xffff);
  jpeg_read_header (&dinfo, TRUE);

  dinfo.do_block_smoothing = FALSE;
  dinfo.out_color_space = JCS_GRAYSCALE;

  // save raw APP1 data (if any)
  if (dinfo.marker_list) {
    _raw_app1_length = dinfo.marker_list[0].data_length;
    _raw_app1 = new unsigned char[_raw_app1_length];
    memcpy(_raw_app1, dinfo.marker_list[0].data, _raw_app1_length);
  }

  this->_width = dinfo.image_width;
  this->_height = dinfo.image_height;

  buffer = (*dinfo.mem->alloc_sarray)((j_common_ptr)&dinfo, JPOOL_IMAGE, width(), 1);
  
  _data = new unsigned short[width()*height()];

  jpeg_start_decompress (&dinfo);

  unsigned short* temp = new unsigned short[width()*height()];

  for (unsigned int line = 0; line < height(); line++) {
    jpeg_read_scanlines (&dinfo, buffer, 1);
    for (unsigned int column = 0; column < width(); column++)
      temp[line*width() + column] = buffer[0][column];
  }

  // EXIF
  readMakerNote();

  // JP4 deblocking
  // from http://code.google.com/p/gst-plugins-elphel/source/browse/trunk/jp462bayer/src/gstjp462bayer.c
  unsigned int y, x;
  unsigned int b_of = 0;
  unsigned int h_of;
  unsigned int i, j;
  unsigned int index1[16]={0,8,1,9,2,10,3,11,4,12,5,13,6,14,7,15};
  unsigned int  index2[16];

  for (j = 0;j < 16; ++j)
    index2[j] = index1[j] * width();

  for (y = 0; y < height(); y += 16, b_of += width() << 4)
    for (x = 0; x < width(); x += 16)
      for (j = 0, h_of = 0; j < 16; ++j, h_of += width())
        for (i = 0; i < 16; ++i)
          _data[x + i + h_of + b_of] = temp[x + index1[i] + index2[j] + b_of];

  jpeg_finish_decompress (&dinfo);
  fclose(ifp);
  delete[] temp;

}

void JP4::readMakerNote() {

  if (_raw_app1)
    _ed = exif_data_new_from_data(_raw_app1, _raw_app1_length);

  if (!_ed)
    return;

  ExifEntry* makerNoteEntry = exif_data_get_entry(_ed, EXIF_TAG_MAKER_NOTE);

  if (!makerNoteEntry) {
    _makerNote.gain[0]  = _makerNote.gain[1]  = _makerNote.gain[2]  = _makerNote.gain[3] = 2.0;
    _makerNote.gamma[0] = _makerNote.gamma[1] = _makerNote.gamma[2] = _makerNote.gamma[3] = 0.57;
    _makerNote.gamma_scale[0] = _makerNote.gamma_scale[1] = _makerNote.gamma_scale[2] = _makerNote.gamma_scale[3] = 0xffff;

    _makerNote.black[0] = _makerNote.black[1] = _makerNote.black[2] = _makerNote.black[3] = 10/256.0;
    _makerNote.decim_hor = 1;
    _makerNote.decim_ver = 1;
    _makerNote.bin_hor = 1;
    _makerNote.bin_ver = 1;
    return;
  }

  int makerNoteLength = makerNoteEntry->size/4;
  long makerNote[makerNoteLength];

  for (int i = 0; i < makerNoteLength; i++) {
    makerNote[i] = get_long(makerNoteEntry->data + i*4);
  }

  for (int i = 0; i < 4; i++) {
    // channel gain  8.16 (0x10000 - 1.0). Combines both analog gain and digital scaling
    _makerNote.gain[i] = makerNote[i] / 65536.0;  
 
    // (P_PIXEL_LOW<<24) | (P_GAMMA <<16) and 16-bit (6.10) scale for gamma tables, 
    _makerNote.gamma_scale[i] = (makerNote[i+4] & 0xffff);
    _makerNote.gamma[i]       = ((makerNote[i+4]>>16) & 0xff) / 100.0;
    _makerNote.black[i]       =  (makerNote[i+4]>>24) / 256.0;
  }

  if (makerNoteLength >= 12) {
    _makerNote.woi_left   = (makerNote[8] & 0xffff);
    _makerNote.woi_width  = (makerNote[8]>>16) & 0xffff;
    _makerNote.woi_top    = (makerNote[9] & 0xffff);
    _makerNote.woi_height = (makerNote[9]>>16);

    _makerNote.flip_hor = (makerNote[10] & 0x1);
    _makerNote.flip_ver = (makerNote[10]>>1) & 0x1;

    _makerNote.bayer_mode = (makerNote[10]>>2) & 0x3;
    _makerNote.color_mode = (makerNote[10]>>4) & 0x0f;

    _makerNote.decim_hor = (makerNote[10]>> 8) & 0x0f;
    _makerNote.decim_ver = (makerNote[10]>>12) & 0x0f;

    _makerNote.bin_hor = (makerNote[10]>>16) & 0x0f;
    _makerNote.bin_ver = (makerNote[10]>>20) & 0x0f;
  }

  if (makerNoteLength >= 14) {
    _makerNote.composite = ((makerNote[10] & 0xc0000000)!=0);
    if (_makerNote.composite) {
      _makerNote.height1 = makerNote[11] & 0xffff;
      _makerNote.blank1  =(makerNote[11]>>16) & 0xffff;
      _makerNote.height2 = makerNote[12] & 0xffff;
      _makerNote.blank2  =(makerNote[12]>>16) & 0xffff;
      _makerNote.height3 =(makerNote[9]>>16) - _makerNote.height1-_makerNote.blank1-_makerNote.height2-_makerNote.blank2;

      _makerNote.flip_h1 = (((makerNote[10] >> 24) & 1)!=0);
      _makerNote.flip_v1 = (((makerNote[10] >> 25) & 1)!=0);
      _makerNote.flip_h2 = (((makerNote[10] >> 26) & 1)!=0);
      _makerNote.flip_v2 = (((makerNote[10] >> 27) & 1)!=0);
      _makerNote.flip_h3 = (((makerNote[10] >> 28) & 1)!=0);
      _makerNote.flip_v3 = (((makerNote[10] >> 29) & 1)!=0);
    }
  }

}

void JP4::flipX() {

  for (unsigned int y = 0; y < _height/2; y++) {
    for (unsigned int x = 0; x < _width; x++) {
      unsigned int src = y*_width + x;
      unsigned int dst = (_height-y-1)*_width + x;

      unsigned short tmp = _data[src];
      _data[src] = _data[dst];
      _data[dst] = tmp;
    }
  }

}
 
void JP4::flipY() {

  for (unsigned int y = 0; y < _height; y++) {
    for (unsigned int x = 0; x < _width/2; x++) {
      unsigned int src = y*_width + x;
      unsigned int dst = y*_width + (_width-x-1);

      unsigned short tmp = _data[src];
      _data[src] = _data[dst];
      _data[dst] = tmp;
    }
  }

}

void JP4::writePGM(const string& pgmFilename) const {

  FILE* pgm = fopen(pgmFilename.c_str(), "w");
  if (!pgm) return;

  fprintf(pgm, "P2\n%d %d\n%d\n", _width, _height, 0xff);
  for (unsigned int i = 0; i < _height; i++) {
    for (unsigned int j = 0; j < _width; j++) {
      fprintf(pgm, "%d ", _data[i*_width + j]);
    }   
    fprintf(pgm, "\n");
  }

  fclose(pgm);

}

void JP4::writeJPEG(const string jpegFilename, unsigned int quality) const {

  struct jpeg_error_mgr jerr;
  struct jpeg_compress_struct cinfo;

  unsigned int i, j, r, row, col, jj;

  JSAMPARRAY buf;
  JSAMPARRAY copy;

  FILE *ofp;

  cinfo.err = jpeg_std_error (&jerr);

  ofp = fopen(jpegFilename.c_str(), "wb");

  jpeg_create_compress (&cinfo);
  jpeg_stdio_dest (&cinfo, ofp);

  cinfo.image_width = _width;
  cinfo.image_height = _height;
  cinfo.input_components = 1;
  cinfo.in_color_space = JCS_GRAYSCALE;
  jpeg_set_defaults(&cinfo);

  jpeg_set_quality(&cinfo, quality, false);

  jpeg_start_compress (&cinfo, TRUE);

  // write EXIF data (if any)
  if (_raw_app1) {
    jpeg_write_marker(&cinfo, 0xe1, _raw_app1, _raw_app1_length);
  }

  unsigned char* scanline = new unsigned char[_width];

  for (i=0; i < _height; i++) {

    for (j=0; j < _width; j++)
      scanline[j] = _data[i*_width + j];

    jpeg_write_scanlines (&cinfo, &scanline, 1);
  }

  jpeg_finish_compress (&cinfo);

  fclose(ofp);

  delete[] scanline;
}

ExifData* JP4::exifData() const {
  return _ed;
}

bool JP4::hasTag(ExifTag tag) const {
  return exif_data_get_entry(_ed, tag) != NULL;
}

unsigned int JP4::getTagUInt(ExifTag tag) const {
  ExifEntry* e = exif_data_get_entry(_ed, tag);
  return exif_get_long(e->data, exif_data_get_byte_order(_ed));
}

string JP4::getTagString(ExifTag tag) const {
  ExifEntry* e = exif_data_get_entry(_ed, tag);
  char value[e->size];
  exif_entry_get_value(e, value, e->size);
  return string(value);
}

void JP4::getTagURational(ExifTag tag, unsigned int* n, unsigned int* d) const {
  ExifEntry* e = exif_data_get_entry(_ed, tag);
  ExifRational r = exif_get_rational(e->data, exif_data_get_byte_order(_ed));
  if (n) *n = r.numerator;
  if (d) *d = r.denominator;
}

void JP4::getTagSRational(ExifTag tag, int* n, int* d) const {
  ExifEntry* e = exif_data_get_entry(_ed, tag);
  ExifSRational r = exif_get_srational(e->data, exif_data_get_byte_order(_ed));
  if (n) *n = r.numerator;
  if (d) *d = r.denominator;
}

ExifEntry* JP4::getTagRaw(ExifTag tag) const {
  return exif_data_get_entry(_ed, tag);
}
