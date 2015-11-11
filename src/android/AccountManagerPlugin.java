// Copyright (C) 2013 Polychrom Pty Ltd
//
// This program is licensed under the 3-clause "Modified" BSD license,
// see LICENSE file for full definition.

package com.qt.cordova;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;

//below imports for checking contacts with server.
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import java.util.ArrayList;
import android.widget.Toast;

/**! Android AccountManager plugin for Cordova
 *
 * @author Mitchell Wheeler
 *
 *	Implements a basic Android AccountManager plugin for Cordova with support for common account management routines.
 *
 *	Features not currently supported are:
 *  *  Account features
 *  *  Automatic Authentication via AccountManagers (only explicit accounts and auth tokens are supported currently)
 */
public class AccountManagerPlugin extends CordovaPlugin
{
	AccountManager manager = null;

	// Naive int to account mapping so our JS can simply reference native objects
	Integer accumulator = 0;
	HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();
	
	private Integer indexForAccount(Account account)
	{
		for(Entry<Integer, Account> e: accounts.entrySet())
		{
			if(e.getValue().equals(account))
			{
				return e.getKey();
			}
		}
		
		accounts.put(accumulator, account);
		return accumulator++;
	}
	
	
	
	private boolean syncContacts(){
		try {
	            Account[] account_list = manager.getAccountsByType("Qt");
	            //JSONArray result = new JSONArray();
	            Account account;
	            if (account_list == null) {
	                account = new Account("QtUser", "Qt");
	                //Integer index = indexForAccount(account);
	                manager.addAccountExplicitly(account, "password", null);
	            }
	            //else
	            //    account = account_list[0];
	            //fetch phone contacts and check with server if any contact has Qt. If yes, create a new contact in Qt account.
	            //use contentresolver().insert() to insert ContentValues.
	
	            String[] mProjection =
	            {
	                    // ContactsContract data table data1 column name
	                    ContactsContract.CommonDataKinds.Phone.NUMBER
	            };
	
	            //get contact from phone
	            // A "projection" defines the columns that will be returned for each row
	
	            /*
				  * This defines a one-element String array to contain the selection argument.
				  */
	            //String[] mSelectionArgs = {""};
	            // Remember to insert code here to check for invalid or malicious input.
	
	            // Constructs a selection clause that matches the contact_id from data table.
	            //mSelectionClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
	            // Get data for 1st record
	            //mSelectionArgs[0] = 1;
	
	            // Does a query against the table and returns a Cursor object
	            Cursor mCursor = getContentResolver().query(
	                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // URI
	                    null,                 // The columns to return for each row
	                    null,          // Data for first contact
	                    null,              // first contact
	                    null                   // The sort order for the returned rows
	            );
	
	            // Some providers return null if an error occurs, others throw an exception
	            if (null == mCursor) {
	
	                // Insert code here to handle the error. Be sure not to use the cursor! You may want to
	                // call android.util.Log.e() to log this error.
	                Log.i("Phone", "query returned null");
	
	                // If the Cursor is empty, the provider found no matches
	            } else if (mCursor.getCount() < 1) {
	
	                // Insert code here to notify the user that the contact query was unsuccessful. This isn’t necessarily
	                // an error. You may want to offer the user the option to insert a new row, or re-type the
	                // search term.
	                Log.i("Phone", "query returned 0 rows");
	
	
	            } else {
	
	                // Insert code here to do something with the results
	
	                // Moves to the next row in the cursor. Before the first movement in the cursor, the
	                // "row pointer" is -1, and if you try to retrieve data at that position you will get an
	                // exception.
	                mCursor.moveToFirst();
	                for(int i=0;i<mCursor.getCount();i++) {
	
	                    // Gets the value from the column.
	
	                    String id = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
	                    String cNumber = "";
	                    String nameContact = "";
	
	                    String hasPhone = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
	
	                    if (hasPhone.equalsIgnoreCase("1")) {
	                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
	                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
	                        //phones.moveToFirst();
	
	                        while (phones.moveToNext())
	                            cNumber += phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "|";
	                        Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();
	
	                        nameContact = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
	
	                        Toast.makeText(getApplicationContext(), nameContact + " " + cNumber, Toast.LENGTH_SHORT).show();
	                    }
	
	
	                    //FIRE AJAX CALL HERE TO CHECK IF PHONE NUMBER HAS A QT TAG ON SERVER. *********
	                    //IF YES, CONTINUE BELOW.
	
	                    String qtNumber = "returned from ajax";
	                    // Creates a new intent for sending to the device's contacts application
	                    Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
	
	                    // Sets the MIME type to the one expected by the insertion activity
	                    insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
	
	                    // Sets the new contact name
	                    insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, nameContact);
	
	                    // Defines an array list to contain the ContentValues objects for each row
	                    ArrayList<ContentValues> contactData = new ArrayList<ContentValues>();
	
	                    // Sets up the row as a ContentValues object
	                    ContentValues rawContactRow = new ContentValues();
	
	                    // Adds the account type and name to the row
	                    rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "Qt");
	                    rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_NAME, "QtUser");
	
	                    // Adds the row to the array
	                    contactData.add(rawContactRow);
	                    // Sets up the row as a ContentValues object
	                    ContentValues phoneRow = new ContentValues();
	
	                    // Specifies the MIME type for this data row (all data rows must be marked by their type)
	                    phoneRow.put(
	                            ContactsContract.Data.MIMETYPE,
	                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
	                    );
	
	                    // Adds the phone number and its type to the row
	                    phoneRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, qtNumber);
	
