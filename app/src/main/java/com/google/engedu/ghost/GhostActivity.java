/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.ghost;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Random;

import static android.R.attr.label;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn;
    private Random random = new Random();
    private Handler handler;
     private boolean afterCheck;  //checking after game over condition

    //declaring currentWord;
    private String currentWord;
    private int startCheck;

    //declaring textview
    TextView ghostT, status;
   //Button challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);

        //get views using findView
        ghostT = (TextView) findViewById(R.id.ghostText);
        status = (TextView) findViewById(R.id.gameStatus);
        ghostT.setText("");
        //challenge = (Button) findViewById(R.id.challengeButton);

        AssetManager assetManager = getAssets();

        // creating simple dictionary which contain list of words present in word.txt inside it namely here dictionary
        try {
            InputStream inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        /** right now null is send as param instead of view bcoz it is to just check who will get chance for first time
         *   it is called from java so not passed view and instead passed null
         *   this will invoke onstart not for reset but for priority of turn
         **/
        onStart(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
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

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     *
     * @param view is used when button reset is clicked
     * @return true
     */
    public boolean onStart(View view) {
        afterCheck=false;
        // challenge.setEnabled(true);
        userTurn = random.nextBoolean();
        TextView text = (TextView) findViewById(R.id.ghostText);
        text.setText("");
        TextView label = (TextView) findViewById(R.id.gameStatus);


        if (userTurn) {
            label.setText(USER_TURN);
            startCheck=2;
        } else {
            label.setText(COMPUTER_TURN);
            startCheck=1;
          //  challenge.setEnabled(false);
            //computerTurn();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 2seconds
                    //invoke computerturn
                    computerTurn();
                }
            }, 2000);
        }
        return true;
    }

    private void computerTurn() {
        //TextView label = (TextView) findViewById(R.id.gameStatus);

        // Do computer turn stuff then make it the user's turn again

        //set label of computer turn
        //status.setText(COMPUTER_TURN);
        //userTurn=false;
        //fetch the existing word
        currentWord = ghostT.getText().toString();

        // 1.Check if the fragment is a word with at least 4 characters. If so declare victory by updating the game status
        if (dictionary.isWord(currentWord) && currentWord.length() >= 4) {
            //Log.d("TAG", "computerTurn: true");
            status.setText("Computer won");
            Toast.makeText(this, "Word is VALID with length > 3 YOU LOST", Toast.LENGTH_LONG).show();
            afterCheck=true;
            userTurn=false;
        }
        // 2.Use the dictionary's getAnyWordStartingWith method to get a possible longer word with currentWord as a prefix
        else {
            String randomWord = dictionary.getGoodWordStartingWith(currentWord,startCheck);

            /** 3.If such a word doesn't exist (method returns null),
             *    challenge the user's fragment and declare victory (you can't bluff this computer!)
             **/
            if (randomWord == null) {
                status.setText("Computer WON");
                Toast.makeText(this, "you can't bluff this computer  current word is INVALID YOU LOST", Toast.LENGTH_LONG).show();
                afterCheck=true;
                userTurn=false;
            }

            /** 4 .If such a word does exist,
             *     add the next letter of it to the fragment (remember the substring method in the Java string library)
             **/
            else {
                char nextLetter = randomWord.charAt(currentWord.length());
                currentWord += nextLetter;
                ghostT.setText(currentWord);
                status.setText(USER_TURN);
                userTurn = true;
            }
        }
        //challenge.setEnabled(true);

        //status.setText(USER_TURN);
        /** Reseting Game after over**/
        if(afterCheck==true) {
            /**handler = new Handler();
             *  handler.postDelayed(new Runnable() {
             *    @Override
             *  public void run() {
             *       //Do something after 2seconds
             *       //invoke computerturn
             *       onStart(null);
             *   }
             * }, 2000);
             * */
            Toast.makeText(this, "Click Restart to start New Game", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handler for user key presses.
     *
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        //if(afterCheck==false) {
        //get char pressed by user from keyboard
        char keyPressed = (char) event.getUnicodeChar();
        if(userTurn==true) {
            if (afterCheck == false) {
                //check whether character pressed is valid i.e between a to z
                if (Character.isLetter(keyPressed)) {
                    currentWord = ghostT.getText().toString();
                    currentWord += keyPressed;
                    ghostT.setText(currentWord);

                    status.setText(COMPUTER_TURN);
                    //set Computer turn
                    userTurn = false;
                    // challenge.setEnabled(false);
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 2seconds
                            //invoke computerturn
                            computerTurn();
                        }
                    }, 2000);
                    //invoke computerturn
                    //computerTurn();

                    //check validity for user input
                    /**if (dictionary.isWord(currentWord)) {
                     status.setText("VALID WORD");
                     } else
                     status.setText("INVALID WORD");
                     return true;
                     **/
                    return true;

                } else {
                    Toast.makeText(this, "Invalid INPUT", Toast.LENGTH_SHORT).show();
                    return super.onKeyUp(keyCode, event);
                }
            }
        }

        return false;
    }

    public void challenge(View view) {

        /**check if User Turn is there ,
         * then only user can click challenge
         */
        if(userTurn==true) {
            if (afterCheck == false) {
                //Get the current word fragment
                currentWord = ghostT.getText().toString();
                //check if user has click on challenge without having word
                if (currentWord.equals("")) {
                    status.setText("You Can't challenge empty word,  Your Turn again..");
                    Toast.makeText(this, "Empty Word challenge not possible !!", Toast.LENGTH_LONG).show();
                }
                //If it has at least 4 characters and is a valid word, declare victory for the user
                else if (currentWord.length() > 3 && dictionary.isWord(currentWord)) {
                    status.setText("You Won");
                    Toast.makeText(this, "You Beat CPU", Toast.LENGTH_LONG).show();
                    afterCheck = true;


                } else {
                    /** otherwise if a word can be formed with the fragment as prefix,
                     * declare victory for the computer and display a possible word
                     */
                    String anotherWord = dictionary.getAnyWordStartingWith(currentWord);
                    if (anotherWord != null) {
                        status.setText("Computer Won");
                        ghostT.setText("possible Word:- " + anotherWord + " !!");
                        Toast.makeText(this, "Still Word can be possible such as " + anotherWord, Toast.LENGTH_LONG).show();
                        afterCheck = true;
                    }
                    //If a word cannot be formed with the fragment, declare victory for the user
                    else {
                        status.setText("You Won");
                        Toast.makeText(this, "You Beat CPU", Toast.LENGTH_LONG).show();
                        afterCheck = true;
                    }
                }
            }
        }
        else
        {
            Toast.makeText(this, "Cannot Challenge Right Now", Toast.LENGTH_LONG).show();
        }
        /** Reseting Game after over**/
       if(afterCheck==true) {
           /**handler = new Handler();
            *  handler.postDelayed(new Runnable() {
            *    @Override
            *  public void run() {
            *       //Do something after 2seconds
            *       //invoke computerturn
            *       onStart(null);
            *   }
            * }, 2000);
            * */
           Toast.makeText(this, "Click Restart to start New Game", Toast.LENGTH_LONG).show();
       }

    }
}
