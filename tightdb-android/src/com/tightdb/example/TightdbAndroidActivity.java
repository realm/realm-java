package com.tightdb.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.csvreader.CsvReader;
import com.tightdb.generated.*;

import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

import com.tightdb.Group;
import com.tightdb.TableBase;

import org.stockchart.StockChartView;
import org.stockchart.core.Area;
import org.stockchart.core.Axis;
import org.stockchart.core.Axis.ILabelFormatProvider;
import org.stockchart.core.Axis.Side;
import org.stockchart.core.AxisRange;
import org.stockchart.points.CustomPoint;
import org.stockchart.points.StockPoint;
import org.stockchart.series.AbstractSeries;
import org.stockchart.series.BarSeries;
import org.stockchart.series.LinearSeries;
import org.stockchart.series.StockSeries;

public class TightdbAndroidActivity extends Activity {
	
	@Table
	class stocka{
		String Date;
		int Open;
		int High;
		int Low;
		int Close;
		int Volume;
		int Adj_Close;
	}

	private Handler mHandler = new Handler();
	private StockSeries s1 = new StockSeries();

	private static long tttt = 0;
	StockChartView s;
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			File dir = Environment.getExternalStorageDirectory();
			File file = new File(dir, "NOK.tightdb");

			Group group = new Group(file);
			StockaTable stocks = new StockaTable(group);

			
			for (Stocka stock : stocks) {
			    //Log.i("STOCK", "BLA!" + stock.getDate() +" " + Long.toString(stock.getHigh()) );
				StockPoint ss = new StockPoint(++tttt);
				ss.setLow(stock.getLow());
				ss.setHigh(stock.getHigh());
				ss.setClose(stock.getClose());
				ss.setOpen(stock.getOpen());
				s1.addPoint(ss);
			}

			s.invalidate();

		}
	};



	@Override
	public void onCreate(Bundle savedInstanceState) {

/*		
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
*/
		File dir = Environment.getExternalStorageDirectory();
		File file = new File(dir, "NOK.tightdb");

		Group group = new Group(file);
//		Group group = new Group();
//		Log.i("TDB", Long.toString(group.getTableCount()) );
		StockaTable stocks = new StockaTable(group);
//		stocks.add("test", 1, 2, 3, 4, 5, 6);

		
		
		
		
		/*
		
		
		
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
		    group.writeToFile("sdcard/NOK.tightdb");
		} catch (IOException e) {
		    e.printStackTrace();
		} 
		
		
		
		*/
		
		
		Log.i("TDB1", Long.toString(stocks.last().getLow()));
		Log.i("TDB1", "name: "+stocks.getName() );
		Log.i("TDB1", "VALID: "+stocks.isValid() );
		
/*		for (Stocka stock : stocks) {
		    Log.i("STOCK", "BLA!" + stock.getDate() +" " + Long.toString(stock.getHigh()) );
		}
*/
		// Write to disk
		/*try {
		    group.writeToFile("sdcard/stocksa.tightdb");
		} catch (IOException e) {
		    e.printStackTrace();
		} 
		File file_new = new File(dir, "stocksa.tightdb");
		Group group_new = new Group(file_new);
		StockaTable stocks_new = new StockaTable(group_new);
		Log.i("TDB1_new", Long.toString(stocks_new.last().getLow()));
*/

		super.onCreate(savedInstanceState);
		
		s = new StockChartView(this);

		AxisRange ar = new AxisRange();
		ar.setMovable(true);
		ar.setZoomable(true);

		s.enableGlobalAxisRange(Axis.Side.BOTTOM, ar);

		Area a1 = new Area();
		Area a2 = new Area();
		a1.getRightAxis().setLabelFormatProvider(new ILabelFormatProvider()
		{
			@Override
			public String getAxisLabel(Axis sender, double value) {
				return String.valueOf(value);
			}
		});

		a2.setAutoHeight(false);
		a2.setHeightInPercents(0.2f);
		a2.getBottomAxis().setLabelFormatProvider(new ILabelFormatProvider() 
		{
			@Override
			public String getAxisLabel(Axis sender, double value) 
			{			
				Area a = sender.getParent();

				for(int i=0;i<a.getSeriesCount();i++)
				{
					AbstractSeries s = a.getSeriesAt(i);

					int index = s.convertToArrayIndex(value);
					if(index >=0 && index < s.getPointCount())
					{
						Object id = s.getPointAt(index).getID();

						if(null != id)
							return id.toString();
					}
				}

				return null;
			}
		});

		a1.getLeftAxis().setVisible(false);
		a1.getTopAxis().setVisible(false);

		a2.getLeftAxis().setVisible(false);
		a2.getTopAxis().setVisible(false);

		a1.addSeries(s1);

		s.addArea(a1);

		setContentView(s, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}
}