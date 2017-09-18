package com.nishan.floatingflash;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
/**
 * Created by Nishan Chathuranga on 9/17/2017.
 */
public class Main extends AppCompatActivity {
    public static int OVERLAY_PERMISSION_REQ_CODE_FLOATER = 1234;
    public static int OVERLAY_PERMISSION_REQ_CODE_MSG = 5678;
    public static Button btnStartService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnStartService = (Button)findViewById(R.id.btnStartService);

        btnStartService.setOnClickListener(lst_StartService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    Button.OnClickListener lst_StartService = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.d(Utils.LogTag, "lst_StartService -> Utils.canDrawOverlays(Main.this): " + Utils.canDrawOverlays(Main.this));

            if (Utils.canDrawOverlays(Main.this)){
                startFloater();
                //finish();
            }
            else{
                requestPermission(OVERLAY_PERMISSION_REQ_CODE_FLOATER);
            }
        }

    };


    private void startFloater(){
        startService(new Intent(Main.this, FlashService.class));
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.setCancelable(false);
        builder.show();
    }



    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();


    }

    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_FLOATER) {
            if (!Utils.canDrawOverlays(Main.this)) {
                needPermissionDialog(requestCode);
            }else{
                startFloater();
            }

        }else if(requestCode == OVERLAY_PERMISSION_REQ_CODE_MSG){
            if (!Utils.canDrawOverlays(Main.this)) {
                needPermissionDialog(requestCode);
            }else{
               //
            }

        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_abtme) {
            Intent browserintent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.linkedin.com/in/nishanchathuranga"));
            startActivity(browserintent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
