package org.codedefenders.model;

/**
 * Created by thomas on 06/03/2017.
 */
public enum EventType {
    GAME_CREATED, GAME_STARTED, GAME_FINISHED, GAME_GRACE_ONE, GAME_GRACE_TWO,
    GAME_PLAYER_LEFT,

    ATTACKER_MUTANT_CREATED, ATTACKER_MUTANT_SURVIVED, ATTACKER_MUTANT_ERROR,
    ATTACKER_MUTANT_KILLED_EQUIVALENT, ATTACKER_MESSAGE,

    DEFENDER_MUTANT_EQUIVALENT, DEFENDER_TEST_CREATED, DEFENDER_TEST_READY,
    DEFENDER_TEST_ERROR, DEFENDER_MUTANT_CLAIMED_EQUIVALENT,
    DEFENDER_KILLED_MUTANT, DEFENDER_MESSAGE,

    ATTACKER_JOINED, DEFENDER_JOINED, GAME_MESSAGE, GAME_MESSAGE_ATTACKER,
    GAME_MESSAGE_DEFENDER
}