package com.iLikeSwag.android.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SwagReaderActivity extends ListActivity {

	List<String> headlines;
	List<String> links;
	ProgressDialog progress;
	ArrayAdapter<String> adapter;
	static final int DIALOG_ERROR_CONNECTION = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swag_reader);

		// initialize
		headlines = new ArrayList<String>();// check parameters for these
		links = new ArrayList<String>();

		// check if online
		if (!isOnline(this)) {
			showDialog(DIALOG_ERROR_CONNECTION); // displaying the created
			// dialog.
		} else {
			// Internet available.
			// parse XML in new thread
			new XmlTask().execute();
		}

	}// onCreate

	public boolean isOnline(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni != null && ni.isConnected())
			return true;
		else
			return false;
	}// isOnline?

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_ERROR_CONNECTION:
			AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
			errorDialog.setTitle("Error");
			errorDialog.setMessage("No internet connection.");
			errorDialog.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});

			AlertDialog errorAlert = errorDialog.create();
			return errorAlert;

		default:
			break;
		}
		return dialog;
	}// onCreateDialog

	// sets input stream
	public InputStream getInputStream(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
			return null;
		}
	}// getInputStream

	public void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = Uri.parse((String) links.get(position));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}// onListItemClick

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_swag_reader, menu);
		return true;
	}// getInputStream

	private class XmlTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(SwagReaderActivity.this, "",
					"Loading Swag. Please Wait...");
		}// onPreExecute

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://www.ilikeswag.com/feed/");

				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				factory.setNamespaceAware(false);
				XmlPullParser xpp = factory.newPullParser();

				// get the XML from an input stream
				xpp.setInput(getInputStream(url), "UTF_8");

				// marks if we're past the channel title tag
				boolean insideItem = false;

				// parse the XML
				int eventType = xpp.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {

					if (eventType == XmlPullParser.START_TAG) {

						if (xpp.getName().equalsIgnoreCase("item")) {
							insideItem = true;
						} else if (xpp.getName().equalsIgnoreCase("title")) {
							if (insideItem)
								headlines.add(xpp.nextText());// extract
																// headline
						} else if (xpp.getName().equalsIgnoreCase("link")) {
							if (insideItem)
								links.add(xpp.nextText());
						}

					} else if (eventType == XmlPullParser.END_TAG
							&& xpp.getName().equalsIgnoreCase("item")) {
						insideItem = false;
					}

					eventType = xpp.next();// move to next element

				}// while
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}// doInBackground

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			// binding data
			adapter = new ArrayAdapter<String>(SwagReaderActivity.this,
					android.R.layout.simple_list_item_1, headlines);
			setListAdapter(adapter);
			progress.dismiss();
		}// onPostExecute

	}// XmlTask

}// Activity
