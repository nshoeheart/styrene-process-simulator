public class ReactorFeed {
	public static final double EB_FEED_FRAC = 0.95; // mole fraction of ethylbenzene in process feed
	public static final double T_FEED_FRAC = 0.03; // mole fraction of toluene in process feed
	public static final double B_FEED_FRAC = 0.02; // mole fraction of benzene in process feed

	private double processFeedBasis; // total mol/h fed to process
	private double recycledEB; // total mol/h of recycled ethylbenzene
	private double steamRatio; // molar ratio of steam to ethylbenzene entering reactor


	/**
	 * Main constructor used to build a ReactorFeed object with a given process feed, recycle rate, and steam ratio
	 *
	 * @param processFeedBasis - total moles of feed entering the process (not including recycle)
	 * @param recycledEB - molar flow rate of unreacted ethylbenzene that was recycled
	 * @param steamRatio - molar ratio of steam to ethylbenzene entering the styrene reactor
	 */
	public ReactorFeed(double processFeedBasis, double recycledEB, double steamRatio) {
		this.processFeedBasis = processFeedBasis;
		this.recycledEB = recycledEB;
		this.steamRatio = steamRatio;
	}

	/**
	 * Constructor used to make a new copy of a ReactorFeed object in memory
	 */
	public ReactorFeed(ReactorFeed reactorFeed) {
		this.processFeedBasis = reactorFeed.getProcessFeedBasis();
		this.recycledEB = reactorFeed.getRecycledEB();
		this.steamRatio = reactorFeed.getSteamRatio();
	}

	public void setProcessFeedBasis(double basis) {
		this.processFeedBasis = basis;
	}

	/**
	 * Used to change the process feed basis by a given amount
	 *
	 * @param basisDiff - the amount by which to change the process feed basis
	 */
	public void changeProcessFeedBasis(double basisDiff) {
		this.processFeedBasis += basisDiff;
	}

	public void setRecycledEB(double recycledEB) {
		this.recycledEB = recycledEB;
	}

	public double getTotalFeed() {
		return getFeedEB() + getFeedT() + getFeedB() + getFeedSteam();
	}

	public double getProcessFeedBasis() {
		return processFeedBasis;
	}

	public double getSteamRatio() {
		return steamRatio;
	}

	public double getFeedEB() {
		return getProcessFeedEB() + getRecycledEB();
	}

	public double getProcessFeedEB() {
		return EB_FEED_FRAC*processFeedBasis;
	}

	public double getRecycledEB() {
		return recycledEB;
	}

	public double getFeedT() {
		return T_FEED_FRAC*processFeedBasis;
	}

	public double getFeedB() {
		return B_FEED_FRAC*processFeedBasis;
	}

	public double getFeedSteam() {
		return getFeedEB() * steamRatio;
	}

	public String toString() {
		return "Styrene Reactor Feed:\n" + 
			"Total Ethylbenzene:\t" + String.format("%.2f", getFeedEB()) + "\n" +
			"Process Feed EB:\t" + String.format("%.2f", getProcessFeedEB()) + "\n" +
			"Recycled EB:\t\t" + String.format("%.2f", getRecycledEB()) + "\n" +
			"Toluene:\t\t" + String.format("%.2f", getFeedT()) + "\n" +
			"Benzene:\t\t" + String.format("%.2f", getFeedB()) + "\n" +
			"Steam:\t\t\t" + String.format("%.2f", getFeedSteam()) + "\n" + 
			"Total:\t\t\t" + String.format("%.2f", getTotalFeed());
	}
}