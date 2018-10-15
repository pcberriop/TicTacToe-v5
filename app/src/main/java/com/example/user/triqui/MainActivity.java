package com.example.user.triqui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

    //various text displayed
    private TextView mInfoTextView;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIAlOG_ABOUT_ID = 2;

    private BoardView mBoardView;

    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputereMediaPlayer;

    private int mDifficulty;

    private SharedPreferences mPrefs;

    @Override
    protected void onResume(){
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.poker);
        mComputereMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alert);
    }

    @Override
    protected void onPause(){
        super.onPause();

        mHumanMediaPlayer.release();
        mComputereMediaPlayer.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.information);

        mNumberHuman = (TextView) findViewById(R.id.human);
        mNumberTie = (TextView) findViewById(R.id.tie);
        mNumberAndroid = (TextView) findViewById(R.id.android);

        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setOnTouchListener(mTouchListener);

        mPrefs = getSharedPreferences("ttt_prefs",MODE_PRIVATE);

        mHumanWins = mPrefs.getInt("mHumanWins",0);
        mAndroidWins = mPrefs.getInt("mComputerWins",0);
        mTie = mPrefs.getInt("mTies",0);
        mDifficulty = mPrefs.getInt("mDifficulty",2); //expert par défaut

        if(mDifficulty==0)
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if(mDifficulty==1)
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else if(mDifficulty==2)
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
        mBoardView.setGame(mGame);


        mhumanFirst = false;
        startNewGame();

        displayScores();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins",mHumanWins);
        ed.putInt("mComputerWins", mAndroidWins);
        ed.putInt("mTies",mTie);
        ed.putInt("mDifficulty",mDifficulty);
        ed.commit();
    }

    private void displayScores(){
        mNumberAndroid.setText("Android : "+Integer.toString(mAndroidWins));
        mNumberHuman.setText("Human : "+Integer.toString(mHumanWins));
        mNumberTie.setText("Ties : "+Integer.toString(mTie));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mHumanWins=savedInstanceState.getInt("mHumanWins");
        mAndroidWins=savedInstanceState.getInt("mComputerWins");
        mTie=savedInstanceState.getInt("mTies");
        mhumanFirst = savedInstanceState.getBoolean("mGoFirst");

        displayScores();

    }

    private void startNewGame(){
        mGame.clearBoard();
        mBoardView.invalidate();

        mhumanFirst=!mhumanFirst;

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


    private boolean setMove(char player,int location){
        if(mGame.setMove(player, location)){
            if(player == TicTacToeGame.HUMAN_PLAYER)
                mHumanMediaPlayer.start();
            else if(player == TicTacToeGame.COMPUTER_PLAYER)
                mComputereMediaPlayer.start();
            mBoardView.invalidate();
            return true;
        }
        return false;
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
            case R.id.about:
                showDialog(DIAlOG_ABOUT_ID);
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            case R.id.reset:
                SharedPreferences.Editor ed = mPrefs.edit();
                ed.putInt("mHumanWins",0);
                ed.putInt("mComputerWins", 0);
                ed.putInt("mTies",0);
                ed.commit();
                mHumanWins = mPrefs.getInt("mHumanWins",0);
                mAndroidWins = mPrefs.getInt("mComputerWins",0);
                mTie = mPrefs.getInt("mTies",0);
                displayScores();
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
                            mDifficulty=0;
                        } else if ( item == 1){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                            mDifficulty=1;
                        } else if ( item == 2){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                            mDifficulty=2;
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

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if(!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
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
                }            }

            return false;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putCharArray("board",mGame.getBoardState());
        outState.putBoolean("mGameOver",mGameOver);
        outState.putInt("mHumanWins", Integer.valueOf(mHumanWins));
        outState.putInt("mComputerWins", Integer.valueOf(mAndroidWins));
        outState.putInt("mTies", Integer.valueOf(mTie));
        outState.putCharSequence("info",mInfoTextView.getText());
        outState.putBoolean("mGoFirst", mhumanFirst);
    }

}