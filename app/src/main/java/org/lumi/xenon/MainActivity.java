package org.lumi.xenon;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button save_to_external_storage = findViewById(R.id.save_to_external_storage);
        save_to_external_storage.setOnClickListener(this);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            save_to_external_storage.setEnabled(false);

        }
        else {
            myExternalFile =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        }

    }

    @Override
    public void onClick(View v) {
        view = v;
        int id = v.getId();

        switch (id) {
            case R.id.save_to_external_storage:
                if (!checkPermission()) {
                    requestPermission();
                    useReflection();

                } else {
                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                    useReflection();

                }
                break;

        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readExternalStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (readExternalStorageAccepted && writeExternalStorageAccepted)
                        Snackbar.make(
                                view,
                                "Permission Granted, Now you can R/W to external storage.",
                                Snackbar.LENGTH_LONG).show();

                    else {
                        Snackbar.make(
                                view,
                                "Permission Denied, You cannot R/W to external storage.",
                                Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel(
                                        "You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(
                                                            new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }

                                            }

                                        });

                                return;

                            }

                        }

                    }

                }

                break;

        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    @SuppressWarnings("Duplicates")
    private void useReflection() {
        //counter variables
        int intCounter = 0;
        int stringCounter = 0;
        int nonIntOrStringCounter = 0;
        String[] methodsTbl;

        //Make reflection call
        //String className = "android.content.Context";
        AccountManager varClass = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        Class classToInvestigate = null;
        try {
            //classToInvestigate = Class.forName(className);
            classToInvestigate = Class.forName(varClass.getClass().getName());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }

        Method[] checkMethods = classToInvestigate.getDeclaredMethods();//Inherited methods are excluded
        methodsTbl = new String[checkMethods.length];

        int i = 0;
        for(Method m : checkMethods) {
            // Found a method m
            methodsTbl[i] = m.getName();
            //Get method parameters
            methodParameters.put(methodsTbl[i], getParameterNames(m));
            i++;

        }

        try {
            FileOutputStream fos = new FileOutputStream(myExternalFile);
            //Classic way
            for (Map.Entry<String, List<String>> elements : methodParameters.entrySet()) {
                fos.write("[".getBytes());
                fos.write(elements.getKey().getBytes()); //Write method
                fos.write("(".getBytes());
                for (String element :elements.getValue()) {
                    fos.write(element.getBytes()); //Write method parameters
                    fos.write(";".getBytes());

                }

                fos.write(")".getBytes());
                fos.write("]".getBytes());
                fos.write(System.getProperty("line.separator").getBytes());
                fos.write("<===================================>".getBytes());
                fos.write(System.getProperty("line.separator").getBytes());

                //==================================================================================
                //Executing found method using reflection
                //==================================================================================
                for (String element :elements.getValue()) {
                    if (element.equals("int")) {
                        intCounter++;

                    }  else if (element.equals("java.lang.String")) {
                        stringCounter++;

                    } else if (!element.equals("int") || !element.equals("java.lang.String")){
                        nonIntOrStringCounter++;

                    }

                }

                //Execute method call with specific int or string parameters
                if ((intCounter > 0) && (stringCounter == 0)
                        && (nonIntOrStringCounter == 0)) { //only int parameters
                    Log.i(TAG, "Executing methods with MIN/MAX int values...");
                    executeMethodCallWithIntParam(
                            elements,
                            classToInvestigate,
                            varClass,
                            fos,
                            intCounter);

                }

                if ((stringCounter > 0) && (intCounter == 0)
                        && (nonIntOrStringCounter == 0)) { //only string parameters
                    Log.i(TAG, "Executing methods with MIN/MAX String values...");
                    executeMethodCallWithStringParam(
                            elements,
                            classToInvestigate,
                            varClass,
                            fos,
                            stringCounter);

                }

                if ((intCounter > 0) && (stringCounter > 0)
                        && (nonIntOrStringCounter == 0)) { //Mixed int & string parameters
                    Log.i(TAG, "Executing methods with MIN/MAX int & String values...");
                    executeMethodCallWithIntAndStringParam(
                            elements,
                            classToInvestigate,
                            varClass,
                            fos,
                            intCounter,
                            stringCounter);

                }

                fos.write(System.getProperty("line.separator").getBytes());
                fos.write("<===================================>".getBytes());
                fos.write(System.getProperty("line.separator").getBytes());
                fos.write(System.getProperty("line.separator").getBytes());
                //Zero-out all counters for next repetition
                intCounter = 0;
                stringCounter = 0;
                nonIntOrStringCounter = 0;

            }

            //Using streams
            /*methodParameters.entrySet().stream().forEach(item ->
                    System.out.println(item.getKey() + ": " + item.getValue());
            );*/

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

        Snackbar.make(view,
                "ClassComponents.txt saved to External Storage...",
                Snackbar.LENGTH_LONG).show();

    }

    /*Return Methods parameter names*/
    private static List<String> getParameterNames(Method method) {
        Class[] parameterTypes = method.getParameterTypes();
        //Parameter[] parameters = parameterTypes;
        List<String> parameterNames = new ArrayList<>();

        for (Class parameter : parameterTypes) {
            String parameterName = parameter.getName();
            parameterNames.add(parameterName);

        }

        return parameterNames;

    }

    private void executeMethodCallWithIntParam(Map.Entry<String, List<String>> elements,
                                               Class classToInvestigate,
                                               AccountManager varClass, FileOutputStream fos,
                                               int intCounter) throws IOException {
        Class[] paramInt;
        //Class classToInvestigate = null;
        Method method;
        //Object returnValue;
        Integer [] tempIntMinArray = new Integer[intCounter];
        Integer [] tempIntMaxArray = new Integer[intCounter];

        paramInt = new Class[intCounter];
        for (int j = 0; j < intCounter; j++) {
            paramInt[j] = Integer.TYPE; //int parameter type

        }

        //Test for Min int value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), paramInt);
            Log.i(TAG, "Method invoked:"  + method);
            //obj = classToInvestigate.newInstance();
            for (int i = 0; i < tempIntMinArray.length; i++) {
                tempIntMinArray[i] = Integer.MIN_VALUE;

            }

            method.invoke(varClass, tempIntMinArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | InvocationTargetException
                | IllegalAccessException e) {
            fos.write(e.toString().getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        }

        //Test for Max int value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), paramInt);
            //obj = classToInvestigate.newInstance();
            for (int i = 0; i < tempIntMaxArray.length; i++) {
                tempIntMaxArray[i] = Integer.MAX_VALUE;

            }

            method.invoke(varClass, tempIntMaxArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            fos.write(e.toString().getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        }

    }

    private void executeMethodCallWithStringParam(Map.Entry<String, List<String>> elements,
                                                  Class classToInvestigate,
                                                  AccountManager varClass,
                                                  FileOutputStream fos, int stringCounter)
            throws IOException {
        Class[] paramString;
        //Class classToInvestigate = null;
        Method method;
        //Object returnValue;
        String [] tempStringMinArray = new String[stringCounter];
        String [] tempStringMaxArray = new String[stringCounter];

        paramString = new Class[stringCounter];
        for (int j = 0; j < stringCounter; j++) {
            paramString[j] = String.class; //String parameter type

        }

        //Test for Min string value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), paramString);
            Log.i(TAG, "Method invoked:"  + method);
            //obj = classToInvestigate.newInstance();
            for (int i = 0; i < tempStringMinArray.length; i++) {
                tempStringMinArray[i] = "abc";

            }

            method.invoke(varClass, tempStringMinArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            fos.write(e.toString().getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        }

        //Test for Max string value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), paramString);
            //obj = classToInvestigate.newInstance();
            //String repeated = new String(new char[2147483647]).replace("\0", "c");
            String repeated = new String(new char[21474836]).replace("\0", "c");
            for (int i = 0; i < tempStringMaxArray.length; i++) {
                tempStringMaxArray[i] = repeated;

            }

            method.invoke(varClass, tempStringMaxArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            fos.write(e.toString().getBytes());

        }

    }

    private void executeMethodCallWithIntAndStringParam(Map.Entry<String, List<String>> elements,
                                                        Class classToInvestigate,
                                                        AccountManager varClass,
                                                        FileOutputStream fos, int intCounter,
                                                        int stringCounter) throws IOException {
        /*Class[] paramInt;
        Class[] paramString;*/

        Method method;
        Object[] obj = {elements.getValue()};
        Class<?> params[] = new Class[obj.length];
        Integer [] tempIntMinArray = new Integer[intCounter];
        Integer [] tempIntMaxArray = new Integer[intCounter];
        String [] tempStringMinArray = new String[stringCounter];
        String [] tempStringMaxArray = new String[stringCounter];

        for (int i = 0; i < obj.length; i++) {
            if (obj[i] instanceof Integer) {
                params[i] = Integer.TYPE;

            } else if (obj[i] instanceof String) {
                params[i] = String.class;

            }

        }

        /*paramInt = new Class[intCounter];
        for (int j = 0; j < intCounter; j++) {
            paramInt[j] = Integer.TYPE; //int parameter type

        }

        paramString = new Class[stringCounter];
        for (int j = 0; j < stringCounter; j++) {
            paramString[j] = String.class; //String parameter type

        }*/

        //Test for Min int & String value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), params);
            Log.i(TAG, "Method invoked:"  + method);

            for (int i = 0; i < tempIntMinArray.length; i++) {
                tempIntMinArray[i] = Integer.MIN_VALUE;

            }

            method.invoke(varClass, tempIntMinArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | InvocationTargetException
                | IllegalAccessException e) {
            fos.write(e.toString().getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        }

        //Test for Max int & String value
        try {
            method = classToInvestigate.getDeclaredMethod(elements.getKey(), params);
            for (int i = 0; i < tempIntMaxArray.length; i++) {
                tempIntMaxArray[i] = Integer.MAX_VALUE;

            }

            method.invoke(varClass, tempIntMaxArray);
            fos.write("Executed...!".getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            fos.write(e.toString().getBytes());
            fos.write(System.getProperty("line.separator").getBytes());

        }

    }

    /* Checks if external storage is available for read and write*/
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;

        }

        return false;

    }

    /* Checks if external storage is available to at least read*/
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;

        }

        return false;

    }

    //=================================================================================================================
    //Class variables
    //=================================================================================================================
    private View view;
    private File myExternalFile;
    String filename = "ClassComponents.txt";
    private Map<String, List<String>> methodParameters = new HashMap<>();  //Create map to store methods parameters
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = "Xenon";

}
