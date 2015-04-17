package com.example.student.mylist;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    static final int READ_BLOCK_SIZE = 100;
    static String colorSelected=null;
    ListView lv = null;
    ArrayList<String> savedColors = new ArrayList<String>();
    static String path = null;
    static String defaultBkGdColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(fileInit()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyColors/" + "savedcolors.txt";
            File sdCardFile = new File(path);
            if (sdCardFile.exists()) {

                FileInputStream fIn = null;
                try {
                    fIn = new FileInputStream(sdCardFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStreamReader isr = new InputStreamReader(fIn);


                char[] inputBuffer = new char[READ_BLOCK_SIZE];
                String[] str=null;
                int charRead;
                try {
                    while ((charRead = isr.read(inputBuffer)) > 0) {
                        String readString = String.copyValueOf(inputBuffer, 0, charRead);
                        str = readString.split("#");
                        inputBuffer = new char[READ_BLOCK_SIZE];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for(String item : str) {
                    savedColors.add(item);
                }

                String saveddefaultBkGdColor = "#" + savedColors.get(1).replaceFirst("^00", "");
                View someView = findViewById(R.id.main);
                View root = someView.getRootView();
                root.setBackgroundColor(Color.parseColor(saveddefaultBkGdColor));
            } else {
                sdCardFile.mkdir();
            }


            String[] colourNames = getResources().getStringArray(R.array.listArray);
            ArrayAdapter<String> aA = new ArrayAdapter<String>(this, R.layout.list, colourNames);
            lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(aA);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //1.1 retrieve octal value from res string array
                    String[] colorVals = getResources().getStringArray(R.array.listValues);
                    //!!!!!!!!!!!!!!!!!!!! colorVals is Octal values, NOT Hexadecimal !!!!!!!!!!!!!!!!!!!
                    //String colorSelected = "#"+ colorVals[position].toUpperCase().replaceFirst("^00", "");//does work without toUpperCase()
                    //String colorSelected = colorVals[position].replaceFirst("^00", ""); //not working because # is missing

                    //1.2 convert the octal value into int for Color.parseColor() function
                    colorSelected = "#" + colorVals[position].replaceFirst("^00", "");//replaceFirst("^00", "") is used to trim off 00 in the octal string value

                    //int colorInt = Integer.parseInt(colorSelected); // not working because the hex string value does NOT have "#"
                    //Long colorInt = Long.parseLong(colorSelected, 16);//int type can not hold hex value
                    //int colorInt = Integer.parseInt(colorSelected.replaceFirst("^#",""), 16);//16: to interpret the value as hexadecimal value
                    //int colorInt = Color.parseColor("#" + "colorSelected");
                    //((TextView)view).setBackgroundColor(Color.pasrsehexString(colorSelected));

                    //1.3 set the bckgrnd color for activity main layout by using view.id
                    RelativeLayout main= (RelativeLayout)findViewById(R.id.main);
                    main.setBackgroundColor(Color.parseColor(colorSelected));
                }
            });

            //2.1 register the list view for a Context Menu
            registerForContextMenu(lv);
        }
    }

    //2.2
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Save the selected color");
        menu.add(0, v.getId(), 0, "Get the saved color");
        //menu.add(0, v.getId(), 0, "Delete this color");
        //menu.add(0, v.getId(), 0, "Delete all saved colors");
    }

    //2.3
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Save the selected color") {
            try {
                onClickSave();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Save the selected color", Toast.LENGTH_LONG).show();
        } else if (item.getTitle() == "Get the saved color") {
            onClickLoad();
            Toast.makeText(getApplicationContext(), "Get the saved color", Toast.LENGTH_LONG).show();
        }
      else {
            return false;
        }
        return true;
    }


    public void onClickSave() throws IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/MyColors/");

        if(!dir.exists()){
            dir.mkdir();
        }

        File file = new File(dir, "savedcolors.txt");

        if(!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fOut = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        try {
            osw.write(colorSelected);
            osw.flush();
            osw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(getBaseContext(), "Color: " + colorSelected + " is saved successfully!", Toast.LENGTH_LONG).show();
    }

    public void onClickLoad() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyColors/" + "savedcolors.txt");;

        try {
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fIn);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String[] str=null;
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                str = readString.split("#");
                inputBuffer = new char[READ_BLOCK_SIZE];
            }

            for(String item : str){
                savedColors.add(item);
            }

            ArrayAdapter<String> aB = new ArrayAdapter<String>(this, R.layout.savedcolorslist, savedColors);
            lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(aB);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    defaultBkGdColor = savedColors.get(position);
                    defaultBkGdColor = "#" + defaultBkGdColor;
                    RelativeLayout main= (RelativeLayout)findViewById(R.id.main);
                    main.setBackgroundColor(Color.parseColor(defaultBkGdColor));
                }
            });
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }


    public void onClickDelete(){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyColors/" + "savedcolors.txt");

        if (file.delete()) {
            Toast.makeText(getBaseContext(), "File deleted successfully.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getBaseContext(), "File not found.", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean fileInit() {
        boolean OK = false;
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;

        mExternalStorageAvailable = false;
        mExternalStorageWriteable = false;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            OK = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return OK;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
