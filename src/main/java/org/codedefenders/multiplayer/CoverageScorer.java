package org.codedefenders.multiplayer;

import org.codedefenders.Test;
import org.codedefenders.scoring.Scorer;

import java.util.ArrayList;

/**
 * Created by thoma on 27/06/2016.
 */
public class CoverageScorer extends Scorer {
    @Override
    protected int scoreTest(MultiplayerGame g, Test t, ArrayList<MultiplayerMutant> killed) {
        return (g.getDefenderValue() + killed.size()) * t.getLineCoverage().getLinesCovered().length;
    }

    @Override
    protected int scoreMutant(MultiplayerGame g, MultiplayerMutant m, ArrayList<Test> passed) {
        return (g.getAttackerValue() + passed.size());
    }
}