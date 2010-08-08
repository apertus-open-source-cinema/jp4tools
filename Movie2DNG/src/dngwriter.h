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

#ifndef DNGWRITER_H
#define DNGWRITER_H 1

#include <string>
using std::string;

class JP4;

class DNGWriter {

 public:
  static void write(const JP4& jp4, const string& dngFilename);

};

#endif
