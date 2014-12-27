using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace App8.DataModel
{
    public class PredictionIcon
    {

        public String Title { get; set; }
        public String Info { get; set;  }
        public String ImagePath { get; set; }
        
        public PredictionIcon(double averageRain, int timeIndex)
        {

            Title = "Rain at " + timeIndex;
            InitImagePath(averageRain);



        }


        private void InitImagePath(double averageRain)
        {

            if (averageRain <= 0.2)
            {
                ImagePath = "Assets/rain/sun.png";
                Info = "Cloudy";
                // cloudy
                return;
            }

            if (averageRain <= 0.7)
            {
                // mildy rainy
                ImagePath = "Assets/rain/sun_cloud.png";
                Info = "Midly Rainy";
                return;
            }

            if (averageRain <= 2)
            {
                ImagePath ="Assets/rain/rain_weak.png";
                Info = "Weak rain";
                // rainy
                return;
            }

            if (averageRain <= 13)
            {
                // very rainy
                ImagePath = "Assets/rain/rain_strong.png";
                Info = "Rain";
                return;
            }
            else
            {
                ImagePath = "Assets/rain/rain_nax.png";
                // yellow and above
                // super rainy
                Info = "Strong rain";
            }

        }


    }

    public class PredictionCollection
    {
        public ObservableCollection<PredictionIcon> PredictionIcons { get; set; }


        public PredictionCollection(double [] averageRain)
        {


            PredictionIcons = new ObservableCollection<PredictionIcon>();
            PredictionIcons.Add(new PredictionIcon(averageRain[0], 0));
            PredictionIcons.Add(new PredictionIcon(averageRain[1], 10));
            PredictionIcons.Add(new PredictionIcon(averageRain[2], 20));
            PredictionIcons.Add(new PredictionIcon(averageRain[3], 30));
        }



    }


    public sealed class PredictionIconDataSource
    {
        private static PredictionIconDataSource __dataSource = new PredictionIconDataSource();

        public static PredictionCollection getData()
        {

            return __dataSource.getPredictionCollection();
        }


        private PredictionCollection getPredictionCollection()
        {

            double[] predictions = { 0.2, 0.3, 5, 1.5 };
            PredictionCollection collection = new PredictionCollection(predictions);
            return collection;


        }

    }
}
