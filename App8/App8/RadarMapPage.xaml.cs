using App8.Common;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Graphics.Display;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Popups;
using Windows.Devices.Geolocation;
using Windows.UI.Xaml.Controls.Maps;
using Windows.UI;
using Windows.UI.Xaml.Shapes;
using Windows.UI.Xaml.Media.Imaging;  

// The Basic Page item template is documented at http://go.microsoft.com/fwlink/?LinkID=390556

namespace App8
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class RadarMapPage : Page
    {
        private NavigationHelper navigationHelper;
        private ObservableDictionary defaultViewModel = new ObservableDictionary();

        public RadarMapPage()
        {
            this.InitializeComponent();

            this.navigationHelper = new NavigationHelper(this);
            this.navigationHelper.LoadState += this.NavigationHelper_LoadState;
            this.navigationHelper.SaveState += this.NavigationHelper_SaveState;


            /* navigate the map to israel */
            //Israel          34.282   29.000   35.667   33.286 
            //country        longmin   latmin  longmax   latmax



         
           

        }

        /// <summary>
        /// Gets the <see cref="NavigationHelper"/> associated with this <see cref="Page"/>.
        /// </summary>
        public NavigationHelper NavigationHelper
        {
            get { return this.navigationHelper; }
        }

        /// <summary>
        /// Gets the view model for this <see cref="Page"/>.
        /// This can be changed to a strongly typed view model.
        /// </summary>
        public ObservableDictionary DefaultViewModel
        {
            get { return this.defaultViewModel; }
        }

        /// <summary>
        /// Populates the page with content passed during navigation.  Any saved state is also
        /// provided when recreating a page from a prior session.
        /// </summary>
        /// <param name="sender">
        /// The source of the event; typically <see cref="NavigationHelper"/>
        /// </param>
        /// <param name="e">Event data that provides both the navigation parameter passed to
        /// <see cref="Frame.Navigate(Type, Object)"/> when this page was initially requested and
        /// a dictionary of state preserved by this page during an earlier
        /// session.  The state will be null the first time a page is visited.</param>
        private void NavigationHelper_LoadState(object sender, LoadStateEventArgs e)
        {
        }

        /// <summary>
        /// Preserves state associated with this page in case the application is suspended or the
        /// page is discarded from the navigation cache.  Values must conform to the serialization
        /// requirements of <see cref="SuspensionManager.SessionState"/>.
        /// </summary>
        /// <param name="sender">The source of the event; typically <see cref="NavigationHelper"/></param>
        /// <param name="e">Event data that provides an empty dictionary to be populated with
        /// serializable state.</param>
        private void NavigationHelper_SaveState(object sender, SaveStateEventArgs e)
        {

            
        }

        #region NavigationHelper registration

        /// <summary>
        /// The methods provided in this section are simply used to allow
        /// NavigationHelper to respond to the page's navigation methods.
        /// <para>
        /// Page specific logic should be placed in event handlers for the  
        /// <see cref="NavigationHelper.LoadState"/>
        /// and <see cref="NavigationHelper.SaveState"/>.
        /// The navigation parameter is available in the LoadState method 
        /// in addition to page state preserved during an earlier session.
        /// </para>
        /// </summary>
        /// <param name="e">Provides data for navigation methods and event
        /// handlers that cannot cancel the navigation request.</param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            this.navigationHelper.OnNavigatedTo(e);
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            this.navigationHelper.OnNavigatedFrom(e);
        }

        #endregion

       


        public static GeoboundingBox GetBounds(/*this*/ MapControl map)
        {
            Geopoint topLeft = null;

            try
            {
                map.GetLocationFromOffset(new Windows.Foundation.Point(0, 0), out topLeft);
            }
            catch
            {
                var topOfMap = new Geopoint(new BasicGeoposition()
                {
                    Latitude = 85,
                    Longitude = 0
                });

                Windows.Foundation.Point topPoint;
                map.GetOffsetFromLocation(topOfMap, out topPoint);
                map.GetLocationFromOffset(new Windows.Foundation.Point(0, topPoint.Y), out topLeft);
            }

            Geopoint bottomRight = null;
            try
            {
                map.GetLocationFromOffset(new Windows.Foundation.Point(map.ActualWidth, map.ActualHeight), out bottomRight);
            }
            catch
            {
                var bottomOfMap = new Geopoint(new BasicGeoposition()
                {
                    Latitude = -85,
                    Longitude = 0
                });

                Windows.Foundation.Point bottomPoint;
                map.GetOffsetFromLocation(bottomOfMap, out bottomPoint);
                map.GetLocationFromOffset(new Windows.Foundation.Point(0, bottomPoint.Y), out bottomRight);
            }

            if (topLeft != null && bottomRight != null)
            {
                return new GeoboundingBox(topLeft.Position, bottomRight.Position);
            }

            return null;
        }

        private void location_TextChanged(object sender, TextChangedEventArgs e)
        {

        }

        private void location_GotFocus(object sender, RoutedEventArgs e)
        {
            ((TextBox)sender).Text = "";
        }

     

        private async void map_Loaded(object sender, RoutedEventArgs e)
        {

            // set bounds to israel

            List<BasicGeoposition> basicPositions = new List<BasicGeoposition>();
            basicPositions.Add(new BasicGeoposition() { Latitude = 33.0317, Longitude = 035.1702 });
            basicPositions.Add(new BasicGeoposition() { Latitude = 29.6253, Longitude = 034.9147 });


           await this.map.TrySetViewBoundsAsync(GeoboundingBox.TryCompute(basicPositions), null, MapAnimationKind.Linear);
        }

        private async void AppBarButton_Click(object sender, RoutedEventArgs e)
        {
            // locate me 

            var locator = new Geolocator();
            locator.DesiredAccuracyInMeters = 50;
            var myPosition = await locator.GetGeopositionAsync();



           // MapIcon MapIcon1 = new MapIcon();
           // MapIcon1.Location = new Geopoint(new BasicGeoposition()
           // {
           //     Latitude = myPosition.Coordinate.Point.Position.Latitude,
           //     Longitude = myPosition.Coordinate.Point.Position.Longitude
           // });
           // MapIcon1.NormalizedAnchorPoint = new Point(0.5, 1.0);
           // MapIcon1.Title = "Space Needle";

           var pin = CreatePin();

          //  map.MapElements.Add(MapIcon1);

            map.Children.Add(pin);
            MapControl.SetLocation(pin, myPosition.Coordinate.Point);
            MapControl.SetNormalizedAnchorPoint(pin, new Point(0.5, 0.5));

            await map.TrySetViewAsync(myPosition.Coordinate.Point, 18D);
        }

        private DependencyObject CreatePin()
        {
            //Creating a Grid element.
            var myGrid = new Grid();
            myGrid.RowDefinitions.Add(new RowDefinition());
          //  myGrid.RowDefinitions.Add(new RowDefinition());
            myGrid.Background = new SolidColorBrush(Colors.Transparent);

            //Creating a Rectangle
            var myRectangle = new Rectangle { Fill = new SolidColorBrush(Colors.Black), Height = 40, Width = 40 };
            ImageBrush imgBrush = new ImageBrush();
            imgBrush.ImageSource = new BitmapImage(new Uri("ms-appx:///Assets/rain/rain_strong.png"));

            var secRec = new Rectangle { Fill = imgBrush, Height = 120, Width = 120 };

            myRectangle.SetValue(Grid.RowProperty, 0);
            myRectangle.SetValue(Grid.ColumnProperty, 0);

            //Adding the Rectangle to the Grid
            //myGrid.Children.Add(myRectangle);
            myGrid.Children.Add(secRec);

            //Creating a Polygon
           // var myPolygon = new Polygon()
           // {
           //     Points = new PointCollection() { new Point(2, 0), new Point(22, 0), new Point(2, 40) },
           //     Stroke = new SolidColorBrush(Colors.Black),
           //     Fill = new SolidColorBrush(Colors.Black)
           // };
            //myPolygon.SetValue(Grid.RowProperty, 1);
            //myPolygon.SetValue(Grid.ColumnProperty, 0);

            //Adding the Polygon to the Grid
           // myGrid.Children.Add(myPolygon);
            return myGrid;
        }
    }
}
