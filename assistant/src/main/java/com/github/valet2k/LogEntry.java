package com.github.valet2k;

import com.github.valet2k.nails.HistoryMl;
import jsat.linear.Vec;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

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
        if (getCmd().contains("commit")) score++;
        if (getCmd().startsWith("git")) score++;
        if (getCmd().startsWith("ls")) score--;
        if (getCmd().startsWith("^")) score -= 10;
        if (getCmd().startsWith("rm")) score -= 5;
        if (getCmd().contains("~")) score += 2;
        if (getCmd().startsWith("cd")) score++;
        return score;
    }

    public Vec getFeatures() {
        String cmd = getCmd();
        Vec features = historyMl.getTvc().newText(cmd);
        HistoryMl.logger.trace("getting " + features.length() + " from " + this);
        return features;
    }
    public Double computedScore;
}
