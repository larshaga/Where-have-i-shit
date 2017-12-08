package lars.wherehaveishit;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity
{

    DBHandler db;
    List<Shit> allShitsInDB;
    double avgPoopRating;
    List<String> listAllShitCountries = new LinkedList<>();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {

        db = new DBHandler(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        TextView totalNumberOfShits = (TextView) findViewById(R.id.etxt_numberoftotalshits);
        TextView avgRatingOfShits = (TextView) findViewById(R.id.etxt_ageratingofshits);
        TextView mostShitCountry = (TextView) findViewById(R.id.etxt_mostshitsincountry);

        allShitsInDB = db.findAllShits();


        long numberOfPlaces = db.getLineCount();
        totalNumberOfShits.setText(String.valueOf(numberOfPlaces));

        for (Shit shitInMap : allShitsInDB)
        {
            avgPoopRating = ((shitInMap.getShitRatingOverall() + shitInMap.getShitRatingPrivacy() + shitInMap.getShitRatingCleanness()) / 3) + avgPoopRating;

            if (isNetworkAvailable())
            {
                double lat = Double.parseDouble(shitInMap.getShitLatitude());
                double lon = Double.parseDouble(shitInMap.getShitLongitude());
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> place = null;
                String countryName = null;
                try
                {
                    place = geocoder.getFromLocation(lat, lon, 1);
                    countryName = place.get(0).getCountryName();
                    if (!countryName.equals(null))
                    {
                        listAllShitCountries.add(countryName);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(StatisticsActivity.this, getApplicationContext().getString(R.string.no_network_available), Toast.LENGTH_LONG).show();
            }
        }
        avgRatingOfShits.setText(String.valueOf(avgPoopRating / numberOfPlaces).substring(0, 3));
        mostShitCountry.setText(getShitiestCountry());


        TextView sendFeedbackToDev = (TextView) findViewById(R.id.mailFeedbackToDeveloper);

        sendFeedbackToDev.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick( View v )
            {

                SettingsActivity.sendFeedback(StatisticsActivity.this);
            }
        });

    }

    private boolean isNetworkAvailable( )
    {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getShitiestCountry( )
    {

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String tempStr;
        for (int i = 0; i < listAllShitCountries.size(); i++)
        {
            tempStr = listAllShitCountries.get(i);
            if (map.containsKey(tempStr))
            {
                map.put(tempStr, map.get(tempStr) + 1);
            }
            else
            {
                map.put(tempStr, 1);
            }
        }
        Map.Entry<String, Integer> maxEntry = null;

        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue())
            {
                maxEntry = entry;
            }
        }
        if (maxEntry != null)
        {
            return maxEntry.getKey();
        }
        return getApplicationContext().getString(R.string.none);
    }
}

