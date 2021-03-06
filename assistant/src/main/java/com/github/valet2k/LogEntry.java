package com.github.valet2k;

import com.github.valet2k.nails.HistoryMl;
import jsat.linear.ConcatenatedVec;
import jsat.linear.Vec;
import org.apache.commons.io.output.StringBuilderWriter;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

import java.io.File;

import static com.github.valet2k.columns.LastCommand.LASTCOMMAND;
import static com.github.valet2k.columns.WorkingDirectory.WORKINGDIRECTORY;

/**
 * Created by automaticgiant on 5/1/16.
 */
@Table(Core.TABLE_NAME)
public class LogEntry extends Model {

    public static HistoryMl historyMl;


    public String getCmd() {
        return this.getString(LASTCOMMAND);
    }

    public String getDir() {
        return this.getString(WORKINGDIRECTORY);
    }

    public double getLabel() {
        double score = 0;
        score += labelFromExtension(getCmd());
        if (historyMl.hm.containsKey(getCmd().split(" ")[0])) score += 30;
        return score;
    }

    public Double labelFromExtension(String command) {
        String currentDirectory = this.getDir();
        File f = new File(currentDirectory);
        File[] listOfFiles = f.listFiles();
        if (listOfFiles==null) return 0d;
        String[] splited = command.split(" ");
        Boolean gitFlag = false;
        Boolean makeFlag = false;
        Boolean mvnFlag = false;

        Double counter = 0.0;
        for (File file : listOfFiles) {
            if (file.isFile()) {

                if(file.getName().contains("git")) {
                    gitFlag = true;
                }
                if(file.getName().contains("make")) {
                    makeFlag = true;
                }
                else if(file.getName().contains("pom.xml")) {
                    mvnFlag = true;
                }
            }
        }

        if(command.equals("git") && gitFlag) {
            counter += 1;
        }

        if(command.equals("make") && makeFlag) {
            counter += 1;
        }

        if(command.equals("mvn") && mvnFlag) {
            counter += 1;
        }
        return counter;
    }


    public Vec getFeatures() {
        String cmd = getCmd();
        String dir = getDir();
        Vec commands = historyMl.getTvc().newText(cmd);
        Vec directories = historyMl.getTvc().newText(dir);
        ConcatenatedVec features = new ConcatenatedVec(commands, directories);
        HistoryMl.logger.trace("getting " + features.length() + " from " + this);
        return features;
    }
    public Double computedScore;

    public int getSelected() {
        Integer selected = this.getInteger("SELECTED");
        return selected==null?0:selected;
    }

    public int getAndIncrementSelected(){
        int old = getSelected();
        this.setInteger("SELECTED", getSelected() + 1);
        this.saveIt();
        return old;
    }
}
