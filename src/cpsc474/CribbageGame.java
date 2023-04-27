package cpsc474;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CribbageGame
{
    private int pairValue = 2;
    private int keepCards = 4;
    private int throwCards = 2;
    private int scoringSum = 15;
    private int sumValue = 2;
    private int dealCards = keepCards + throwCards;
    private int peggingLimit = 31;
    int winningScore = 121;
    private int heelsValue = 2;

    private OrderedRanks cardRanks;
    private CharacterSuits cardSuits;
    private List<Integer> allValues;
    
    public CribbageGame()
    {
	cardRanks = new OrderedRanks(new String[] {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T,10", "J", "Q", "K"});
	cardSuits = new CharacterSuits("SHDC");
	allValues = new ArrayList<>();
	for (int i = 1; i <= 10; i++)
	    {
		allValues.add(i);
	    }
    }
	
    
    public CribbageHand[] deal()
    {
	Deck<CardRank, Character, OrderedRanks, CharacterSuits> deck = new Deck<>(cardRanks, cardSuits, 1);
	deck.shuffle();
	List<CribbageCard> bothHandsAndTurn = deck.deal(dealCards * 2 + 1).stream().map(c -> new CribbageCard(c)).collect(Collectors.toList());
	CribbageHand p0Hand = new CribbageHand(bothHandsAndTurn.subList(0, dealCards));
	CribbageHand p1Hand = new CribbageHand(bothHandsAndTurn.subList(dealCards, dealCards * 2));
	CribbageHand turnCard = new CribbageHand(bothHandsAndTurn.subList(2 * dealCards, 2 * dealCards + 1));

	return new CribbageHand[] {p0Hand, p1Hand, turnCard};
    }

    public int cardsToKeep()
    {
	return keepCards;
    }

    public int cardsDealt()
    {
	return dealCards;
    }
    
    public int getPeggingLimit()
    {
	return peggingLimit;
    }
    
    public int sumScore(int sum)
    {
	return sum == scoringSum ? sumValue : 0;
    }

    public int pegPairValue(int count)
    {
	return pairValue * (int)Combinatorics.combinations(count, 2);
    }

    public int pegStraightValue(int length)
    {
	return length >= 3 ? length : 0;
    }

    public int pegSumValue(int sum)
    {
	return sum == 15 ? 2 : 0;
    }

    public int pegExactValue(boolean alreadyGo)
    {
	return alreadyGo ? 1 : 2;
    }
    
    /**
     * Returns the pegging point value of the given rank in this game.
     *
     * @param r a cpsc474.CardRank in this game
     * @return the pegging point value of rank
     */
    public int rankValue(CardRank r)
    {
	return r.intValue() < 10 ? r.intValue() + 1 : 10;
    }

    public int handFlushValue(int length)
    {
	if (length == keepCards)
	    {
		return keepCards;
	    }
	else
	    {
		return 0;
	    }
    }

    public int turnFlushValue(int length)
    {
	if (length == keepCards + 1)
	    {
		return keepCards + 1;
	    }
	else
	    {
		return 0;
	    }
    }	
	       
    public int runValue(int length, int combinations)
    {
	if (length >= 3)
	    {
		return length * combinations;
	    }
	else
	    {
		return 0;
	    }
    }

    public int turnCardValue(CribbageCard turn)
    {
	if (turn.getRank().getName().equals("J"))
	    {
		return heelsValue;
	    }
	else
	    {
		return 0;
	    }
    }
    
    public int nobValue(CardRank r)
    {
	if (r.getName().equals("J"))
	    {
		return 1;
	    }
	else
	    {
		return 0;
	    }
    }
    
    /**
     * Returns the score and subscores that would be earned by the given
     * hand with the given turn (cut) card.  The scores are returned
     * in a vector in the order total, pairs, 15s, runs, flushes, nobs.
     *
     * @param hand a cpsc474.CribbageHand, non-null
     * @param turn a cpsc474.CribbageCard, or null
     * @param cribRules true to score using crib scoring rules, false otherwise
     * @return an array of integers where the first element is the total
     * of the remaining elements
     */
    public int[] score(CribbageHand hand, CribbageCard turn, boolean cribRules)
    {
	// make one hand with turn card if given
	CribbageHand allCards = turn == null ? hand : new CribbageHand(hand, new CribbageHand(Arrays.asList(new CribbageCard[] {turn})));

	Map<Integer, Integer> valueCount = new HashMap<>();
	for (Integer i : allValues)
	    {
		valueCount.put(i, 0);
	    }
	Map<CardRank, Integer> rankCount = new HashMap<>();
	for (CardRank r : cardRanks.allRanks())
	    {
		rankCount.put(r, 0);
	    }
	Map<Character, Integer> suitCount = new HashMap<>();
	for (Character s : cardSuits.allSuits())
	    {
		suitCount.put(s, 0);
	    }
	
	for (CribbageCard c : allCards)
	    {
		CardRank rank = c.getRank();
		char suit = c.getSuit();
		int value = rankValue(rank);
		valueCount.put(value, valueCount.get(value) + 1);
		rankCount.put(rank, rankCount.get(rank) + 1);
		suitCount.put(suit, suitCount.get(suit) + 1);
	    }

	int fifteens = 0;
	for (int count = 2; count <= allCards.size(); count++)
	    {
		for (List<CribbageCard> subset : IterTools.combinations(allCards, count))
		    {
			int sum = 0;
			for (CribbageCard c : subset)
			    {
				sum += rankValue(c.getRank());
			    }
			fifteens += sumScore(sum);
		    }
	    }

	int pairs = 0;
	for (CardRank r : cardRanks.allRanks())
	    {
		pairs += Combinatorics.combinations(rankCount.get(r), 2);
	    }
	pairs *= pairValue;

	int runs = 0;
	int currRun = 0;
	int combinations = 1;
	for (CardRank r : cardRanks.allRanks())
	    {
		if (rankCount.get(r) == 0)
		    {
			runs += runValue(currRun, combinations);
			currRun = 0;
			combinations = 1;
		    }
		else
		    {
			currRun += 1;
			combinations *= rankCount.get(r);
		    }
	    }
	runs += runValue(currRun, combinations);

	int flushes = 0;
	int maxFlush = 0;
	char longSuit = '0';
	for (Character s : cardSuits.allSuits())
	    {
		if (suitCount.get(s) > maxFlush)
		    {
			longSuit = s;
			maxFlush = suitCount.get(s);
		    }
	    }
	if (maxFlush == hand.size() && !cribRules && (turn == null || turn.getSuit() != longSuit))
	    {
		flushes += handFlushValue(hand.size());
	    }
	else if (maxFlush == hand.size() + 1)
	    {
		flushes += turnFlushValue(maxFlush);
	    }

	int nobs = 0;
	for (CribbageCard c : hand)
	    {
		if (turn != null && c.getSuit() == turn.getSuit())
		    {
			nobs += nobValue(c.getRank());
		    }
	    }
	
	return new int[] {pairs + fifteens + runs + flushes + nobs, pairs, fifteens, runs, flushes, nobs};
    }
    
    public CribbageCard parseCard(String card)
    {
	CardRank rank = cardRanks.parseRank(card.substring(0, card.length() - 1));
	Character suit = cardSuits.parseSuit(card.substring(card.length() - 1));
	return new CribbageCard(rank, suit);
    }

    public int[] play(CribbagePolicy p0, CribbagePolicy p1, Logger logger)
    {
	int[] scores = new int[] {0, 0};
	CribbagePolicy[] policies = new CribbagePolicy[] {p0, p1};
	int dealer = 0;
	int handsPlayed = 0;

	while (MoreArrays.max(scores) < winningScore)
	    {
		logger.log("Dealing at " + Arrays.toString(scores));
		// deal cards
		CribbageHand[] cardsInPlay = deal();
		handsPlayed++;
		// turned card is first (only) element of third part of deal
		CribbageCard turn = cardsInPlay[2].iterator().next();
		logger.log("TURN: " + turn);
		
		// check for 2 for heels (turned card is a Jack)
		int heels = turnCardValue(turn);
		scores[dealer] += heels;
		if (heels != 0)
		    {
			logger.log(Arrays.toString(scores));
		    }

		// get keep/throw for each player
		CribbageHand[][] keeps = new CribbageHand[2][];
		for (int p = 0; p < 2; p++)
		    {
			keeps[p] = policies[p].keep(cardsInPlay[p], scores, p == dealer);
			if (!cardsInPlay[p].isLegalSplit(keeps[p]))
			    {
				throw new RuntimeException(Arrays.toString(keeps[p]) + " does not partition " + cardsInPlay[p]);
			    }
			else if (keeps[p][0].size() != keepCards)
			    {
				throw new RuntimeException("Invalid partition sizes " + Arrays.toString(keeps[p]));
			    }
		    }

		// initialize pegging
		int pegTurn = 1 - dealer;
		PeggingHistory history = new PeggingHistory(this);
		CribbageHand[] pegCards = new CribbageHand[] {keeps[0][0], keeps[1][0]};
		while (MoreArrays.max(scores) < winningScore
		       && !history.isTerminal())
		    {
			// get player's played card
			CribbageCard play = policies[pegTurn].peg(pegCards[pegTurn], history, pegTurn == 0 ? Arrays.copyOf(scores, scores.length) : MoreArrays.reverse(scores), pegTurn == dealer);

			// check for legality of chosen card
			if (play == null && history.hasLegalPlay(pegCards[pegTurn], pegTurn == dealer ? 0 : 1))
			    {
				throw new RuntimeException("passing when " + pegCards[pegTurn] + " contains a valid card");
			    }
			else if (play != null && !history.isLegal(play, pegTurn == dealer ? 0 : 1))
			    {
				throw new RuntimeException("chosen card " + play + " us not legal");
			    }
			
			history = history.play(play, pegTurn == dealer ? 0 : 1);

			logger.log(pegTurn + " " + play);
			
			// score the play
			int[] playScore = history.getScore();
			if (playScore[0] > 0)
			    {
				scores[pegTurn] += playScore[0];
				logger.log(Arrays.toString(scores));
			    }
			else if (playScore[0] < 0)
			    {
				scores[1 - pegTurn] += -playScore[0];
				logger.log(Arrays.toString(scores));
			    }

			// remove played card from hand
			if (play != null)
			    {
				CribbageHand newHand = pegCards[pegTurn].remove(play);
				if (newHand == null)
				    {
					throw new RuntimeException("played card " + play + " not in hand " + pegCards[pegTurn]);
				    }
				pegCards[pegTurn] = newHand;
			    }

			// next player's turn
			pegTurn = 1 - pegTurn;
			}

		// score non-dealer's hand
		if (MoreArrays.max(scores) < winningScore)
		    {
			int[] handScore = score(keeps[1 - dealer][0], turn, false);
			scores[1 - dealer] += handScore[0];
			logger.log("NON-DEALER: "
				   + keeps[1 - dealer][0]
				   + " " + Arrays.toString(scores));
		    }

		// score dealer's hand
		if (MoreArrays.max(scores) < winningScore)
		    {
			int[] handScore = score(keeps[dealer][0], turn, false);
			scores[dealer] += handScore[0];
			logger.log("DEALER: "
				   + keeps[dealer][0]
				   + " " + Arrays.toString(scores));
		    }

		// score crib
		if (MoreArrays.max(scores) < winningScore)
		    {
			CribbageHand crib = new CribbageHand(keeps[0][1], keeps[1][1]);
			int[] handScore = score(crib, turn, true);
			scores[dealer] += handScore[0];
			logger.log("CRIB: "
				   + crib
				   + " " + Arrays.toString(scores));
		    }

		// change dealer
		dealer = 1 - dealer;
	    }
	logger.log(Arrays.toString(scores));

	return new int[] {gameValue(scores), handsPlayed};
    }

    public EvaluationResults evaluatePolicies(CribbagePolicy p0, CribbagePolicy p1, int count)
    {
	Logger nullLogger = new NullLogger();
	EvaluationResults results = new EvaluationResults();
	int last_percent = 0;
	for (int g = 0; g < count; g++)
	    {
		int percentComplete = g * 100 / count;
		if (percentComplete % 10 == 0 && percentComplete != last_percent) {
			last_percent = percentComplete;
			System.out.println(percentComplete + "% complete");
		}
		int points;
		int[] result;
		if (g % 2 == 0)
		    {
			result = play(p0, p1, nullLogger);
			points = result[0];
		    }
		else
		    {
			result = play(p1, p0, nullLogger);
			points = -result[0];
		    }
		results.update(points, result[1]);
	    }

	return results;
    }

    public static double computeMean(Map<Integer, Integer> results)
    {
	int totalGames = 0;
	int netPoints = 0;
	for (Map.Entry<Integer, Integer> e: results.entrySet())
	    {
		int points = e.getKey();
		int games = e.getValue();
		totalGames += games;
		netPoints += points * games;
	    }
	return netPoints / (double)totalGames;
    }
    
    public int gameValue(int[] scores)
    {
	// System.out.println(Arrays.toString(scores));
	if (MoreArrays.max(scores) < winningScore)
	    {
		return 0;
	    }

	int points = 1;

	// we are making this a zero sum game

	int loserScore = MoreArrays.min(scores);
	if (loserScore <= 60)
	    {
		points = 3;
	    }
	else if (loserScore <= 90)
	    {
		points = 2;
	    }


	return (scores[0] > scores[1] ? 1 : -1) * points;
    }
}

