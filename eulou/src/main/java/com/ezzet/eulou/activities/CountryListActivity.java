package com.ezzet.eulou.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.extra.CustomListAdapter;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.Code;

public class CountryListActivity extends BaseActivity {

    ListView list;
    HelperFunction HF;
    ArrayList<Code> codeList;
    ProgressDialog pDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);
        init();
        pDialog = new ProgressDialog(CountryListActivity.this);
        pDialog.setMessage("Please Wait...");
        pDialog.show();
        setData();
        setClickListner();

    }

    private void setClickListner() {
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("cName", codeList.get(position).getName());
                intent.putExtra("cCode", codeList.get(position).getCallingCodes());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void setData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                codeList = HF.getInforamation();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (codeList.size() > 0) {
                            list.setAdapter(new CustomListAdapter(CountryListActivity.this, codeList));
                            pDialog.dismiss();
                        } else {
                            Toast.makeText(CountryListActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                            pDialog.dismiss();
                        }

                    }
                });
            }
        }).start();

    }

    private void init() {
        list = (ListView) findViewById(R.id.listViewCalls);
        HF = new HelperFunction(CountryListActivity.this);
        codeList = new ArrayList<Code>();
    }

    class AsyncCountry extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            init();
            pDialog = new ProgressDialog(CountryListActivity.this);
            pDialog.setMessage("Please Wait...");
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            codeList = HF.getInforamation();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (codeList.size() > 0) {
                        list.setAdapter(new CustomListAdapter(CountryListActivity.this, codeList));
                        pDialog.dismiss();
                    } else {
                        Toast.makeText(CountryListActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }

                }
            });
            return null;
        }

        protected void onPostExecute(String file_url) {
            // Dismiss the dialog

            pDialog.dismiss();
        }

    }
}
