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

#ifndef JP4_H
#define JP4_H 1

#include <string>
using std::string;

#include <vector>
using std::vector;

class JP4 {

 public:

  JP4() {}
  ~JP4() {}

  void open(const string& _filename);

  const string& filename() { return _filename; }

  unsigned int width() { return _width; }

  unsigned int height() { return _height; }

  vector<unsigned char>& data() { return _data; }

 private:
  JP4(const JP4& other); // non-conpyable
  JP4& operator=(const JP4& other); // non-assignable

  string _filename;
  unsigned int _width;
  unsigned int _height;
  vector<unsigned char> _data;

};

#endif
