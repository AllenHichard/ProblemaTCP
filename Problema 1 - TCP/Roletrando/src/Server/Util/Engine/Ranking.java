package Server.Util.Engine;

import Server.Util.Exceptions.RankingLoadException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Game ranking' implementation. Implements Singleton design pattern to threat
 * concurrency factors.
 *
 * @author Allen Hichard
 * @author Daniel Andrade
 */
public class Ranking {

    private Properties generalRanking; //General ranking' file.
    private Properties top3; //Top 3 users' file, always sorted.
    private final RankingItem[] topUsers; //Top 3 users.
    private static Ranking instance; //Singleton instance.

    /**
     * Private constructor, part of the implementation of Singleton Design
     * Pattern. Also, loads the top3 and ranking files.
     */
    private Ranking() throws RankingLoadException {
        this.topUsers = new RankingItem[3];
    }
    
    /**
     * Loads the rankings.
     * @param rankingFile location of the general ranking file.
     * @param top3File location of the top3 ranking file
     * @return true if the rakings were loaded, false otherwise.
     * @throws RankingLoadException
     */
    public boolean loadRankings(String rankingFile, String top3File) throws RankingLoadException {
        if(this.generalRanking != null || this.top3 != null){
            return false;
        }
        this.generalRanking = new Properties();
        this.top3 = new Properties();
        try {
            this.generalRanking.load(new FileInputStream(rankingFile));
        } catch (IOException ex) {
            throw new RankingLoadException(rankingFile);
        }
        try {
            this.top3.load(new FileInputStream(top3File));
        } catch (IOException ex) {
            throw new RankingLoadException(top3File);
        }
        this.loadTop3();
        return true;
    }

    /**
     * Returns the current instance, if it doesn't exists, creates and returns a
     * new one.
     *
     * @return a Ranking instance
     * @throws Server.Util.Exceptions.RankingLoadException
     */
    public static Ranking instance() throws RankingLoadException  {
        if (Ranking.instance == null) {
            Ranking.instance = new Ranking();
        }
        return Ranking.instance;
    }

    /**
     * Loads the top 3 from the file.
     */
    private void loadTop3() {
        Enumeration<?> propertyNames = this.top3.propertyNames();
        
        /*Loads all entries of the top3.data file. If the entries < 3, the ranking
         *is completed with "empty - 0" entries.*/
        for (int i = 0; propertyNames.hasMoreElements() || i < 3; i++) {
            String username = propertyNames.nextElement().toString();
            String score = this.generalRanking.getProperty(username);
            if (score == null) {
                score = "0";
                username = "empty";
            }
            this.topUsers[i] = new RankingItem(username, Integer.parseInt(score));
        }
        Arrays.sort(this.topUsers);
    }

    /**
     * Return the current top 3 users.
     *
     * @return
     */
    public RankingItem[] getTop3() {
        return this.topUsers;
    }

    /**
     * Get a user's highscore.
     * 
     * @param username of the user.
     * @return
     */
    public int getUserHighscore(String username) {
        String highscore = this.generalRanking.getProperty(username);

        if (highscore == null) {
            this.generalRanking.setProperty(username, "0");
            return 0;
        }
        return Integer.parseInt(highscore);
    }

    /**
     * Refreshes a user's highscore if the given score is higher than the actual
     * score.
     * 
     * @param username of the user
     * @param score
     * @return true if the score was refreshed, false otherwise.
     * @throws IOException
     */
    public boolean refreshUserHighscore(String username, int score) throws IOException {

        if (this.getUserHighscore(username) < score) { //Refreshs only if the given score is higher than the highscore
            this.generalRanking.setProperty(username, "" + score); //Set the score on the general ranking
            if (this.topUsers[2].getScore() < score) { //The score is higher than the lowest top 3 score?
                this.topUsers[2] = new RankingItem(username, score); //Switch the given score with the lowest top3 score
                this.top3.clear(); //Clear the top 3 file
                for (RankingItem userTuple : this.topUsers) { //Create a new top3 file
                    this.top3.setProperty(userTuple.getUsername(), "" + userTuple.getScore());
                }
                this.top3.store(new FileOutputStream("top3.data"), ""); //Saving top3 file
                Arrays.sort(topUsers);
            }
            this.generalRanking.store(new FileOutputStream("ranking.data"), ""); //Saving general ranking
            
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        Ranking ranking = new Ranking();

        RankingItem[] top3 = ranking.getTop3();

        for (RankingItem x : top3) {
            System.out.println(x.getUsername() + " : " + x.getScore());
        }

        ranking.refreshUserHighscore("second", 11000000);

        for (RankingItem x : top3) {
            System.out.println(x.getUsername() + " : " + x.getScore());
        }
    }
}
