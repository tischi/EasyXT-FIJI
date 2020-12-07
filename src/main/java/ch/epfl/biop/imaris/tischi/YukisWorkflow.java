package ch.epfl.biop.imaris.tischi;

import Imaris.Error;
import Imaris.ISurfacesPrx;
import ch.epfl.biop.imaris.EasyXT;
import ch.epfl.biop.imaris.SurfacesDetector;
import ch.epfl.biop.imaris.demo.FreshStartWithIJAndBIOPImsSample;

import java.io.File;

/**
 *
 * Running the analysis workflow from Yuki
 *
 * - Segment the surface in Channel 1 (0) (This could the largest structure - nucleolus)
 * - Segment the surface in Channel 3 (2) (Nucleolus)
 * - Segment spots in Channel 2 (1)
 * - Making a new segmentation by subtracting channel 3 (2) surface (Nucleolus) from channel 1 (0) surfaces (Nucleus)
 *   - I select the spot/surface that I want to make a new one based on the filter threshold (Blue and pink surface). Then, I use ’Shortest distance… (in this case channel 3 (NPM1)) in filter function and set the threshold over 0.5 µm from the surface. Then I can make a filtered spot/surface (yellow surface) and can create a new surface by ‘Duplicate selection…’ at the bottom of function.
 * - Track surfaces/spots
 * - Measurement
 * 	- Shape features
 * 	- Intensity of each channel in surfaces/spots
 * 	- Distance between spots and surfaces
 * - Export statistics
 *
 * @author Christian Tischer
 *
 * December 2020
 *
 * EMBL - CBA
 *
 *
 * /Applications/Imaris 9.6.0.app/Contents/SharedSupport/html/xtinterface/structImaris_1_1ISurfaces.html
 */

public class YukisWorkflow
{
    public static void main(String... args) {
        try {
            run();
        } catch ( Error error ) {
            System.out.println( "ERROR:" + error.mDescription );
            System.out.println( "LOCATION:" + error.mLocation );
            System.out.println( "String:" + error.toString() );
        }
    }

    private static void run() throws Error
    {
        EasyXT.openImage(new File("/Users/tischer/Desktop/yuki/e0519_spinning_disc_test3_1_130_decon_crop_frame1_frame2.ims"));

        // Segment Nuclei in Channel 0
        ISurfacesPrx nuclei = SurfacesDetector.Channel(0)
                .setSmoothingWidth(0.5)
                .enableAutomaticLowerThreshold()
                //.setLowerThreshold(40)
                //.setUpperThreshold(255.0)
                .setName("Nuclei")
                .setColor(new Integer[]{0,0,255})
                .setSurfaceFilter( "\"Volume\" above 8 um^3" )
                .setSurfaceFilter( "\"Distance to Image Border XYZ Img=1\" above 0.001 um"  )
                .build()
                .detect();
        EasyXT.getImaris().GetSurpassScene().AddChild(nuclei,0);

        // Segment Nucleoli in Channel 2
        //
        ISurfacesPrx nucleoli = SurfacesDetector.Channel(2)
                .setSmoothingWidth(0.13)
                .enableAutomaticLowerThreshold()
                //.setLowerThreshold(40)
                //.setUpperThreshold(255.0)
                .setName("Nucleoli")
                .setColor(new Integer[]{0,255,0})
                .setSurfaceFilter( "\"Volume\" above 1 um^3" )
                .setSurfaceFilter( "\"Distance to Image Border XYZ Img=1\" above 0.001 um"  )
                //.setSurfaceFilter(  )
                .build()
                .detect();
        EasyXT.getImaris().GetSurpassScene().AddChild(nucleoli,0);

        /**
         *
         * [Algorithm]
         *   Enable Region Of Interest = false
         *   Enable Region Growing = false
         *   Enable Tracking = false
         *   Enable Classify = false
         *   Enable Region Growing = false
         *   Enable Shortest Distance = true
         * [Source Channel]
         *   Source Channel Index = 2
         *   Estimated Diameter = 0.650 µm
         *   Background Subtraction = true
         * [Filter Spots]
         *   "Quality" above automatic threshold
         */


    }
}
