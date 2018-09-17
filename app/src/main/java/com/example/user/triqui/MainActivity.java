package com.example.user.triqui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.audiofx.DynamicsProcessing;
import android.media.tv.TvView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Represents the internal state of the game
    private TicTacToeGame mGame;

    //human first or not
    private boolean mhumanFirst;

    //the numbers of wins
    private int mAndroidWins;
    private int mHumanWins;
    private int mTie;

    //text views
    private TextView mNumberHuman;
    private TextView mNumberTie;
    private TextView mNumberAndroid;

    //game over
    private boolean mGameOver;

    //buttons making the board
    private Button mBoardButtons[];

    //various text displayed
    private TextView mInfoTextView;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIAlOG_ABOUT_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.one);
        mBoardButtons[1] = (Button) findViewById(R.id.two);
        mBoardButtons[2] = (Button) findViewById(R.id.three);
        mBoardButtons[3] = (Button) findViewById(R.id.four);
        mBoardButtons[4] = (Button) findViewById(R.id.five);
        mBoardButtons[5] = (Button) findViewById(R.id.six);
        mBoardButtons[6] = (Button) findViewById(R.id.seven);
        mBoardButtons[7] = (Button) findViewById(R.id.eight);
        mBoardButtons[8] = (Button) findViewById(R.id.nine);

        mInfoTextView = (TextView) findViewById(R.id.information);

        mNumberHuman = (TextView) findViewById(R.id.human);
        mNumberTie = (TextView) findViewById(R.id.tie);
        mNumberAndroid = (TextView) findViewById(R.id.android);

        mGame = new TicTacToeGame();
        mAndroidWins = 0;
        mHumanWins=0;
        mTie=0;
        mhumanFirst = false;
        startNewGame();
    }

    private void startNewGame(){
        mGame.clearBoard();

        mhumanFirst=!mhumanFirst;

        for(int i=0;i<mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }
        mGameOver=false;

        //human goes first
        if(mhumanFirst){
            mInfoTextView.setText(R.string.first_human);
        } else {
            mInfoTextView.setText(R.string.first_android);
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER,move);
            mInfoTextView.setText(R.string.turn_human);
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location){
            this.location=location;
        }

        public void onClick(View view){
            if(mBoardButtons[location].isEnabled() && !mGameOver){
                setMove(TicTacToeGame.HUMAN_PLAYER, location);

                //if no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if(winner==0){
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if(winner==0){
                    mInfoTextView.setText(R.string.turn_human);
                } else if ( winner == 1){
                    mInfoTextView.setText(R.string.result_tie);
                    mTie++;
                    mNumberTie.setText("Ties : " + mTie);
                    mGameOver=true;
                } else if (winner == 2){
                    mInfoTextView.setText(R.string.result_human_wins);
                    mHumanWins++;
                    mNumberHuman.setText("Human : " + mHumanWins);
                    mGameOver=true;
                } else {
                    mInfoTextView.setText(R.string.result_computer_wins);
                    mAndroidWins++;
                    mNumberAndroid.setText("Android : " + mAndroidWins);
                    mGameOver=true;
                }
            }
        }
    }

    private void setMove(char player,int location){

        mGame.setMove(player,location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if(player == TicTacToeGame.HUMAN_PLAYER )
            mBoardButtons[location].setTextColor(Color.rgb(0,200,0));
        else
            mBoardButtons[location].setTextColor(Color.rgb(200,0,0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            case R.id.about:
                showDialog(DIAlOG_ABOUT_ID);
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id){
            case DIAlOG_ABOUT_ID:

                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;

            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert),
                };

                int selected =0;
                TicTacToeGame.DifficultyLevel dif;
                dif = mGame.getDifficultyLevel();

                if(dif == TicTacToeGame.DifficultyLevel.Easy){
                    selected = 0;
                } else if ( dif == TicTacToeGame.DifficultyLevel.Harder){
                    selected = 1;
                } else if ( dif == TicTacToeGame.DifficultyLevel.Expert){
                    selected = 2;
                }

                builder.setSingleChoiceItems(levels, selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();

                        if(item == 0){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                        } else if ( item == 1){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                        } else if ( item == 2){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                        }

                        Toast.makeText(getApplicationContext(), levels[item],
                                Toast.LENGTH_SHORT).show();

                    }
                });
                dialog = builder.create();
                break;
            case DIALOG_QUIT_ID:

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

}