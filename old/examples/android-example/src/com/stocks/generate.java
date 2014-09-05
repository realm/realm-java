package com.stocks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

import com.csvreader.CsvReader;
import io.realm.Group;
import io.realm.example.StockaTable;

public class generate {
    public void stocks() {
        File mfile=new File("/");
        File[] list=mfile.listFiles();

        for(int i=0;i<mfile.listFiles().length;i++)
        {
            Log.v("File", list[i].getAbsolutePath());
            if(list[i].isHidden())
            {
                // Log.v("hidden", list[i].getAbsolutePath());
            }
        }
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, "NOK.realm");

        Group group = new Group(file);
        //  Group group = new Group();
        //  Log.i("TDB", Long.toString(group.size()) );
        StockaTable stocks = new StockaTable(group);

        try {

            CsvReader stock = new CsvReader("/sdcard/rawdata/NOK_20120710.csv");

            stock.readHeaders();

            while (stock.readRecord())
            {
                String Date     = stock.get("Date");
                int Open        = (int) Math.round(new Float(stock.get("Open"))*100);
                int High        = (int) Math.round(new Float(stock.get("High"))*100);
                int Low         = (int) Math.round(new Float(stock.get("Low"))*100);
                int Close       = (int) Math.round(new Float(stock.get("Close"))*100);
                int Volume      = Integer.parseInt(stock.get("Volume"));
                int Adj_Close   = (int) Math.round(new Float(stock.get("Adj Close"))*100);


                // perform program logic here
                System.out.println(Date + " : " + Open + " : " + High + " : " + Low + " : " + Close + " : " + Volume + " : " + Adj_Close);
                stocks.add(Date, Open, High, Low, Close, Volume, Adj_Close);
            }

            stock.close();
            System.out.println(Long.toString(stocks.last().getLow()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Write to disk
        try {
            group.writeToFile("sdcard/NOK.realm");
        } catch (IOException e) {
            e.printStackTrace();
        }





        // Log.i("TDB1", Long.toString(stocks.last().getLow()));
        //  Log.i("TDB1", "name: "+stocks.getName() );
        //  Log.i("TDB1", "VALID: "+stocks.isValid() );

        /*      for (Stocka stock : stocks) {
        Log.i("STOCK", "BLA!" + stock.getDate() +" " + Long.toString(stock.getHigh()) );
    }
         */
        // Write to disk
        /*try {
        group.writeToFile("sdcard/stocksa.realm");
    } catch (IOException e) {
        e.printStackTrace();
    }
    File file_new = new File(dir, "stocksa.realm");
    Group group_new = new Group(file_new);
    StockaTable stocks_new = new StockaTable(group_new);
    Log.i("TDB1_new", Long.toString(stocks_new.last().getLow()));
         */
    }
}
