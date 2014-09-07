/**
 * @author Deepak Ravishankar Ramkumar
 */
package com.deepak.expensetracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	CheckBox myExpense;
	CheckBox commonExpense;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		int[] drawable=new int[]{R.drawable.background1,R.drawable.background2,R.drawable.background3,R.drawable.background4,R.drawable.background5,R.drawable.background6,R.drawable.background7,R.drawable.background8};
		Random random=new Random();
		int index=random.nextInt(8);
		View background = findViewById(R.id.background);
		background.setBackgroundResource(drawable[index]);

		myExpense=(CheckBox) findViewById(R.id.PersonalExpense);
		myExpense.setChecked(true);
		commonExpense=(CheckBox) findViewById(R.id.CommonExpense);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//Method to write the expense to file
	public void saveExpenseToFile(View v){
		String fileName=null;
		EditText expenseName=(EditText)findViewById(R.id.expensename);
		EditText expenseAmount=(EditText)findViewById(R.id.expensevalue);

		if(expenseName.getText().toString().length() > 0 && expenseAmount.getText().toString().length() >0){
			fileName=getFileName();
			if(fileName.equals("common-expenses.txt")){
				sendSMS("7164358351","Message From Expense Tracker Application\r\nCommon Expenses\r\n"+expenseName.getText().toString()+"   "+expenseAmount.getText().toString()+"$");
				sendSMS("4696488107","Message From Expense Tracker Application\r\nCommon Expenses\r\n"+expenseName.getText().toString()+"   "+expenseAmount.getText().toString()+"$");
				//sendSMS("7164309669","Message From Expense Tracker Application\r\nCommon Expenses\r\n"+expenseName.getText().toString()+"   "+expenseAmount.getText().toString()+"$");
			}
			writeToFile(expenseName.getText().toString(),expenseAmount.getText().toString(),fileName);
			expenseName.setText("");
			expenseAmount.setText("");
			expenseName.requestFocus();
			showToastMessage("Expense Saved");
		}
		else{
			showToastMessage("Enter value for all fields to proceed.");
		}
	}

	//Method to save the expenses file to external memory
	public void getTotalExpensesFromFile(View v){
		int i=0;
		double total=0;
		String fileName=getFileName();
		String response=readFromInternalStorage(fileName);
		if(response!=null){
			String fileContents[]=response.split("%%%");
			String root=Environment.getExternalStorageDirectory().toString();
			File directory=new File(root + "/expenseTracker");
			directory.mkdirs();
			File file=new File(directory,fileName);
			try {
				FileOutputStream fos=new FileOutputStream(file);
				fos.write("                         Expense List\r\n".getBytes());
				while(i<fileContents.length){
					fos.write((fileContents[i].toString()+"          "+fileContents[i+1].toString()+"$\r\n").getBytes());
					total+=Double.parseDouble(fileContents[i+1]);
					i=i+2;
				}
				fos.write(("\r\nTotal is "+String.valueOf(total)+"$").getBytes());
				fos.close();
				
				showToastMessage("Expense Report saved to phone storage.");
				Intent intent=new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),"text/plain");
				startActivity(intent);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			showToastMessage("No Expense List present currently.");
		}
	}

	//Function to delete all files in internal storage.
	public void deleteCurrentExpenseList(View v){
		String list[]=getApplicationContext().fileList();
		final String fileName=getFileName();
		boolean flag=false;
		for(int i=0;i<list.length;i++){
			if(list[i].equals(fileName)){
				flag=true;
				break;
			}
		}
		if(!flag){
			showToastMessage("There are no files to be deleted.");
		}
		else{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Delete Expense List");
			alertDialogBuilder.setMessage("Are you sure about deleting all the saved expenses?");

			alertDialogBuilder.setPositiveButton("Yes", 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					getApplicationContext().deleteFile(fileName);
					showToastMessage("Delete Successful. You can now start a new Expense list.");
				}
			});

			alertDialogBuilder.setNegativeButton("No", 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}

	//Function to write files to internal storage.
	public void writeToFile(String expenseName, String expenseValue,String fileName){
		try {
			FileOutputStream fos=this.getApplicationContext().openFileOutput(fileName,Context.MODE_APPEND);
			fos.write((expenseName.trim()+"%%%"+expenseValue.trim()+"%%%").getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Function to read file from internal storage.
	public String readFromInternalStorage(String fileName){
		String fileContents=null;
		try {
			if(this.getApplicationContext().fileList().length >0){
				FileInputStream fis=this.getApplicationContext().openFileInput(fileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis)); 
				fileContents=br.readLine();
				br.close();                                                         
				fis.close();
			}
			else{
				showToastMessage("No Expense List present currently.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContents;
	}

	//Method to send Message.
	private void sendSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	private String getFileName(){
		String fileName=null;
		if(myExpense.isChecked())
			fileName="expenses.txt";
		else
			fileName="common-expenses.txt";
		return fileName;
	}

	//Method to display a Toast Message to user.
	public void showToastMessage(String message){
		Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
	}

	//Method to deselect the checkboxes when the other is selected.
	public void deselectPersonalCheckBox(View v){
		myExpense.setChecked(false);
	}
	public void deselectCommonCheckBox(View v){
		commonExpense.setChecked(false);
	}
}
