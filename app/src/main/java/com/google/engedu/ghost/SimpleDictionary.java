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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import static android.R.attr.id;
import static android.media.CamcorderProfile.get;

public class SimpleDictionary implements GhostDictionary {
    private ArrayList<String> words;

    public SimpleDictionary(InputStream wordListStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(wordListStream));
        words = new ArrayList<>();
        String line = null;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() >= MIN_WORD_LENGTH)
              words.add(line.trim());
        }
    }

    @Override
    public boolean isWord(String word) {
        return words.contains(word);
    }

    @Override
    public String getAnyWordStartingWith(String prefix)
    {
        // 1 If prefix is empty return a randomly selected word from the words ArrayList
        if(prefix.equals(""))
        {
            Random randomIndex= new Random();
            return words.get(randomIndex.nextInt(words.size()));
        }
        /** 2. Otherwise, perform a binary search over the words ArrayList
         *     until you find a word that starts with the given prefix and return it.
         */
        else
        {
            return binarySearch(prefix);
        }


        //return null;
    }

    @Override
    public String getGoodWordStartingWith(String prefix, int start) {
        String selected = null;
        int upIndex, downIndex, t;
        String possibleWord, checkWord;
        ArrayList<String> oddRandomWords = new ArrayList<String>();
        ArrayList<String> evenRandomWords = new ArrayList<String>();
        Random randomIndex;

        // 1 If prefix is empty return a randomly selected word from the words ArrayList
        if (prefix.equals("")) {
            randomIndex = new Random();
            return words.get(randomIndex.nextInt(words.size()));
        }
        /**
         *  1. using binary search to determine the whole range of words that start with the given prefix.
         **/
        else
        {
            possibleWord = binarySearch(prefix);  //get 1st possible word
            if (possibleWord == null) {
                return null;   // there is no possible word with given prefix
            }
            else     // there is atleast one possible word with given prefix
            {
                if(possibleWord.length() == 1)
                    oddRandomWords.add(possibleWord);
                else if (possibleWord.length() % 2 == 0)
                    evenRandomWords.add(possibleWord);
                else
                    oddRandomWords.add(possibleWord);

                upIndex = downIndex = words.indexOf(possibleWord);

                /** (HALF RANGE) searching ahead of possible word to get range of another word starting with prefix**/
                while (true) {
                    upIndex++;
                    if (upIndex == words.size()) {   //traversing ahead
                        break;
                    }
                    checkWord = words.get(upIndex);
                    t = checkWord.startsWith(prefix) ? 0 : prefix.compareTo(checkWord);
                    if (t != 0) {
                        break; // we get last word starting with prefix
                    }
                    if(checkWord.length() == 1)
                        oddRandomWords.add(checkWord);
                    if (checkWord.length() % 2 == 0)
                        evenRandomWords.add(checkWord);
                    else
                        oddRandomWords.add(checkWord);
                }
                /** (ANOTHER HALF RANGE) searching before of possible word to get range of another word starting with prefix**/
                while (true) {
                    downIndex--;
                    if (downIndex < 0) {   //traversing before possible word
                        break;
                    }
                    checkWord = words.get(downIndex);
                    t = checkWord.startsWith(prefix) ? 0 : prefix.compareTo(checkWord);
                    if (t != 0) {
                        break; // we get last word starting with prefix
                    }
                    if(checkWord.length() == 1)
                        oddRandomWords.add(checkWord);
                    if (checkWord.length() % 2 == 0)
                        evenRandomWords.add(checkWord);
                    else
                        oddRandomWords.add(checkWord);
                }

                /**
                 *  2. dividing the words between odd lengths and even lengths i.e done in above part 1.
                 **/

                /**
                 *  3. randomly selecting a word from the appropriate set
                 *  (whether it's even or odd depends on who went first)
                 **/

                if (start == 1)   //at odd position
                {
                    randomIndex = new Random();

                    if (evenRandomWords.size() != 0) {
                        selected = evenRandomWords.get(randomIndex.nextInt(evenRandomWords.size()));
                    } else {
                        if (oddRandomWords.size() != 0)
                            selected = oddRandomWords.get(randomIndex.nextInt(oddRandomWords.size()));
                    }

                }
                else
                {
                    if (start == 2)   //at even position
                    {
                        randomIndex = new Random();

                        if (oddRandomWords.size() != 0) {
                            selected = oddRandomWords.get(randomIndex.nextInt(oddRandomWords.size()));
                        } else {
                            if (evenRandomWords.size() != 0)
                                selected = evenRandomWords.get(randomIndex.nextInt(evenRandomWords.size()));
                        }

                    }
                }

                /** over.............**/
                return selected;
            }
            //dividing the words between odd lengths and even lengths
            // randomly selecting a word from the appropriate set (whether it's even or odd depends on who went first)

           // return selected;
        }
    }

    private String binarySearch(String prefix) {

        String dictionaryWord;
        int low = 0;
        int high = words.size() - 1;
        while (high >= low) {
            int middle = (high + low) / 2;
            dictionaryWord = words.get(middle);
            if (dictionaryWord.startsWith(prefix)) {
                // if words exist with given prefix return it to getAnyWordStartWith
                return dictionaryWord;
            }
            if (dictionaryWord.compareTo(prefix) < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        // If no such word exists, return null
        return null;
    }
}
