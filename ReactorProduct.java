public class ReactorProduct {
	private ReactorFeed reactorFeed;
	private double x1; // extent of reaction [in mol/hr] of main styrene production: EB -> S + H
	private double x2; // extent of reaction [in mol/hr] of side toluene reaction: EB + H -> T + M

	/**
	 * Main constructor used to build a ReactorProduct object
	 *
	 * @param reactorFeed - ReactorFeed object representing reactor feed component flow rates
	 * @param x1 - extent of reaction in mol/hr of the main styrene reaction
	 * @param x2 - extent of reaction in mol/hr of the side toluene reaction
	 */
	public ReactorProduct(ReactorFeed reactorFeed, double x1, double x2) {
		this.reactorFeed = reactorFeed;
		this.x1 = x1;
		this.x2 = x2;
	}

	/**
	 * Constructor used to make a new copy of a ReactorProduct object in memory
	 */
	public ReactorProduct(ReactorProduct reactorProduct) {
		this.reactorFeed = new ReactorFeed(reactorProduct.getReactorFeed());
		this.x1 = reactorProduct.getX1();
		this.x2 = reactorProduct.getX2();
	}

	public void setExtentsOfReaction(double x1, double x2) {
		this.x1 = x1;
		this.x2 = x2;
	}

	public ReactorFeed getReactorFeed() {
		return reactorFeed;
	}

	public double getX1() {
		return x1;
	}

	public double getX2() {
		return x2;
	}

	public double getTotalProd() {
		return getProdEB() + getProdS() + getProdH() + getProdT() + getProdB() + getProdM() + getProdSteam();
	}

	public double getProdEB() {
		return reactorFeed.getFeedEB() - x1 - x2;
	}

	public double getProdS() {
		return x1;
	}

	public double getProdH() {
		return x1 - x2;
	}

	public double getProdT() {
		return reactorFeed.getFeedT() + x2;
	}

	public double getProdB() {
		return reactorFeed.getFeedB();
	}

	public double getProdM() {
		return x2;
	}

	public double getProdSteam() {
		return reactorFeed.getFeedSteam();
	}

	public String toString() {
		return "Styrene Reactor Product:\n" + 
			"Ethylbenzene:\t\t" + String.format("%.2f", getProdEB()) + "\n" +
			"Styrene:\t\t" + String.format("%.2f", getProdS()) + "\n" +
			"Hydrogen:\t\t" + String.format("%.2f", getProdH()) + "\n" +
			"Toluene:\t\t" + String.format("%.2f", getProdT()) + "\n" +
			"Benzene:\t\t" + String.format("%.2f", getProdB()) + "\n" +
			"Methane:\t\t" + String.format("%.2f", getProdM()) + "\n" +
			"Steam:\t\t\t" + String.format("%.2f", getProdSteam()) + "\n" + 
			"Total:\t\t\t" + String.format("%.2f", getTotalProd());
	}
}