using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Geolocation;
using Windows.UI;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace App8.DataModel
{
    class RadarMap
    {

        public WriteableBitmap ImageSrc { get; set; }
         
        // 0, 1, 2 or 3 
        public int ImageIndex { get; set; }



        public RadarMap(int index, WriteableBitmap imgSrc)
        {
            this.ImageIndex = index;
            this.ImageSrc = imgSrc;


        }

        public WriteableBitmap cropAndScale(GeoboundingBox bounds, int mapControlWidth, int mapControlHeight )
        {

            /* get the corners */

            BasicGeoposition northwest = bounds.NorthwestCorner;
            BasicGeoposition southeast = bounds.SoutheastCorner;


            /* TO DO */
            
            // transfrom the corners to pixels

            Pixel northwestPixel = this.transformLocationToPixel(northwest.Longitude, northwest.Latitude);
            Pixel southeastPixel = this.transformLocationToPixel(southeast.Longitude, southeast.Latitude);

            // crop the square from the map

            WriteableBitmap cropped = ImageSrc.Crop(northwestPixel.X, northwestPixel.Y, northwestPixel.X - southeastPixel.X, northwestPixel.Y - southeastPixel.X);

            // resize to fit the given dimensions

            var resized = cropped.Resize(mapControlWidth, mapControlHeight, WriteableBitmapExtensions.Interpolation.Bilinear);

            return resized;



        }

        // need to implement
        public Color getColorAtPixel(Pixel pixel)
        {

            return new Color();
        }


        public Pixel transformLocationToPixel(double longtitue, double latitute)
        {
            return new Pixel(0, 0);
        }

        
    }

    public class Pixel
    {
        public int X { get; set; }
        public int Y { get; set; }

        public Pixel(int x, int y)
        {
            this.X = x;
            this.Y = y;
        }

    }



    public class RadarMapManager
    {

        public RadarMap[] Maps { get; set; }

        public RadarMapManager()
        {
            Maps = new RadarMap[4];
        }

        public async void updateMaps()
        {

            // update logic here
            var tmp = BitmapFactory.New(1, 1);
            String imgSrc = "radar/baseImage.gif";
            // currently
            for(int i = 0; i < 4; ++i)
            {
                WriteableBitmap current =  await tmp.FromContent(new Uri(String.Format("ms-appx:///Assets/{0}", imgSrc)));
                Maps[i] = new RadarMap(i, current);
            }


        }
    }
}
