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

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class SoundEffect {
	private static void play(int soundEffect) {
		int keyclickVolume = Preferences.getKeyclickVolume();

		if (keyclickVolume > 0) {
			MediaPlayer mediaPlayer = MediaPlayer.create(
					Globals.getApplication().getApplicationContext(), soundEffect);

			float volume = keyclickVolume / 10.0f;
			mediaPlayer.setVolume(volume, volume);

			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					mediaPlayer.release();
				}
			});

			mediaPlayer.start();
		}
	}

	public static void playKeyClick() {
		play(R.raw.keyclick);
	}
}
