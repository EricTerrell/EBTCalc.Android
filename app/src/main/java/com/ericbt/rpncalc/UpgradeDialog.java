/*
  EBTCalc
  (C) Copyright 2015, Eric Bergman-Terrell
  
  This file is part of EBTCalc.

    EBTCalc is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBTCalc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBTCalc.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.rpncalc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class UpgradeDialog {
	private AlertDialog alertDialog;
	private final Context context;
	
	public UpgradeDialog(Context context) {
		this.context = context;
	}
	
	public void display() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(String.format(context.getString(R.string.upgrade_title), context.getString(R.string.app_name)));
		alertDialogBuilder.setMessage("You are using the free version of EBTCalc.\r\n\r\nUpgrade to the paid version to:\r\n\r\n1) Support continued EBTCalc development.\r\n\r\n2) Stop displaying this dialog every time you edit Javascript.");
		
		alertDialogBuilder.setNegativeButton("Cancel", null);
		alertDialogBuilder.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();

				String appStore = context.getResources().getString(R.string.app_store);
				
				if (!appStore.equals("BN")) {
					String paidVersionURL = context.getResources().getString(R.string.paid_version_url);
					Log.i(StringLiterals.LogTag, String.format("UpgradeDialog.show: paidVersionURL: %s", paidVersionURL));
					
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paidVersionURL));
					context.startActivity(intent);
				}
				else {
					String ean = context.getResources().getString(R.string.bn_EAN);

				    Intent intent = new Intent();
				    intent.setAction("com.bn.sdk.shop.details"); 
				    intent.putExtra("product_details_ean", ean);
				    context.startActivity(intent);
				}
			}
		});

		alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
