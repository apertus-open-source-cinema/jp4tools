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
#include <jpeglib.h>
}

#include "jp4.h"

void JP4::open(const string& _filename) {

  this->_filename = string(_filename);

  struct jpeg_error_mgr jerr;
  struct jpeg_decompress_struct dinfo;

  JSAMPARRAY buffer;

  FILE *ifp;

  dinfo.err = jpeg_std_error (&jerr);

  ifp = fopen(filename().c_str(), "rb");

  jpeg_create_decompress (&dinfo);
  jpeg_stdio_src (&dinfo, ifp);
  jpeg_read_header (&dinfo, TRUE);
  dinfo.out_color_space = JCS_GRAYSCALE;

  this->_width = dinfo.image_width;
  this->_height = dinfo.image_height;

  buffer = (*dinfo.mem->alloc_sarray)((j_common_ptr)&dinfo, JPOOL_IMAGE, width(), 1);
  _data.resize(width()*height());

  jpeg_start_decompress (&dinfo);

  vector<unsigned char> temp;
  temp.resize(width()*height());

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


//   PGM debug

//   printf("P2\n%d %d\n%d\n", width(), height(), 0xff);
//   for (int i = 0; i < height(); i++) {
//     for (int j = 0; j < width(); j++) {
//       printf("%d ", data()[i*width() + j]);
//     }
//     printf("\n");
//   }
	   
  jpeg_finish_decompress (&dinfo);


  fclose(ifp);
}
