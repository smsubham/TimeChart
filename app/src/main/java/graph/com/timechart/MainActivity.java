package graph.com.timechart;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.FormattedStringCache;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends DemoBase {

    private LineChart chart;
    ArrayList<Float> data = new ArrayList<>();  //used to store timestamp corresponding value in getChartData class i.e. 303 for 1467871629

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart_time);





      try {
          // add data
          setData();
      }

        catch (JSONException e){}

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(60000L); // one minute in millis
        xAxis.setValueFormatter(new AxisValueFormatter() {

            private FormattedStringCache.Generic<Long, Date> mFormattedStringCache = new FormattedStringCache.Generic<>(new SimpleDateFormat("dd MMM HH:mm"));

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Long v = (long) value;
                return mFormattedStringCache.getFormattedValue(new Date(v), v);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(170f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void setData() throws JSONException{
        getChartData get_data=new getChartData();
        get_data.execute("");
    }

    private class getChartData extends AsyncTask<String, Void, String> {
        int len;
        ArrayList<String> listdata; //timestamp data for graph
        ArrayList<Float> listvalue; //corresponding date data value
        ArrayList<Entry> values;
        private final String LOG_TAG = getChartData.class.getSimpleName();

        protected String doInBackground(String... params) {
            return getData();
        }

        protected String getData() {
            String decodedString ="";
            String returnMsg = "";
            String request = "http://52.77.220.93:4000/getLast?device=thane1&sensor=arduino&lim=300";
            URL url;
            HttpURLConnection connection = null;
            JSONObject myJson=null;
            String JsonStr = null; //used to store json string
            JSONArray jsonArray=null; //used to store json data as json array
            String prsdData[];

            try {
                url = new URL(request);

                //making connection
                connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                // Read the input stream into a String
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while ((decodedString = in.readLine()) != null) {
                    returnMsg += decodedString;
                    myJson = new JSONObject(returnMsg);
                    String str = myJson.optString("data");
                    str = str.substring(1, str.length() - 1);
                    str = str.replace(",", " ");
                    str = str.replace(":", " ");
                    prsdData = str.split("[\"]");
                    len = prsdData.length;
                }


                in.close(); //close stream

                 Log.v(LOG_TAG, "Forecast string: " + returnMsg);

            } catch (Exception e) {
               // e.printStackTrace();
            }
            //used to close connection in case of an exception
            finally {
                if (connection != null) {
                    connection.disconnect();              //connection.disconnect();
                }
            }
            return returnMsg;
        }

        protected void onPostExecute(String result) {
           chart = (LineChart) findViewById(R.id.chart1);
            String str;
            str=getData();
            listdata = new ArrayList<>(); //timestamp data for graph
            listvalue = new ArrayList<>(); //corresponding value of timestamp


                int  index_end=0,index_start=0;
            //this is used to extract data from parsed string
                    for (int i = 0; i < str.length(); i+=2)
                    {
                        while(!(str.charAt(i)==' '))
                        index_end=str.indexOf(" ",index_end);
                        String temp=str.substring(index_start,index_end);
                        listdata.add(temp);
                        index_start=index_end;
                        while(!(str.charAt(i)==' '))
                        index_end=str.indexOf(" ",index_end);
                        temp=str.substring(index_start,index_end);
                        listvalue.add(Float.parseFloat(temp));
                        index_start=index_end;
                    }

            ArrayList<Date> time = new ArrayList<>(); //will be convert time stamp to real date and time
            for (int i = 0;i<listdata.size(); i++){
                Timestamp stamp = new Timestamp(Long.parseLong(listdata.get(i).toString()));
                Date date = new Date(stamp.getTime());
                time.add(date);
            }
            //used to set entries
            ArrayList<Entry> values = new ArrayList<>(); //data for y-axis
            for (int i=0;i<data.size();i++){ //adding data for x and y axis respectively
                values.add(new Entry(listvalue.get(i),Float.parseFloat(listdata.get(i)))); //
            }


            LineDataSet set1 = new LineDataSet(values, "DataSet 1");


            // create a data object with the datasets
            LineData data = new LineData(set1);

            // set data
            chart.setData(data);
        }
    }
}