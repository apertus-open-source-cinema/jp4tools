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

#ifndef JP4_H
#define JP4_H 1

#include <string>
using std::string;

#include <vector>
using std::vector;

typedef struct {
  double gain[4];
  double gamma[4];
  double black[4];
  int    woi_left;
  int    woi_width;
  int    woi_top;
  int    woi_height;
  bool   flip_hor;
  bool   flip_ver;
  int    bayer_mode;
  int    color_mode;
  int    decim_hor;
  int    decim_ver;
  int    bin_hor;
  int    bin_ver;
} ElphelMakerNote;

class JP4 {

 public:

  JP4() {}
  ~JP4() {}

  void open(const string& _filename);

  const string& filename() { return _filename; }

  unsigned int width() { return _width; }

  unsigned int height() { return _height; }

  vector<unsigned char>& data() { return _data; }

  ElphelMakerNote& makerNote() { return _makerNote; }

 private:
  JP4(const JP4& other); // non-conpyable
  JP4& operator=(const JP4& other); // non-assignable

  void readMakerNote();

  string _filename;
  unsigned int _width;
  unsigned int _height;
  vector<unsigned char> _data;
  ElphelMakerNote _makerNote;

};

#endif
