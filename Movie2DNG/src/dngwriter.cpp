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

#include "dng_camera_profile.h"
#include "dng_color_space.h"
#include "dng_exceptions.h"
#include "dng_file_stream.h"
#include "dng_globals.h"
#include "dng_host.h"
#include "dng_ifd.h"
#include "dng_image_writer.h"
#include "dng_info.h"
#include "dng_linearization_info.h"
#include "dng_memory_stream.h"
#include "dng_mosaic_info.h"
#include "dng_negative.h"
#include "dng_preview.h"
#include "dng_read_image.h"
#include "dng_render.h"
#include "dng_simple_image.h"
#include "dng_tag_codes.h"
#include "dng_tag_types.h"
#include "dng_tag_values.h"
#include "dng_xmp.h"
#include "dng_xmp_sdk.h"

#include "dngwriter.h"
#include "jp4.h"

#include <vector>
using std::vector;

void DNGWriter::write(const JP4& jp4, const string& dngFilename) {

  // TODO
  unsigned int whitePoint = 0xffff;
  
  // DNG memory allocation and initialization

  dng_memory_allocator memalloc(gDefaultDNGMemoryAllocator);
  dng_memory_stream stream(memalloc);
  stream.Put(jp4.data(), jp4.width()*jp4.height()*sizeof(unsigned short));

  unsigned int width = jp4.width();
  unsigned int height = jp4.height();

  dng_rect rect(height, width);

  dng_host host(&memalloc);
  host.SetSaveDNGVersion(dngVersion_SaveDefault);
  host.SetSaveLinearDNG(false);
  host.SetKeepOriginalFile(true);

  AutoPtr<dng_image> image(new dng_simple_image(rect, 1, ttShort, memalloc));

  // DNG IFD structure creation

  dng_ifd ifd;

  ifd.fUsesNewSubFileType        = true;
  ifd.fCompression               = ccUncompressed;

  ifd.fPlanarConfiguration       = pcPlanar;
  ifd.fPhotometricInterpretation = piCFA;

  ifd.fImageWidth                = width;
  ifd.fImageLength               = height;

  ifd.fTileWidth                 = width;
  ifd.fTileLength                = height;

  ifd.fSamplesPerPixel           = 1;
  ifd.fBitsPerSample[0]          = 16;
  ifd.fSampleFormat[0]           = sfUnsignedInteger;

  ifd.fBlackLevel[0][0][0]       = jp4.makerNote().black[0]*256;
  ifd.fBlackLevel[0][0][1]       = jp4.makerNote().black[1]*256;
  ifd.fBlackLevel[0][0][2]       = jp4.makerNote().black[2]*256;
  ifd.fBlackLevel[0][0][3]       = jp4.makerNote().black[3]*256;

  ifd.fWhiteLevel[0]             = whitePoint;
  
  ifd.fLinearizationTableType   = ttShort;
  ifd.fLinearizationTableCount  = 256;

  ifd.fCFARepeatPatternRows      = 2;
  ifd.fCFARepeatPatternCols      = 2;

  ifd.fActiveArea                = rect;
  ifd.fDefaultCropSizeH          = dng_urational(width, 1);
  ifd.fDefaultCropSizeV          = dng_urational(height, 1);

  ifd.ReadImage(host, stream, *image.Get());

  // DNG Negative structure creation

  AutoPtr<dng_negative> negative(host.Make_dng_negative());

  negative->SetDefaultScale(ifd.fDefaultScaleH, ifd.fDefaultScaleV);
  negative->SetDefaultCropOrigin(ifd.fDefaultCropOriginH, ifd.fDefaultCropOriginV);
  negative->SetDefaultCropSize(ifd.fDefaultCropSizeH, ifd.fDefaultCropSizeV);
  negative->SetActiveArea(ifd.fActiveArea);

  negative->SetOriginalRawFileName(jp4.filename().c_str());

  negative->SetWhiteLevel(whitePoint, 0);

  negative->SetBlackLevel(jp4.makerNote().black[0]*65536, 0);
  negative->SetBlackLevel(jp4.makerNote().black[1]*65536, 1);
  negative->SetBlackLevel(jp4.makerNote().black[2]*65536, 2);
  negative->SetBlackLevel(jp4.makerNote().black[3]*65536, 3);

  // linearization table (handles gamma, gamma_scale and black level)
  AutoPtr<dng_memory_block> curve(memalloc.Allocate(256*sizeof(unsigned short)));
  jp4.reverseGammaTable(curve->Buffer_uint16(), 0);
  negative->SetLinearization(curve);

  // gain
  dng_vector gain = dng_vector(4);
  gain[0] = jp4.makerNote().gain[0];
  gain[1] = jp4.makerNote().gain[1];
  gain[2] = jp4.makerNote().gain[2];
  gain[3] = jp4.makerNote().gain[3];

  negative->SetAnalogBalance(gain);
  
  // bayer
  negative->SetRGB();
  negative->SetBayerMosaic(0);
  negative->SetBaseOrientation(dng_orientation::Normal());

  // -------------------------------------------------------------------------------

  // Set Camera->XYZ Color matrix as profile.
  dng_matrix_3by3 matrix(2.005, -0.771, -0.269, -0.752, 1.688, 0.064, -0.149, 0.283, 0.745);

  AutoPtr<dng_camera_profile> prof(new dng_camera_profile);
  prof->SetColorMatrix1((dng_matrix) matrix);
  prof->SetCalibrationIlluminant1(lsD65);
  negative->AddProfile(prof);

  double cameraMult[] = { 0.807133, 1.0, 0.913289};
  negative->SetCameraNeutral(dng_vector_3(cameraMult[0],
                                          cameraMult[1],
                                          cameraMult[2]));

  // Updating metadata to DNG Negative
  dng_exif *exif = negative->GetExif();
  exif->fModel.Set_ASCII("Elphel 353E"); // TODO: model
  exif->fMake.Set_ASCII("Elphel"); // TODO: model

  // Time from original shot
  dng_date_time dt; // TODO: datetime
  dt.fYear   = 2010;
  dt.fMonth  = 06;
  dt.fDay    = 22;
  dt.fHour   = 00;
  dt.fMinute = 00;
  dt.fSecond = 01;

  dng_date_time_info dti;
  dti.SetDateTime(dt);
  exif->fDateTimeOriginal  = dti;
  exif->fDateTimeDigitized = dti;
  negative->UpdateDateTime(dti);

  // Assign Raw image data.
  negative->SetStage1Image(image);

  // Compute linearized and range mapped image
  negative->BuildStage2Image(host);

  // Compute demosaiced image (used by preview and thumbnail)
  negative->BuildStage3Image(host);

  negative->SynchronizeMetadata();
  negative->RebuildIPTC(true, false);

  // DNGWriter: DNG thumbnail creation

  dng_preview_list previewList;

  dng_image_preview thumbnail;
  dng_render thumbnail_render(host, *negative);
  thumbnail_render.SetFinalSpace(dng_space_sRGB::Get());
  thumbnail_render.SetFinalPixelType(ttByte);
  thumbnail_render.SetMaximumSize(256);
  thumbnail.fImage.Reset(thumbnail_render.Render());

  // Creating DNG file

  dng_image_writer writer;
  dng_file_stream filestream(dngFilename.c_str(), true);

  writer.WriteDNG(host, filestream, *negative.Get(), thumbnail, ccUncompressed, &previewList);

}