	                    // Adds the row to the array
	                    contactData.add(phoneRow);
	
				/*
				 * Adds the array to the intent's extras. It must be a parcelable object in order to
				 * travel between processes. The device's contacts app expects its key to be
				 * Intents.Insert.DATA
				 */
	                    insertIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);
	
	                    // Send out the intent to start the device's contacts app in its add contact activity.
	                    startActivity(insertIntent);
	
	
	                    // end of while loop
	                }
	            }
	
	
	        }
	        catch(Exception err){
	            Log.i("Phone", "error - catch"+err);
	
	        }
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
	{
		if(manager == null)
		{
			manager = AccountManager.get(cordova.getActivity());
		}

		try
		{
			if("getAccountsByType".equals(action))
			{
				Account[] account_list = manager.getAccountsByType(args.isNull(0)? null : args.getString(0));
				JSONArray result = new JSONArray();
				
				for(Account account: account_list)
				{
					Integer index = indexForAccount(account);
	
					JSONObject account_object = new JSONObject();
					account_object.put("_index", (int)index);
					account_object.put("name", account.name);
					account_object.put("type", account.type);
					result.put(account_object);
				}
	
				callbackContext.success(result);
				return true;
			}
			else if("addAccountExplicitly".equals(action))
			{
				if(args.isNull(0) || args.getString(0).length() == 0)
				{
					callbackContext.error("accountType can not be null or empty");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("username can not be null or empty");
					return true;
				}
				else if(args.isNull(2) || args.getString(2).length() == 0)
				{
					callbackContext.error("password can not be null or empty");
					return true;
				}
	
				Account account = new Account(args.getString(1), args.getString(0));
				Integer index = indexForAccount(account);
	
				Bundle userdata = new Bundle();
				if(!args.isNull(3))
				{
					JSONObject userdata_json = args.getJSONObject(3);
					if(userdata_json != null)
					{
						Iterator<String> keys = userdata_json.keys();
						while(keys.hasNext())
						{
							String key = keys.next();
							userdata.putString(key, userdata_json.getString(key));
						}
					}
				}
	
				if(false == manager.addAccountExplicitly(account, args.getString(2), userdata))
				{
					callbackContext.error("Account with username already exists!");
					return true;
				}
				
				JSONObject result = new JSONObject();
				result.put("_index", (int)index);
				result.put("name", account.name);
				result.put("type", account.type);
	
				callbackContext.success(result);
				return true;
			}
			else if("updateCredentials".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
	
				callbackContext.error("Not yet implemented");
				return true;
			}
			else if("clearPassword".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				manager.clearPassword(account);
				callbackContext.success();
				return true;
			}
			else if("removeAccount".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				
				int index = args.getInt(0);
				Account account = accounts.get(index);
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				// TODO: Add support for AccountManager (callback)
				AccountManagerFuture<Boolean> future = manager.removeAccount(account, null, null);
				try
				{
					if(future.getResult() == true)
					{
						accounts.remove(index);
						callbackContext.success();
					}
					else
					{
						callbackContext.error("Failed to remove account");
					}
				}
				catch (OperationCanceledException e)
				{
					callbackContext.error("Operation canceled: " + e.getLocalizedMessage());
				}
				catch (AuthenticatorException e)
				{
					callbackContext.error("Authenticator error: " + e.getLocalizedMessage());
				}
				catch (IOException e)
				{
					callbackContext.error("IO error: " + e.getLocalizedMessage());
				}
				
				return true;
			}
			else if("setAuthToken".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("authTokenType can not be null or empty");
					return true;
				}
				else if(args.isNull(2) || args.getString(2).length() == 0)
				{
					callbackContext.error("authToken can not be null or empty");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				manager.setAuthToken(account, args.getString(1), args.getString(2));
				callbackContext.success();
				return true;
			}
			else if("peekAuthToken".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("authTokenType can not be null or empty");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				JSONObject result = new JSONObject();
				result.put("value", manager.peekAuthToken(account, args.getString(1)));
				callbackContext.success(result);
				return true;
			}
			else if("getAuthToken".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("authTokenType can not be null or empty");
					return true;
				}
				else if(args.isNull(3))
				{
					callbackContext.error("notifyAuthFailure can not be null");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				Bundle options = new Bundle();
				// TODO: Options support (will be relevent when we support AccountManagers)
				
				// TODO: AccountManager support
				AccountManagerFuture<Bundle> future = manager.getAuthToken(account, args.getString(1), options, args.getBoolean(3), null, null);
				try
				{
					JSONObject result = new JSONObject();
					result.put("value", future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
					callbackContext.success(result);
				}
				catch (OperationCanceledException e)
				{
					callbackContext.error("Operation canceled: " + e.getLocalizedMessage());
				}
				catch (AuthenticatorException e)
				{
					callbackContext.error("Authenticator error: " + e.getLocalizedMessage());
				}
				catch (IOException e)
				{
					callbackContext.error("IO error: " + e.getLocalizedMessage());
				}
				
				return true;
			}
			else if("setPassword".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("password can not be null or empty");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
				
				manager.setPassword(account, args.getString(1));
				callbackContext.success();
				return true;
			}
			else if("getPassword".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
	
				JSONObject result = new JSONObject();
				result.put("value", manager.getPassword(account));
				callbackContext.success(result);
				return true;
			}
			else if("setUserData".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("key can not be null or empty");
					return true;
				}
				else if(args.isNull(2) || args.getString(2).length() == 0)
				{
					callbackContext.error("value can not be null or empty");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
	
				manager.setUserData(account, args.getString(1), args.getString(2));
				callbackContext.success();
				return true;
			}
			else if("getUserData".equals(action))
			{
				if(args.isNull(0))
				{
					callbackContext.error("account can not be null");
					return true;
				}
				else if(args.isNull(1) || args.getString(1).length() == 0)
				{
					callbackContext.error("key can not be null or empty");
					return true;
				}
				
				Account account = accounts.get(args.getInt(0));
				if(account == null)
				{
					callbackContext.error("Invalid account");
					return true;
				}
	
				JSONObject result = new JSONObject();
				result.put("value", manager.getUserData(account, args.getString(1)));
				callbackContext.success(result);
				return true;
			}
		}
		catch(SecurityException e)
		{
			callbackContext.error("Access denied");
			return true;
		}

		return false;
	}
}
