public class StyreneProcess {
	// Costs and values given in $/lbm
	static final double EB_COST = 0.50;
	static final double STEAM_COST = 0.044;
	static final double S_VALUE = 0.62;
	static final double T_VALUE = 0.44;
	static final double H_VALUE = 0.21;
	static final double M_VALUE = 0.086;
	static final double WW_COST = 0.004;

	// Molecular weights in kg/kmol
	static final double EB_MW = 106.17;
	static final double W_MW = 18.01528;
	static final double S_MW = 104.15;
	static final double T_MW = 92.14;
	static final double H_MW = 1.008*2d;
	static final double M_MW = 16.04;

	static final double KG_TO_LBM_CONV = 1 / 0.453592;
	static final double MOL_TO_KMOL_CONV = 1d / 1000d;

	private ReactorFeed reactorFeed;
	private ReactorProduct reactorProduct;

	/**
	 * Main constructor used to build a StyreneProcess object out of a ReactorFeed and ReactorProduct combination
	 * 
	 * @param reactorFeed - a ReactorFeed object representing the feed flow rates to the styrene reactor
	 * @param reactorProduct - a ReactorProduct object representing the product flow rates coming out of the styrene reactor
	 */
	public StyreneProcess(ReactorFeed reactorFeed, ReactorProduct reactorProduct) {
		this.reactorFeed = reactorFeed;
		this.reactorProduct = reactorProduct;
	}

	/**
	 * Constructor used to make a new copy of a StyreneProcess object in memory
	 */
	public StyreneProcess(StyreneProcess styreneProcess) {
		this.reactorFeed = new ReactorFeed(styreneProcess.getReactorFeed());
		this.reactorProduct = new ReactorProduct(styreneProcess.getReactorProduct());
	}

	public ReactorFeed getReactorFeed() {
		return reactorFeed;
	}

	public ReactorProduct getReactorProduct() {
		return reactorProduct;
	}

	/**
	 * Calculates the economic potential in $/hr of the styrene process based off of product value minus raw materials cost
	 */
	public double getEconomicPotential() {
		double styreneValue = reactorProduct.getProdS() * MOL_TO_KMOL_CONV * S_MW * KG_TO_LBM_CONV * S_VALUE;
		double tolueneValue = reactorProduct.getProdT() * MOL_TO_KMOL_CONV * T_MW * KG_TO_LBM_CONV * T_VALUE;
		double hydrogenValue = reactorProduct.getProdH() * MOL_TO_KMOL_CONV * H_MW * KG_TO_LBM_CONV * H_VALUE;
		double methaneValue = reactorProduct.getProdM() * MOL_TO_KMOL_CONV * M_MW * KG_TO_LBM_CONV * M_VALUE;
		double productValue = styreneValue + tolueneValue + hydrogenValue + methaneValue;

		double processFeedCost = reactorFeed.getProcessFeedBasis() * MOL_TO_KMOL_CONV * EB_MW * KG_TO_LBM_CONV * EB_COST;
		double steamCost = reactorFeed.getFeedSteam() * MOL_TO_KMOL_CONV * W_MW * KG_TO_LBM_CONV * STEAM_COST;
		double rawMaterialsCost = processFeedCost + steamCost;

		double wasteWaterCost = reactorProduct.getProdSteam() * MOL_TO_KMOL_CONV * W_MW * KG_TO_LBM_CONV * WW_COST;
		//todo include more operating costs here
		double operatingCosts = wasteWaterCost;

		return productValue - rawMaterialsCost - wasteWaterCost; // economic potential in $/hr
	}

	public String toString() {
		return "Styrene Process Summary:\n\n" + reactorFeed.toString() + "\n\n" + reactorProduct.toString() + "\n";
	}
}