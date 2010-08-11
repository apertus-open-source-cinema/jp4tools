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

void JP4::open(const string& _filename) {

  this->_filename = string(_filename);

  struct jpeg_error_mgr jerr;
  struct jpeg_decompress_struct dinfo;

  JSAMPARRAY buffer;

  FILE *ifp;

  // EXIF
  _ed = exif_data_new_from_file(_filename.c_str());
  readMakerNote();
	   
  dinfo.err = jpeg_std_error (&jerr);

  ifp = fopen(filename().c_str(), "rb");

  jpeg_create_decompress (&dinfo);
  jpeg_stdio_src (&dinfo, ifp);
  jpeg_read_header (&dinfo, TRUE);
  dinfo.out_color_space = JCS_GRAYSCALE;

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

void JP4::reverseGammaTable(unsigned short* rgtable, unsigned int component) const {

  int i;
  double x, black256 ,k;
  int* gtable = new int[257];
  int ig;

  double gamma       = _makerNote.gamma[component];
  double gamma_scale = _makerNote.gamma_scale[component];
  double black       = _makerNote.black[component];

  black256=black*256.0;
  
  k = 1.0/(256.0-black256);

  if (gamma < 0.13) gamma=0.13;
  if (gamma >10.0)  gamma=10.0;

  for (i=0; i<257; i++) {
    x=k*(i-black256);
    if (x < 0.0 ) x=0.0;
    ig= (int) (0.5+65535.0*pow(x,gamma));
    ig=(ig* (int) gamma_scale)/0x400;
    if (ig > 0xffff) ig=0xffff;
    gtable[i]=ig;
  }

  /** now gtable[] is the same as was used in the camera */
  /** FPGA was using linear interpolation between elements of the gamma table, so now we'll reverse that process */
  int indx=0;
  unsigned short outValue;
  
  for (i=0; i<256; i++ ) {
    outValue=128+(i<<8);
    
    while ((gtable[indx+1]<outValue) && (indx<256)) indx++;
    
    if (indx>=256)
      rgtable[i]=(65535.0/256);
    else if (gtable[indx+1]==gtable[indx])
      rgtable[i]=i;
    else
      rgtable[i]=256.0*(indx+(1.0*(outValue-gtable[indx]))/(gtable[indx+1] - gtable[indx]));
  }

  delete[] gtable;
}

void JP4::readMakerNote() {

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
