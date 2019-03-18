package com.gmail.seinkenaiyan;
//class retrieves data from url using sttp url connection
// and file handling methods
//**Data returned will be in JSON format**

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {

    public  String readUrl(String myUrl) throws IOException
    {
        String data = "";
        //methods to read url
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(myUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

        //reading data from the url
            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            //Reading strings line by line using the while loop
            String line = "";

            while ((line = br.readLine()) !=null)
            {
                //string not null
                sb.append(line);//appending to string buffer
            }
            //converting sb to string and storing in data variable
            data = sb.toString();
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //code that executes regardless of exception
        finally {
            inputStream.close();
            urlConnection.disconnect();

        }
        return data;

    }
}
